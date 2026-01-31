package com.smartwealth.smartwealth_backend.service.investment;

import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentBuyRequest;
import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentSellRequest;
import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentBuyResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentSellResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.OrderHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.entity.enums.*;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.InvalidBuyConfigurationException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.InvalidSellConfigurationException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.PlanNotFoundException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.repository.investment.InvestmentOrderRepository;
import com.smartwealth.smartwealth_backend.repository.nav.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.nav.projection.LatestNavProjection;
import com.smartwealth.smartwealth_backend.repository.investment.projection.OrderHistoryProjection;
import com.smartwealth.smartwealth_backend.service.common.AfterCommitRedisService;
import com.smartwealth.smartwealth_backend.service.nav.NavCutoffService;
import com.smartwealth.smartwealth_backend.service.holding.UserHoldingService;
import com.smartwealth.smartwealth_backend.service.user.UserService;
import com.smartwealth.smartwealth_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentOrderRepository investmentOrderRepository;
    private final UserService userService;
    private final NavCutoffService navCutoffService;
    private final SchemePlanRepository schemePlanRepository;
    private final WalletService walletService;
    private final AfterCommitRedisService afterCommitRedisService;
    private final UserHoldingService userHoldingService;
    private final NavHistoryRepository navHistoryRepository;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String KEY_PREFIX_IDEMPOTENCY = "investment:idempotency:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvestmentBuyResponse buy(InvestmentBuyRequest request, String customerId, String idempotencyKey) {

        // Check for idempotent request
        String redisKey = getRedisKey(idempotencyKey);

        InvestmentBuyResponse cachedResponse = afterCommitRedisService.redisIdempotencyCheck(
                redisKey,
                idempotencyKey,
                InvestmentBuyResponse.class
        );

        if (cachedResponse != null) {
            log.info("Idempotent buy request detected. Returning cached response for customerId={}, idempotencyKey={}",
                    customerId, idempotencyKey);
            cachedResponse.setMessage(cachedResponse.getMessage() + " (from cache)");
            return cachedResponse;
        }

        try {
            validateBuy(request);

            Long userId = getUserId(customerId);

            OffsetDateTime now = OffsetDateTime.now(IST);

            // lock amount in wallet

            TransactionResponse lockTxn = walletService.lockAmountInWallet(
                    userId,
                    request.getAmount(),
                    idempotencyKey
            );

            if (!lockTxn.getStatus().equals(TransactionStatus.SUCCESS)) {
                throw new TransactionFailedException("Wallet lock failed for lumpsum investment :" + lockTxn.getMessage());
            }

            log.info(
                    "Wallet locked for lumpsum investment. userId={}, amount={}, referenceId={}",
                    userId,
                    request.getAmount(),
                    lockTxn.getReferenceId()
            );

            LocalDate navDate = navCutoffService.calculateApplicableNavDate(request.getPlanId(), InvestmentType.BUY, now);

            InvestmentOrder order = InvestmentOrder.builder()
                    .userId(userId)
                    .planId(request.getPlanId())
                    .sipMandateId(null) // LUMPSUM
                    .investmentType(InvestmentType.BUY)
                    .investmentMode(InvestmentMode.LUMPSUM)
                    .amount(request.getAmount())
                    .units(null) // for sell investment type
                    .status(OrderStatus.PENDING)
                    .applicableNavDate(navDate)
                    .paymentReferenceId(lockTxn.getReferenceId())
                    .paymentStatus(PaymentStatus.FUNDS_LOCKED)
                    .orderTime(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            InvestmentOrder savedOrder = investmentOrderRepository.save(order);

            log.info(
                    "Lumpsum order created. orderId={}, userId={}, navDate={}",
                    order.getInvestmentOrderId(),
                    userId,
                    navDate
            );

            InvestmentBuyResponse response = InvestmentBuyResponse.fromOrder(savedOrder);

            afterCommitRedisService.putAfterCommit(
                    redisKey,
                    response
            );
            return response;

        } catch (PlanNotFoundException ex) {
            log.error("Lumpsum investment failed planId not found: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Lumpsum investment failed: {}", ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvestmentSellResponse sell(InvestmentSellRequest request, String customerId, String idempotencyKey) {

        String redisKey = getRedisKey(idempotencyKey);

        InvestmentSellResponse cachedResponse = afterCommitRedisService.redisIdempotencyCheck(
                redisKey,
                idempotencyKey,
                InvestmentSellResponse.class
        );

        if (cachedResponse != null) {
            log.info("Idempotent sell request detected. Returning cached response for customerId={}, idempotencyKey={}",
                    customerId, idempotencyKey);
            cachedResponse.setMessage(cachedResponse.getMessage() + " (from cache)");
            return cachedResponse;
        }

        try {
            // validate sell request
            validateSell(request);

            Long userId = getUserId(customerId);
            Integer planId = request.getPlanId();

            LatestNavProjection navLatest = navHistoryRepository.findLatestNavByPlanId(planId)
                    .orElseThrow(() -> new PlanNotFoundException("No NAV data found for planId: " + planId));

            BigDecimal unitsToSell = request.getUnits() != null ?
                    request.getUnits() : userHoldingService.getUnitsToSell(request.getAmount(), navLatest.getNavValue());

            // validate sufficient holdings based on requested units(amount) to sell
            userHoldingService.validateSufficientHoldings(userId, request.getPlanId(), unitsToSell);

            OffsetDateTime now = OffsetDateTime.now(IST);

            LocalDate applicableNavDate =
                    navCutoffService.calculateApplicableNavDate(
                            request.getPlanId(),
                            InvestmentType.SELL,
                            now
                    );

            InvestmentOrder order = InvestmentOrder.builder()
                    .userId(userId)
                    .planId(request.getPlanId())
                    .sipMandateId(null)
                    .investmentType(InvestmentType.SELL)
                    .investmentMode(InvestmentMode.LUMPSUM)
                    .amount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount())
                    .units(request.getUnits() == null ? BigDecimal.ZERO : request.getUnits())
                    .applicableNavDate(applicableNavDate)
                    .status(OrderStatus.PENDING)
                    .paymentReferenceId("N/A") // to be updated after allotment
                    .paymentStatus(PaymentStatus.PENDING_CREDIT)
                    .orderTime(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            InvestmentOrder savedOrder = investmentOrderRepository.save(order);

            log.info(
                    "Lumpsum sell order created. orderId={}, userId={}, navDate={}",
                    order.getInvestmentOrderId(),
                    userId,
                    applicableNavDate
            );

            InvestmentSellResponse response = InvestmentSellResponse.fromOrder(savedOrder);

            afterCommitRedisService.putAfterCommit(
                    redisKey,
                    response
            );

            return response;

        } catch (PlanNotFoundException e) {
            log.error("Lumpsum sell failed planId not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lumpsum sell failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public PaginationResponse<OrderHistoryResponse> getOrderHistory(String customerId) {
        Long userId = getUserId(customerId);

        Pageable pageable = Pageable.ofSize(1000);

        Page<OrderHistoryProjection> page =
                investmentOrderRepository.findOrderHistory(userId, pageable);

        List<OrderHistoryResponse> data = page.getContent()
                .stream()
                .map(p -> OrderHistoryResponse.builder()
                        .investmentOrderId(p.getInvestmentOrderId())
                        .planName(p.getPlanName())
                        .investmentType(p.getInvestmentType())
                        .investmentMode(p.getInvestmentMode())
                        .orderStatus(p.getOrderStatus())
                        .paymentStatus(p.getPaymentStatus())
                        .units(p.getUnits())
                        .amount(p.getAmount())
                        .nav(p.getNav())
                        .navDate(p.getNavDate())
                        .orderTime(p.getOrderTime())
                        .build()
                )
                .toList();

        return PaginationResponse.<OrderHistoryResponse>builder()
                .meta(PageMetaResponse.from(page))
                .data(data)
                .build();
    }

    private void validateBuy(InvestmentBuyRequest r) {
        // Basic null checks first to avoid NPEs
        if (r == null) {
            throw new InvalidBuyConfigurationException("Request cannot be null");
        }

        if (r.getPlanId() == null) {
            throw new InvalidBuyConfigurationException("PlanId is required");
        }

        if (r.getAmount() == null) {
            throw new InvalidBuyConfigurationException("Amount is required");
        }

        // Fetch minimum configurable lumpsum amount for the plan
        BigDecimal minAmount;
        try {
            minAmount = schemePlanRepository.findMinLumpsumAmountByPlanId(r.getPlanId())
                    .orElseThrow(() -> new PlanNotFoundException("Invalid planId: " + r.getPlanId()));
        } catch (Exception ex) {
            log.error("Error fetching minimum lumpsum amount for planId={}: {}", r.getPlanId(), ex.getMessage());
            throw ex;
        }

        if (minAmount == null) {
            return; // No minimum amount configured; skip further validation
        }

        if (r.getAmount().compareTo(minAmount) < 0) {
            throw new InvalidBuyConfigurationException("Invalid investment amount; minimum allowed is " + minAmount);
        }
    }

    private void validateSell(InvestmentSellRequest r) {
        boolean hasAmount = r.getAmount() != null;
        boolean hasUnits = r.getUnits() != null;

        if (hasAmount == hasUnits) { // both true or both false
            throw new InvalidSellConfigurationException(
                    "Provide exactly one of, amount or units for sell"
            );
        }

        if (hasAmount && r.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSellConfigurationException("Sell amount must be positive");
        }

        if (hasUnits && r.getUnits().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSellConfigurationException("Sell units must be positive");
        }

        // planId check

        if (!schemePlanRepository.existsById(r.getPlanId())) {
            throw new PlanNotFoundException("Invalid planId");
        }
    }

    private Long getUserId(String customerId) {
        return userService.getUserIdByCustomerId(customerId);
    }

    private String getRedisKey(String idempotencyKey) {
        return KEY_PREFIX_IDEMPOTENCY + idempotencyKey;
    }
}

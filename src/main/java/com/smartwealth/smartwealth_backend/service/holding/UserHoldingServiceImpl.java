package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.holding.UserHolding;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.HoldingNotFoundException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.HoldingUpdateFailedException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.InvalidSellRequestException;
import com.smartwealth.smartwealth_backend.repository.holding.UserHoldingRepository;
import com.smartwealth.smartwealth_backend.repository.holding.projection.HoldingIdProjection;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingSellProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserHoldingServiceImpl implements UserHoldingService {

    private final UserHoldingRepository userHoldingRepository;
    private final HoldingTransactionService holdingTransactionService;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Override
    public void updateUserHoldingsForBuy(
            InvestmentOrder order,
            BigDecimal unitsToBuy,
            PlanNavProjection nav,
            InvestmentMode investmentMode
    ) {

        Long userId = order.getUserId();
        Integer planId = order.getPlanId();
        BigDecimal investedAmount = order.getAmount();
        Long holdingId;

        // Fetch existing holding
        Optional<HoldingIdProjection> existingHoldingId =
                userHoldingRepository.findByUserIdAndPlanId(
                        userId,
                        planId,
                        HoldingIdProjection.class
                );

        if (existingHoldingId.isPresent()) {
            holdingId = existingHoldingId.get().getHoldingId();
            // Update existing holding
            int rowsUpdated = userHoldingRepository.updateUserHoldingUnitsForBuy(
                    userId,
                    planId,
                    unitsToBuy,
                    investedAmount,
                    OffsetDateTime.now(IST)
            );

            if (rowsUpdated == 0) {
                log.error("Failed to update user holding. userId={}, planId={}", userId, planId);
                throw new HoldingUpdateFailedException("Failed to update user holding");
            }
            log.info("User holding updated. userId={}, planId={}, newUnits={}", userId, planId, unitsToBuy);

        } else {
            // Create new holding
            UserHolding holding = UserHolding.builder()
                    .userId(userId)
                    .planId(planId)
                    .totalUnits(unitsToBuy)
                    .totalInvestedAmount(order.getAmount())
                    .totalRedeemedAmount(BigDecimal.ZERO)
                    .isActive(true)
                    .createdAt(OffsetDateTime.now(IST))
                    .updatedAt(OffsetDateTime.now(IST))
                    .build();

            UserHolding savedHolding = userHoldingRepository.save(holding);
            holdingId = savedHolding.getHoldingId();
            log.info("New user holding created. userId={}, planId={}, units={}", userId, planId, unitsToBuy);
        }

        // Create holding transaction record
        holdingTransactionService.createHoldingTransactionRecord(
                holdingId,
                order.getInvestmentOrderId(),
                unitsToBuy,
                investedAmount,
                HoldingTxnType.BUY,
                investmentMode,
                nav
        );
    }

    @Override
    public void updateUserHoldingsForSell(
            InvestmentOrder order,
            BigDecimal unitsToSell,
            BigDecimal redeemedAmount,
            PlanNavProjection nav,
            InvestmentMode investmentMode
    ) {
        Long userId = order.getUserId();
        Integer planId = order.getPlanId();

        Optional<HoldingIdProjection> existingHoldingId =
                userHoldingRepository.findByUserIdAndPlanId(
                        userId,
                        planId,
                        HoldingIdProjection.class
                );

        if (existingHoldingId.isEmpty()) {
            log.error("No existing holding found for sell operation. userId={}, planId={}", userId, planId);
            throw new HoldingNotFoundException("No existing holding found for sell operation");
        }

        Long holdingId = existingHoldingId.get().getHoldingId();

        // Update existing holding
        int rowsUpdated = userHoldingRepository.updateUserHoldingUnitsForSell(
                userId,
                planId,
                unitsToSell,
                redeemedAmount,
                OffsetDateTime.now(IST)
        );

        if (rowsUpdated == 0) {
            log.error("Failed to update user holding for sell. userId={}, planId={}", userId, planId);
            throw new HoldingUpdateFailedException("Failed to update user holding for sell");
        }

        log.info("User holding updated for sell. userId={}, planId={}, unitsSold={}", userId, planId, unitsToSell);

        // Create holding transaction record
        holdingTransactionService.createHoldingTransactionRecord (
                holdingId,
                order.getInvestmentOrderId(),
                unitsToSell,
                redeemedAmount,
                HoldingTxnType.SELL,
                investmentMode,
                nav
        );
    }

    @Override
    public UserHoldingSellProjection getHoldingForSell(
            Long userId,
            Integer planId
    ) {
        return userHoldingRepository.findByUserIdAndPlanId(userId, planId, UserHoldingSellProjection.class)
                .orElseThrow(() -> {
                    log.error("No holding found for userId={}, planId={}", userId, planId);
                    return new HoldingNotFoundException("No holding found for the given user and plan");
                });
    }

    @Override
    public BigDecimal getRedeemAmount(
            BigDecimal unitsToSell,
            BigDecimal navLatestValue
    ) {
        if (unitsToSell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSellRequestException("Invalid SELL units specified");
        }
        return unitsToSell
                .multiply(navLatestValue)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getUnitsToSell(
            BigDecimal redeemAmount,
            BigDecimal navLatestValue
    ) {
        if (redeemAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSellRequestException("Invalid SELL amount specified");
        }
        return redeemAmount
                .divide(navLatestValue, 8, RoundingMode.DOWN); // VERY IMPORTANT
    }

    @Override
    public void validateSufficientHoldings(
            Long userId,
            Integer planId,
            BigDecimal unitsToSell
    ) {
        UserHoldingSellProjection holding = getHoldingForSell(userId, planId);

        if (holding == null || !holding.getIsActive()) {
            log.warn("No active holding found for SELL.");
            throw new InvalidSellRequestException("No active holding found for SELL");
        }

        if (unitsToSell.compareTo(holding.getTotalUnits()) > 0) {
            log.warn(
                    "SELL exceeds holding units, requested={}, available={}",
                    unitsToSell,
                    holding.getTotalUnits()
            );
            throw new InvalidSellRequestException("SELL exceeds holding units");
        }
    }
}

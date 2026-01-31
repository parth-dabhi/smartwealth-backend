package com.smartwealth.smartwealth_backend.service.investment;

import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentAllotment;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.entity.enums.*;
import com.smartwealth.smartwealth_backend.event.*;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.BusinessLogicException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.InvalidInvestmentOrderException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.TemporarySystemException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletTransactionException;
import com.smartwealth.smartwealth_backend.repository.investment.InvestmentAllotmentRepository;
import com.smartwealth.smartwealth_backend.repository.investment.InvestmentOrderRepository;
import com.smartwealth.smartwealth_backend.repository.sip.SipMandateRepository;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;
import com.smartwealth.smartwealth_backend.service.sip.SipExecutionServiceImpl;
import com.smartwealth.smartwealth_backend.service.holding.UserHoldingService;
import com.smartwealth.smartwealth_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentAllotmentServiceImpl implements InvestmentAllotmentService {

    private final InvestmentOrderRepository investmentOrderRepository;
    private final InvestmentAllotmentRepository investmentAllotmentRepository;
    private final WalletService walletService;
    private final SipMandateRepository sipMandateRepository;
    private final UserHoldingService userHoldingService;
    private final ApplicationEventPublisher eventPublisher;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // Core Allotment Logic
    @Transactional(
            rollbackFor = Exception.class,
            propagation = Propagation.REQUIRES_NEW
    )
    public int processSingleOrder(InvestmentOrder order, PlanNavProjection nav) {

        // Idempotency safety
        if (investmentAllotmentRepository.existsByInvestmentOrderId(order.getInvestmentOrderId())) {
            log.warn(
                    "Allotment already exists for orderId={}",
                    order.getInvestmentOrderId()
            );
            return 0;
        }

        // Allot units based on order type BUY/SELL
        switch (order.getInvestmentType()) {
            case BUY -> {
                return allotUnitsForBuy(order, nav);
            }
            case SELL -> {
                return allotUnitsForSell(order, nav);
            }
            default -> {
                log.error("Unknown order type for orderId={}", order.getInvestmentOrderId());
                return 0;
            }
        }
    }

    private int allotUnitsForBuy(InvestmentOrder order, PlanNavProjection nav) {

        Long userId = order.getUserId();
        BigDecimal amount = order.getAmount();

        try {

            String idempotencyKey = String.format(
                    "WALLET-DEBIT-LOCKED-BUY-%s-%d",
                    order.getInvestmentMode(),
                    order.getInvestmentOrderId()
            );

            TransactionResponse lockedDebitTxn;

            // Debit locked amount from wallet Only for BUY orders now.

            if (!(order.getPaymentStatus().equals(PaymentStatus.FUNDS_LOCKED))) {
                throw new BusinessLogicException("Invalid payment state for allotment");
            }

            lockedDebitTxn = walletService.debitLockedAmountInWallet(
                    userId,
                    amount,
                    idempotencyKey
            );

            if (!TransactionStatus.SUCCESS.equals(lockedDebitTxn.getStatus())) {
                log.error("Failed to debit locked amount from wallet. orderId={}", order.getInvestmentOrderId());
                // send notification to user
                throw new TransactionFailedException("Debit of locked amount failed");
            }

            // Calculate units
            BigDecimal units = amount
                    .divide(nav.getNavValue(), 8, RoundingMode.HALF_UP);

            // Update User holdings
            userHoldingService.updateUserHoldingsForBuy(
                    order,
                    units,
                    nav,
                    order.getInvestmentMode()
            );

            // Create allotment record
            InvestmentAllotment allotment = InvestmentAllotment.builder()
                    .investmentOrderId(order.getInvestmentOrderId())
                    .navDate(nav.getNavDate())
                    .navValue(nav.getNavValue())
                    .units(units)
                    .allottedAt(OffsetDateTime.now(IST))
                    .build();

            investmentAllotmentRepository.save(allotment); // Save allotment record

            log.info("Investment allotted. orderId={}, units={}", order.getInvestmentOrderId(), units);

            // SIP lifecycle update
            if (order.getInvestmentMode().equals(InvestmentMode.SIP)) {
                updateSipMandateAfterAllotment(order);
            }

            order.setPaymentReferenceId(lockedDebitTxn.getReferenceId());
            order.setUnits(units);
            order.setStatus(OrderStatus.ALLOTTED); // Update order status
            order.setPaymentStatus(PaymentStatus.FUNDS_DEBITED); // Update payment status
            order.setUpdatedAt(OffsetDateTime.now(IST)); // Update timestamp

            // Save order updates
            investmentOrderRepository.save(order);

            return 1; // One allotment created

        } catch (BusinessLogicException | TransactionFailedException | TemporarySystemException ex) {

            publishReleaseLockedAmount(order, ex);
            throw ex;
        } catch (WalletTransactionException ex) {

            log.error(
                    "Wallet transaction failed for BUY allotment. orderId={}, failureType={}, userId={}, amount={}",
                    order.getInvestmentOrderId(),
                    ex.getFailureType(),
                    order.getUserId(),
                    order.getAmount()
            );
            publishReleaseLockedAmount(order, ex);
            throw ex;
        } catch (Exception ex) {

            log.error("BUY allotment failed for orderId={}", order.getInvestmentOrderId(), ex);

            publishReleaseLockedAmount(order, ex);
            throw new TemporarySystemException("Failed to persist BUY allotment", ex);
        }
    }

    private int allotUnitsForSell(InvestmentOrder order, PlanNavProjection nav) {

        try {

            BigDecimal amount = order.getAmount();

            if (!InvestmentMode.LUMPSUM.equals(order.getInvestmentMode())) {
                log.error("SELL orders cannot be SIP. orderId={}", order.getInvestmentOrderId());
                throw new InvalidInvestmentOrderException("SELL orders cannot be SIP");
            }

            String idempotencyKey = String.format(
                    "WALLET-CREDIT-SELL-%s-%d",
                    order.getInvestmentMode(),
                    order.getInvestmentOrderId()
            );

            BigDecimal navValue = nav.getNavValue();

            BigDecimal unitsToSell;
            BigDecimal redeemAmount;

            // check, requested amount or units are fulfilled by current nav value and user's holdings

            // Determine SELL type
            if (order.getUnits() != null) {
                // SELL BY UNITS
                unitsToSell = order.getUnits();

                redeemAmount = userHoldingService.getRedeemAmount(unitsToSell, nav.getNavValue());

            } else if (amount != null) {
                // SELL BY AMOUNT
                redeemAmount = amount;

                unitsToSell = userHoldingService.getUnitsToSell(redeemAmount, nav.getNavValue()); // VERY IMPORTANT

            } else {
                log.error("Invalid SELL order: neither units nor amount specified. orderId={}", order.getInvestmentOrderId());
                throw new InvalidInvestmentOrderException("Invalid SELL order: neither units nor amount specified");
            }

            // Validate against holdings
            userHoldingService.validateSufficientHoldings(order.getUserId(), order.getPlanId(), unitsToSell);

            // Credit amount to user's wallet
            TransactionResponse creditedTxn = walletService.creditWallet(
                    order.getUserId(),
                    redeemAmount,
                    idempotencyKey
            );

            if (!TransactionStatus.SUCCESS.equals(creditedTxn.getStatus())) {
                log.error("Failed to credit amount in wallet. orderId={}", order.getInvestmentOrderId());
                // send notification to user
                throw new TransactionFailedException("Debit of locked amount failed");
            }

            // Update User holdings
            userHoldingService.updateUserHoldingsForSell(order, unitsToSell, redeemAmount, nav, order.getInvestmentMode());

            // Create allotment (negative units)
            investmentAllotmentRepository.save(
                    InvestmentAllotment.builder()
                            .investmentOrderId(order.getInvestmentOrderId())
                            .navDate(nav.getNavDate())
                            .navValue(navValue)
                            .units(unitsToSell.negate())
                            .allottedAt(OffsetDateTime.now(IST))
                            .build()
            );

            // Update order

            order.setPaymentReferenceId(creditedTxn.getReferenceId());
            order.setUnits(unitsToSell);
            order.setAmount(redeemAmount);
            order.setStatus(OrderStatus.ALLOTTED);
            order.setPaymentStatus(PaymentStatus.FUNDS_CREDITED);
            order.setUpdatedAt(OffsetDateTime.now(IST));

            investmentOrderRepository.save(order);

            log.info("SELL allotted. orderId={}, units={}, amount={}", order.getInvestmentOrderId(), unitsToSell, redeemAmount);

        } catch (WalletTransactionException ex) {
            log.error(
                    "Wallet transaction failed for SELL allotment. orderId={}, failureType={}",
                    order.getInvestmentOrderId(),
                    ex.getFailureType()
            );
            publishInvestmentOrderFailedEvent(order, ex, PaymentStatus.CREDIT_FAILED);
            throw ex;

        } catch (Exception ex) {
            log.error("SELL allotment failed for orderId={}", order.getInvestmentOrderId(), ex);
            publishInvestmentOrderFailedEvent(order, ex, PaymentStatus.CREDIT_FAILED);
            throw new TemporarySystemException("Failed to persist SELL allotment", ex);
        }

        return 1; // One sell order processed
    }

    private void updateSipMandateAfterAllotment(InvestmentOrder order) {
        sipMandateRepository.findById(order.getSipMandateId()).ifPresent(sip -> {

            sip.setCompletedInstallments(Integer.valueOf(sip.getCompletedInstallments() + 1));
            sip.setLastRunAt(OffsetDateTime.now(IST));

            // Compute next run date
            OffsetDateTime nextRunAt = SipExecutionServiceImpl.computeNextRunAt(sip.getLastRunAt(), sip.getSipDay());
            sip.setNextRunAt(nextRunAt);

            // Auto-complete SIP if finished
            if (sip.getCompletedInstallments() >= sip.getTotalInstallments()) {
                sip.setStatus(SipStatus.COMPLETED);
                sip.setNextRunAt(null);
                sip.setEndDate(LocalDate.now(IST));
            }

            sip.setUpdatedAt(OffsetDateTime.now(IST));

            log.info(
                    "SIP mandate updated after allotment. sipId={}, installment={}/{}",
                    sip.getSipMandateId(),
                    sip.getCompletedInstallments(),
                    sip.getTotalInstallments()
            );

            sipMandateRepository.save(sip); // Save SIP mandate updates
        });
    }

    private void publishReleaseLockedAmount(InvestmentOrder order, Exception ex) {
        String idempotencyKeyPrefix = "WALLET-RELEASE-LOCKED-BUY-"
                + order.getInvestmentMode() + "-"
                + order.getInvestmentOrderId();

        eventPublisher.publishEvent(
                new ReleaseLockedAmountEvent(
                        order.getInvestmentOrderId(),
                        order.getUserId(),
                        order.getSipMandateId(),
                        OffsetDateTime.now(IST),
                        order.getAmount(),
                        idempotencyKeyPrefix,
                        order.getInvestmentMode(),
                        ex
                )
        );
    }

    private void publishInvestmentOrderFailedEvent(InvestmentOrder order, Exception ex, PaymentStatus paymentStatus) {
        log.error(
                "General failure during allotment. orderId={}",
                order.getInvestmentOrderId(),
                ex
        );

        // Publish failure event
        eventPublisher.publishEvent(
                new InvestmentOrderFailedEvent(
                        order.getInvestmentOrderId(),
                        order.getUserId(),
                        order.getSipMandateId(),
                        OffsetDateTime.now(IST),
                        order.getInvestmentMode(),
                        paymentStatus,
                        null,
                        ex
                )
        );
    }
}

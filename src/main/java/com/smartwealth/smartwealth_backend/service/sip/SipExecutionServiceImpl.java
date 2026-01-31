package com.smartwealth.smartwealth_backend.service.sip;

import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;
import com.smartwealth.smartwealth_backend.entity.enums.*;
import com.smartwealth.smartwealth_backend.event.SipExecutionFailedEvent;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.InvalidSipConfigurationException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.SipExecutionFailedException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletTransactionException;
import com.smartwealth.smartwealth_backend.repository.investment.InvestmentOrderRepository;
import com.smartwealth.smartwealth_backend.service.nav.NavCutoffService;
import com.smartwealth.smartwealth_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class SipExecutionServiceImpl implements SipExecutionService{

    private final InvestmentOrderRepository investmentOrderRepository;
    private final NavCutoffService navCutoffService;
    private final WalletService walletService;
    private final ApplicationEventPublisher eventPublisher;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // Core Execution Logic
    @Transactional(
            rollbackFor = Exception.class
    )
    public int executeSingleSip(SipMandate sip, OffsetDateTime now) {

        String idempotencyKey = String.format(
                "SIP-EXEC-LOCK-%d-%s-INST-%d",
                sip.getSipMandateId(),
                now.toLocalDate().toString(),
                sip.getCompletedInstallments() + 1
        );

        // Debit wallet
        try {
            TransactionResponse lockTxn = walletService.lockAmountInWallet(
                    sip.getUserId(),
                    sip.getSipAmount(),
                    idempotencyKey
            );

            if (!lockTxn.getStatus().equals(TransactionStatus.SUCCESS)) {
                log.warn("SIP lock payment not successful. sipId={}, status={}", sip.getSipMandateId(), lockTxn.getStatus());
                throw new SipExecutionFailedException("SIP lock payment failed: " + lockTxn.getMessage());
            }

            // Compute NAV date using cut-off service
            LocalDate navDate = navCutoffService.calculateApplicableNavDate(
                    sip.getPlanId(),
                    InvestmentType.BUY,
                    now
            );

            InvestmentOrder order = InvestmentOrder.builder()
                    .userId(sip.getUserId())
                    .planId(sip.getPlanId())
                    .sipMandateId(sip.getSipMandateId())
                    .investmentType(InvestmentType.BUY)
                    .investmentMode(InvestmentMode.SIP)
                    .amount(sip.getSipAmount())
                    .orderTime(now)
                    .applicableNavDate(navDate)
                    .status(OrderStatus.PENDING)
                    .paymentReferenceId(lockTxn.getReferenceId())
                    .paymentStatus(PaymentStatus.FUNDS_LOCKED)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            investmentOrderRepository.save(order);

            log.info(
                    "SIP executed. sipId={}, orderId={}, installment={}/{}",
                    sip.getSipMandateId(),
                    order.getInvestmentOrderId(),
                    sip.getCompletedInstallments(),
                    sip.getTotalInstallments()
            );

        } catch (WalletTransactionException ex) {

            log.error(
                    "Wallet transaction failed for SIP execution. sipId={}, userId={}, amount={}, failureType={}",
                    sip.getSipMandateId(),
                    sip.getUserId(),
                    sip.getSipAmount(),
                    ex.getFailureType()
            );

            publishSipExecutionFailureEvent(sip, now, ex);

            throw ex;
        } catch (Exception ex) {
            log.error("SIP wallet lock failed. sipId={}, userId={}", sip.getSipMandateId(), sip.getUserId(), ex);

            // Update SIP schedule even on failure to avoid repeated attempt, it will be retried next month
            publishSipExecutionFailureEvent(sip, now, ex);

            throw ex;
        }
        return 1;
    }

    private void publishSipExecutionFailureEvent(SipMandate sip, OffsetDateTime now, Exception ex) {
        // Update SIP schedule even on failure to avoid repeated attempt,
        // it will be retried next month
        eventPublisher.publishEvent(
                new SipExecutionFailedEvent(
                        sip.getSipMandateId(),
                        sip.getUserId(),
                        now,
                        ex
                )
        );
    }

    public static OffsetDateTime computeNextRunAt(OffsetDateTime lastRunAt, int sipDay) {
        // Validate sipDay (1-28 ensures day exists in all months)
        if (sipDay < 1 || sipDay > 28) {
            throw new InvalidSipConfigurationException("sipDay must be between 1 and 28");
        }

        LocalDate targetDate;

        if (lastRunAt == null) {
            // First SIP: schedule for sipDay of current or next month
            LocalDate today = LocalDate.now(IST);
            targetDate = today.withDayOfMonth(sipDay);

            // If that day has already passed this month, move to next month
            if (targetDate.isBefore(today) || targetDate.equals(today)) {
                targetDate = targetDate.plusMonths(1);
            }
        } else {
            // Subsequent SIPs: next month on sipDay
            LocalDate lastRun = lastRunAt.atZoneSameInstant(IST).toLocalDate();
            targetDate = lastRun.plusMonths(1).withDayOfMonth(sipDay);
        }

        return targetDate
                .atTime(9, 0)
                .atZone(IST)
                .toOffsetDateTime();
    }
}

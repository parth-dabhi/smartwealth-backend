package com.smartwealth.smartwealth_backend.event.handler;

import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.enums.PaymentStatus;
import com.smartwealth.smartwealth_backend.event.InvestmentOrderFailedEvent;
import com.smartwealth.smartwealth_backend.event.RefundAmountEvent;
import com.smartwealth.smartwealth_backend.event.ReleaseLockedAmountEvent;
import com.smartwealth.smartwealth_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletCompensationEventHandler {

    private final WalletService walletService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReleaseLockedAmountEvent(ReleaseLockedAmountEvent event) {
        Long userId = event.getUserId();
        Long orderId = event.getOrderId();
        BigDecimal amountToRelease = event.getAmount();

        TransactionResponse savedTxn;
        try {
            savedTxn = walletService.unlockAmountInWallet(
                    userId,
                    amountToRelease,
                    event.getIdempotencyKeyPrefix()
            );
        } catch (Exception releaseEx) {
            log.error(
                    "CRITICAL: Failed to release locked funds. amount={} - Manual intervention required",
                    event.getOrderId(),
                    releaseEx
            );
            return;
        }

        // On successful release, publish InvestmentOrderFailedEvent
        eventPublisher.publishEvent(
                new InvestmentOrderFailedEvent(
                        orderId,
                        event.getUserId(),
                        event.getSipMandateId(),
                        event.getFailedAt(),
                        event.getInvestmentMode(),
                        PaymentStatus.FUNDS_REVERSED,
                        savedTxn.getReferenceId(),
                        event.getEx()
                )
        );

        log.info("Locked amount released for investment order. orderId={}", orderId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRefundAmountEvent(RefundAmountEvent event) {
        Long userId = event.getUserId();
        Long orderId = event.getOrderId();
        BigDecimal amountToRefund = event.getAmount();
        String refundKeyPrefix = event.getReleaseKeyPrefix();

        walletService.refundDebitedAmountInWallet(
                userId,
                amountToRefund,
                refundKeyPrefix + orderId
        );
        log.info("Amount refunded to wallet for investment order. orderId={}", orderId);
    }
}

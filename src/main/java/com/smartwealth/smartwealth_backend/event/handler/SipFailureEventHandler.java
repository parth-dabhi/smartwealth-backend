package com.smartwealth.smartwealth_backend.event.handler;

import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;
import com.smartwealth.smartwealth_backend.event.SipExecutionFailedEvent;
import com.smartwealth.smartwealth_backend.event.SipSuspendedEvent;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.SipMandateNotFoundException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletTransactionException;
import com.smartwealth.smartwealth_backend.repository.sip.SipMandateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static com.smartwealth.smartwealth_backend.service.sip.SipExecutionServiceImpl.computeNextRunAt;

@Component
@RequiredArgsConstructor
@Slf4j
public class SipFailureEventHandler {

    private final SipMandateRepository sipMandateRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${sip.failure.max-attempts}")
    private int maxFailureAttempts;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSipFailure(SipExecutionFailedEvent event) {

        sipMandateRepository.findBySipMandateIdAndUserId(
                event.getSipMandateId(),
                event.getUserId(),
                SipMandate.class
        ).ifPresent(sip -> {
            sip.setLastRunAt(event.getFailedAt()); // Mark that as attempted today
            sip.setNextRunAt(computeNextRunAt(event.getFailedAt(), sip.getSipDay()));
            sip.setLastFailureAt(event.getFailedAt());
            sip.setUpdatedAt(OffsetDateTime.now(IST));

            // If failure is due to wallet transaction then only increment failure count otherwise only update last failure time
            if (event.getEx() instanceof WalletTransactionException wtxEx && wtxEx.shouldIncrementFailureCount()) {

                Integer newFailureCount = Integer.valueOf(sip.getFailureCount() + 1);
                sip.setFailureCount(newFailureCount);

                log.warn("SIP failure count incremented. sipId={}, userId={}, newFailureCount={}, reason={}",
                        sip.getSipMandateId(), sip.getUserId(), newFailureCount, wtxEx.getFailureType());

                // If failure count reaches threshold, publish suspension event
                if (newFailureCount >= maxFailureAttempts) {
                    eventPublisher.publishEvent(
                            new SipSuspendedEvent(
                                    sip.getSipMandateId(),
                                    sip.getUserId(),
                                    newFailureCount
                            )
                    );
                }
            }
            sipMandateRepository.save(sip); // Save updated SIP mandate
        });
        log.info("SIP schedule updated after rollback. sipId={}", event.getSipMandateId());
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSipSuspended(SipSuspendedEvent event) {

        int rowsUpdated = sipMandateRepository.suspendSip(
                event.getSipMandateId(),
                event.getUserId()
        );

        if (rowsUpdated == 0) {
            log.error("Failed to suspend SIP mandate. sipId={}, userId={}",
                    event.getSipMandateId(), event.getUserId());
            throw new SipMandateNotFoundException("SIP mandate not found while suspending. sipId=" + event.getSipMandateId());
        }

        log.error(
                "SIP auto-suspended due to repeated failures. sipId={}, userId={}, failureCount={}",
                event.getSipMandateId(),
                event.getUserId(),
                event.getNewFailureCount()
        );

        // mail to user about suspension
    }
}


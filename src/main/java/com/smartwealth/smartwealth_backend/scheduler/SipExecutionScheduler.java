package com.smartwealth.smartwealth_backend.scheduler;

import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;
import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;
import com.smartwealth.smartwealth_backend.repository.sip.SipMandateRepository;
import com.smartwealth.smartwealth_backend.service.sip.SipExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SipExecutionScheduler {

    private final SipExecutionService sipExecutionService;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private final SipMandateRepository sipMandateRepository;

    // Note: Don't execute twice a day, otherwise SIPs will be processed twice. Now disabled.
    // Now works via manual trigger as service class
    @Scheduled(cron = "0 0 9 1-28 * ?", zone = "Asia/Kolkata")
    public String executeSipMandates() {

        log.info("Starting scheduled SIP Execution...");

        int success = 0;
        int failed = 0;

        OffsetDateTime now = OffsetDateTime.now(IST);

        // Fetch SIP mandates that are ACTIVE and due for execution
        List<SipMandate> dueSips =
                sipMandateRepository.findDueSipMandatesByStatusAndNextRunAt(SipStatus.ACTIVE, now);

        if (dueSips.isEmpty()) {
            log.info("No SIP mandates due for execution at this time.");
            return "success: 0, failed: 0";
        }

        for (SipMandate sip : dueSips) {

            if (!sip.getStatus().equals(SipStatus.ACTIVE)) {
                log.warn("Skipping SIP mandate id {} as its status is not ACTIVE", sip.getSipMandateId());
                continue;
            }

            // Check if today is the scheduled SIP day, if not, skip
            // TODO: Uncomment if SIP day check is needed
//            if (sip.getSipDay() != now.getDayOfMonth()) continue;

            try {
                // Execute the SIP mandate
                success += sipExecutionService.executeSingleSip(sip, now);
            } catch (Exception e) {
                log.error("Error executing SIP mandate id {}: {}", sip.getSipMandateId(), e.getMessage());
                failed++;
            }

        }

        log.info("Scheduled SIP Execution completed. Success: {}, Failed: {}", success, failed);

        return "success: " + success + ", failed: " + failed;
    }
}
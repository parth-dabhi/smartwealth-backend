package com.smartwealth.smartwealth_backend.scheduler;

import com.smartwealth.smartwealth_backend.service.impl.NavImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NavScheduler {
    private final NavImportService navImportService;

    @Scheduled(cron = "0 30 23 ? * MON-FRI", zone = "Asia/Kolkata")
    public void fetchDailyNav() {
        navImportService.importTodayNav();
    }
}

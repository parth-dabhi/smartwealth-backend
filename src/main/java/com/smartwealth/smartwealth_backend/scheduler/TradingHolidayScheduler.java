package com.smartwealth.smartwealth_backend.scheduler;

import com.smartwealth.smartwealth_backend.service.common.TradingHolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradingHolidayScheduler {

    private final TradingHolidayService tradingHolidayService;

    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    @CacheEvict(value = "tradingHolidays", allEntries = true)
    public void syncTradingHolidays() {

        log.info("Starting NSE trading holiday sync");

        int insertedCount = tradingHolidayService.syncTradingHolidays();

        log.info("NSE trading holiday sync completed. Inserted {} new holidays.", insertedCount);
    }
}

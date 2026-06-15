package com.smartwealth.smartwealth_backend.scheduler;

import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.nav.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.service.common.TradingHolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class NavAnchorScheduler {

    private final NavHistoryRepository navHistoryRepository;
    private final SchemePlanRepository schemePlanRepository;
    private final TradingHolidayService tradingHolidayService;

    /**
     * Nightly pipeline — runs at 23:30 IST every day.
     *
     * Step 1  importTodayNav()              — fetch today's NAV from AMFI → nav_history
     * Step 2  refreshNavAnchors()           — update 4 anchor NAVs per plan (3,336 rows)
     * Step 3  recalculateReturnsFromAnchors() — compute return_1y/3y/5y from anchors
     * Step 4  recalculateAllScores()        — compute composite score from returns
     *
     * Each step is logged with timing so you can see how long each takes.
     */
    @Scheduled(cron = "0 35 23 * * *", zone = "Asia/Kolkata")
    public void runNightlyPipeline() {

        log.info("  Nightly NAV pipeline — starting");

        // ── Step 1 ── Import today's NAV

        // ── Step 2 ── Refresh nav_anchors
        try {
            LocalDate latestNavDate = resolveLatestNavDate();
            navHistoryRepository.refreshNavAnchors(latestNavDate);
            log.info("nav_anchors refreshed");
        } catch (Exception e) {
            log.error("nav_anchors refresh failed — continuing pipeline", e);
        }

        // ── Step 3 ── Recalculate return_1y / 3y / 5y
        try {
            schemePlanRepository.recalculateReturnsFromAnchors();
            log.info("Returns recalculated");
        } catch (Exception e) {
            log.error("Return recalculation failed — continuing pipeline", e);
        }

        // ── Step 4 ── Recalculate composite score
        try {
            schemePlanRepository.recalculateAllScores();
            log.info("Scores recalculated");
        } catch (Exception e) {
            log.error("Score recalculation failed", e);
        }

        log.info("Nightly pipeline complete");
    }

    private LocalDate resolveLatestNavDate() {
        LocalDate candidate = LocalDate.now().minusDays(1);
        int maxLookback = 10;

        for (int i = 0; i < maxLookback; i++) {
            if (isTradingDay(candidate)) {
                log.info("Latest real NAV date resolved: {}", candidate);
                return candidate;
            }
            candidate = candidate.minusDays(1);
        }

        log.warn("Could not resolve latest NAV date in last {} days — using today", maxLookback);
        return LocalDate.now();
    }

    private boolean isTradingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return !tradingHolidayService.isHoliday(date);
    }
}

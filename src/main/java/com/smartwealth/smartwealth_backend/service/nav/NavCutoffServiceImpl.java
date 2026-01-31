package com.smartwealth.smartwealth_backend.service.nav;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentType;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.PlanNotFoundException;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.service.common.TradingHolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NavCutoffServiceImpl implements NavCutoffService {

    private final TradingHolidayService tradingHolidayService;
    private final SchemePlanRepository schemePlanRepository;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private static final LocalTime LIQUID_OVERNIGHT_PURCHASE_CUTOFF = LocalTime.of(13, 30); // 1:30 PM
    private static final LocalTime REGULAR_CUTOFF = LocalTime.of(15, 0);          // 3:00 PM
    private static final LocalTime INTERNATIONAL_CUTOFF = LocalTime.of(11, 0);    // 11:00 AM

    private static final int INTERNATIONAL_CATEGORY_ID = 25;
    private static final int LIQUID_CATEGORY_ID = 32;
    private static final int OVERNIGHT_CATEGORY_ID = 33;

    @Override
    public LocalDate calculateApplicableNavDate(Integer planId, InvestmentType transactionType, OffsetDateTime orderTime) {

        // Normalize to IST
        ZonedDateTime istTime = orderTime.atZoneSameInstant(IST);

        LocalDate orderDate = istTime.toLocalDate();
        LocalTime orderLocalTime = istTime.toLocalTime();

        boolean isTradingDay = isTradingDay(orderDate);
        LocalTime cutoffTime = getCutoffTime(planId, transactionType);

        // Rule: Nav is allocated only on trading days, even if order is placed on non-trading day and some plans publish navs on non-trading days.

        // Case 1: Trading day & before cut-off
        if (isTradingDay && orderLocalTime.isBefore(cutoffTime)) {
            return orderDate;
        }

        // Case 2: After cut-off OR non-trading day
        return nextTradingDay(orderDate);
    }

    private LocalTime getCutoffTime(Integer planId, InvestmentType transactionType) {

        Integer categoryId = schemePlanRepository.findCategoryIdByPlanId(planId)
                .orElseThrow(() -> new PlanNotFoundException("Invalid planId: " + planId));

        // International Equity Funds (BUY & SELL both)
        if (categoryId == INTERNATIONAL_CATEGORY_ID) {
            return INTERNATIONAL_CUTOFF; // 11:00 AM
        }

        // Liquid / Overnight Funds
        if (categoryId == LIQUID_CATEGORY_ID || categoryId == OVERNIGHT_CATEGORY_ID) {
            return transactionType == InvestmentType.BUY
                    ? LIQUID_OVERNIGHT_PURCHASE_CUTOFF // 1:30 PM
                    : REGULAR_CUTOFF;                  // 3:00 PM
        }

        // All other schemes
        return REGULAR_CUTOFF; // 3:00 PM
    }

    private boolean isTradingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return !tradingHolidayService.isHoliday(date);
    }

    private LocalDate nextTradingDay(LocalDate date) { // Find the next trading day after the given date
        LocalDate next = date.plusDays(1);
        while (!isTradingDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }
}

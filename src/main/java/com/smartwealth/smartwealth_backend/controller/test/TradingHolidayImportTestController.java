package com.smartwealth.smartwealth_backend.controller.test;

import com.smartwealth.smartwealth_backend.service.common.TradingHolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/trading-holiday-import")
public class TradingHolidayImportTestController {
    private final TradingHolidayService tradingHolidayService;

    @PostMapping
    public String importTradingHolidays() {
        int importedCount = tradingHolidayService.syncTradingHolidays();
        return "Trading holidays import initiated. Imported " + importedCount + " holidays.";
    }
}

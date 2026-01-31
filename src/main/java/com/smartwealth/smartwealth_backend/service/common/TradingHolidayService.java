package com.smartwealth.smartwealth_backend.service.common;

import com.smartwealth.smartwealth_backend.repository.mutual_fund.TradingHolidayDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingHolidayService {

    private final TradingHolidayDao tradingHolidayDao;
    private final RestTemplate restTemplate;

    private static final String NSE_HOLIDAY_API = "https://www.nseindia.com/api/holiday-master?type=trading";
    private static final DateTimeFormatter NSE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    /**
     * Used by NAV logic
     */
    @Cacheable(value = "tradingHolidays", key = "#date.toString()")
    public boolean isHoliday(LocalDate date) {
        return tradingHolidayDao.existsByDate(date);
    }

    /**
     * Used by scheduler to fetch and store MF trading holidays
     */
    @CacheEvict(value = "tradingHolidays", allEntries = true)
    public int syncTradingHolidays() {
        try {
            log.info("Fetching trading holidays from NSE API");

            // Set headers to mimic browser request (NSE requires this)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            headers.set("Accept-Language", "en-US,en;q=0.9");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    NSE_HOLIDAY_API,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getBody() == null) {
                log.error("Empty response from NSE API");
                return 0;
            }

            Object mfObj = response.getBody().get("MF");
            if (!(mfObj instanceof List<?>)) {
                log.warn("MF key missing or invalid in NSE response");
                return 0;
            }

            // Extract MF holidays from response
            List<Map<String, Object>> mfHolidays =
                    (List<Map<String, Object>>) response.getBody().get("MF");

            if (mfHolidays == null || mfHolidays.isEmpty()) {
                log.warn("No MF holidays found in API response");
                return 0;
            }

            // Parse dates
            List<LocalDate> holidays = mfHolidays.stream()
                    .map(holiday -> {
                        String tradingDate = (String) holiday.get("tradingDate");
                        return parseNseDate(tradingDate);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Save to database
            int savedCount = tradingHolidayDao.saveAll(holidays);

            log.info("Successfully synced {} MF trading holidays (total: {})",
                    savedCount, holidays.size());

            return savedCount;

        } catch (Exception e) {
            log.error("Error syncing trading holidays from NSE", e);
            throw new RuntimeException("Failed to sync trading holidays", e);
        }
    }

    /**
     * Parse NSE date format (dd-MMM-yyyy)
     */
    private LocalDate parseNseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, NSE_DATE_FORMAT);
        } catch (Exception e) {
            log.error("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Get all upcoming holidays
     */
    public List<LocalDate> getUpcomingHolidays() {
        // You can add a method in DAO if needed
        return List.of(); // placeholder
    }
}
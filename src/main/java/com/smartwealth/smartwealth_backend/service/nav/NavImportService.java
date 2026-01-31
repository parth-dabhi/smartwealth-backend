package com.smartwealth.smartwealth_backend.service.nav;

import com.smartwealth.smartwealth_backend.repository.nav.NavHistoryRepository;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.IsinAndPlanIdProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavImportService {

    private static final String TODAY_NAV_URL =
            "https://portal.amfiindia.com/spages/NAVAll.txt";

    private static final String HISTORICAL_NAV_URL =
            "https://portal.amfiindia.com/DownloadNAVHistoryReport_Po.aspx?frmdt=%s";

    private static final DateTimeFormatter NAV_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    private final SchemePlanRepository schemePlanRepository;
    private final NavHistoryRepository navHistoryRepository;

    // PUBLIC APIs

    @Transactional
    @CacheEvict(value = {"latestNav", "navHistory", "planDetail"}, allEntries = true)
    public String importTodayNav() {
        log.info("Starting TODAY NAV import");
        return importNavFromUrl(TODAY_NAV_URL, NavFileType.TODAY);
    }

    @Transactional
    @CacheEvict(value = {"latestNav", "navHistory", "planDetail"}, allEntries = true)
    public String importHistoricalNav(String fromDate) {
        log.info("Starting HISTORICAL NAV import for date: {}", fromDate);
        String url = String.format(HISTORICAL_NAV_URL, fromDate);
        return importNavFromUrl(url, NavFileType.HISTORICAL);
    }

    // CORE IMPORT ENGINE

    private String importNavFromUrl(String url, NavFileType fileType) {

        Map<String, Integer> isinToPlanId = loadIsinMap();
        log.info("Loaded ISIN mappings: {}", isinToPlanId.size());

        List<NavInsertRow> batchRows = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {

            String line;
            boolean dataSectionStarted = fileType == NavFileType.TODAY;

            while ((line = reader.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) continue;

                if (fileType == NavFileType.HISTORICAL) {
                    if (line.startsWith("Scheme Code;")) {
                        dataSectionStarted = true;
                        continue;
                    }
                    if (!dataSectionStarted || !line.contains(";")) continue;
                } else {
                    if (!line.contains(";")) continue;
                }

                String[] parts = splitLine(line, fileType);
                if (parts == null) continue;

                NavParsedRow parsedRow = parseNavRow(parts, fileType);
                if (parsedRow == null) continue;

                addIfValid(
                        parsedRow.isinPrimary(),
                        parsedRow.navDate(),
                        parsedRow.navValue(),
                        isinToPlanId,
                        batchRows
                );

                addIfValid(
                        parsedRow.isinReinvest(),
                        parsedRow.navDate(),
                        parsedRow.navValue(),
                        isinToPlanId,
                        batchRows
                );
            }

            int insertedCount = bulkInsert(batchRows);

            log.info("{} NAV import completed. Records inserted: {}",
                    fileType.name(), insertedCount);

            return fileType.name() + " NAV import completed. Records inserted: " + insertedCount;

        } catch (Exception e) {
            log.error("{} NAV import failed", fileType.name(), e);
            return fileType.name() + " NAV import failed.";
        }
    }

    // PARSING HELPERS

    private String[] splitLine(String line, NavFileType fileType) {
        String[] parts = line.split(";", -1);
        return fileType == NavFileType.TODAY
                ? (parts.length >= 6 ? parts : null)
                : (parts.length >= 8 ? parts : null);
    }

    private NavParsedRow parseNavRow(String[] parts, NavFileType fileType) {
        try {
            String isinPrimary = fileType == NavFileType.TODAY ? parts[1].trim() : parts[2].trim();
            String isinReinvest = fileType == NavFileType.TODAY ? parts[2].trim() : parts[3].trim();
            String navStr = parts[4].trim();
            String dateStr = fileType == NavFileType.TODAY ? parts[5].trim() : parts[7].trim();

            LocalDate navDate = LocalDate.parse(dateStr, NAV_DATE_FORMAT);
            BigDecimal navValue = new BigDecimal(navStr);

            return new NavParsedRow(isinPrimary, isinReinvest, navDate, navValue);
        } catch (Exception e) {
            return null;
        }
    }

    // BATCH HELPERS

    private void addIfValid(
            String isin,
            LocalDate navDate,
            BigDecimal navValue,
            Map<String, Integer> isinToPlanId,
            List<NavInsertRow> batch
    ) {
        if (isin == null || isin.isBlank() || "-".equals(isin)) return;

        Integer planId = isinToPlanId.get(isin);
        if (planId == null) return;

        batch.add(new NavInsertRow(planId, navDate, navValue));
    }

    private int bulkInsert(List<NavInsertRow> rows) {
        if (rows.isEmpty()) return 0;

        Integer[] planIds = rows.stream().map(NavInsertRow::planId).toArray(Integer[]::new);
        LocalDate[] navDates = rows.stream().map(NavInsertRow::navDate).toArray(LocalDate[]::new);
        BigDecimal[] navValues = rows.stream().map(NavInsertRow::navValue).toArray(BigDecimal[]::new);

        if (planIds.length != navDates.length || planIds.length != navValues.length) {
            throw new IllegalStateException("Mismatched batch array lengths");
        }

        return navHistoryRepository.bulkInsertNav(planIds, navDates, navValues);
    }

    private Map<String, Integer> loadIsinMap() {
        return schemePlanRepository.findIsinAndPlanIdMap()
                .stream()
                .collect(Collectors.toMap(
                        IsinAndPlanIdProjection::getIsin,
                        IsinAndPlanIdProjection::getPlanId
                ));
    }

    // INTERNAL STRUCTURES

    private enum NavFileType {
        TODAY,
        HISTORICAL
    }

    private record NavParsedRow(
            String isinPrimary,
            String isinReinvest,
            LocalDate navDate,
            BigDecimal navValue
    ) {
    }

    private record NavInsertRow(
            Integer planId,
            LocalDate navDate,
            BigDecimal navValue
    ) {
    }
}

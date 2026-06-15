package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.common.AssetMix;
import com.smartwealth.smartwealth_backend.dto.common.RecommendedLumpsumSchemeDto;
import com.smartwealth.smartwealth_backend.dto.common.RecommendedSipSchemeDto;
import com.smartwealth.smartwealth_backend.dto.common.SchemeCount;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.TopLumpsumPlanProjection;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.TopSipPlanProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Selects the final recommended schemes for both SIP and Lumpsum investments.
 *
 * Flow for SIP:
 *  1. DB returns best-scored plan per (asset × category) bucket — ordered by score DESC.
 *  2. Java groups by asset_id (1=equity, 2=debt).
 *  3. Equity pool → filter by min_sip affordability → take top N → split amount equally.
 *  4. Debt pool   → same logic.
 *  5. If either pool ends up empty → reallocate that money to the other asset class.
 *
 * Flow for Lumpsum:
 *  Same, but uses min_investment instead of min_sip.
 *  Amount per scheme is calculated BEFORE filtering so the affordability check
 *  uses the real per-scheme amount, not the total.
 *
 * Key design decisions:
 *  - DB does category-level ranking (DISTINCT ON + ORDER BY score DESC).
 *  - Java does affordability filtering. These are two separate concerns.
 *  - No Java-level re-sorting: DB already picked the best plan per category.
 *    Java just limits count and checks min amount — it does NOT re-rank.
 *  - Fallback reallocation: if debt has no affordable plans, debt money moves
 *    to equity rather than disappearing from the recommendation silently.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchemeSelectionService {

    private final SchemePlanRepository schemePlanRepository;

    // PUBLIC API

    public List<RecommendedSipSchemeDto> pickSipSchemes(
            AssetMix mix,
            BigDecimal sipAmount,
            int durationMonths,
            SchemeCount schemeCount,
            Integer riskProfileId
    ) {
        // Single DB call — returns best plan per (asset × category), score-ranked
        Map<Integer, List<TopSipPlanProjection>> byAsset =
                schemePlanRepository.findAllTopSipPlans(durationMonths, riskProfileId)
                        .stream()
                        .sorted(Comparator.comparing(TopSipPlanProjection::getScore,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.groupingBy(TopSipPlanProjection::getAssetId));

        log.info("SIP candidates — equity: {}, debt: {}",
                byAsset.getOrDefault(1, List.of()).size(),
                byAsset.getOrDefault(2, List.of()).size());

        BigDecimal equityAmount = mix.getEquityAmount(sipAmount);
        BigDecimal debtAmount   = mix.getDebtAmount(sipAmount);

        // ── Build debt first so we know if it failed (needed for reallocation) ──

        List<RecommendedSipSchemeDto> debtSchemes = Collections.emptyList();

        if (debtAmount.compareTo(BigDecimal.ZERO) > 0 && schemeCount.debtCount() > 0) {
            debtSchemes = buildSipSchemes(
                    byAsset.getOrDefault(2, List.of()),
                    schemeCount.debtCount(),
                    durationMonths,
                    debtAmount
            );
        }

        // ── If debt is empty, give its allocation to equity ──
        BigDecimal effectiveEquityAmount = debtSchemes.isEmpty() ? sipAmount : equityAmount;

        if (debtSchemes.isEmpty() && debtAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("No affordable debt SIP plans found for duration={}m, amount={}. Reallocating to equity.",
                    durationMonths, debtAmount);
        }

        // ── Build equity ──
        List<RecommendedSipSchemeDto> equitySchemes = Collections.emptyList();

        if (effectiveEquityAmount.compareTo(BigDecimal.ZERO) > 0 && schemeCount.equityCount() > 0) {
            equitySchemes = buildSipSchemes(
                    byAsset.getOrDefault(1, List.of()),
                    schemeCount.equityCount(),
                    durationMonths,
                    effectiveEquityAmount
            );
        }

        log.info("SIP result — equity schemes: {}, debt schemes: {}",
                equitySchemes.size(), debtSchemes.size());

        List<RecommendedSipSchemeDto> result = new ArrayList<>();
        result.addAll(equitySchemes);
        result.addAll(debtSchemes);
        return result;
    }

    public List<RecommendedLumpsumSchemeDto> pickLumpsumSchemes(
            AssetMix mix,
            BigDecimal lumpsumAmount,
            int durationMonths,
            SchemeCount schemeCount,
            Integer riskProfileId
    ) {
        // Single DB call — returns best plan per (asset × category), score-ranked
        Map<Integer, List<TopLumpsumPlanProjection>> byAsset =
                schemePlanRepository.findAllTopLumpsumPlans(durationMonths, riskProfileId)
                        .stream()
                        .sorted(Comparator.comparing(TopLumpsumPlanProjection::getScore,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.groupingBy(TopLumpsumPlanProjection::getAssetId));

        log.info("Lumpsum candidates — equity: {}, debt: {}",
                byAsset.getOrDefault(1, List.of()).size(),
                byAsset.getOrDefault(2, List.of()).size());

        BigDecimal equityAmount = mix.getEquityAmount(lumpsumAmount);
        BigDecimal debtAmount   = mix.getDebtAmount(lumpsumAmount);

        // ── Build debt first so we know if it failed ──

        List<RecommendedLumpsumSchemeDto> debtSchemes = Collections.emptyList();

        if (debtAmount.compareTo(BigDecimal.ZERO) > 0 && schemeCount.debtCount() > 0) {
            debtSchemes = buildLumpsumSchemes(
                    byAsset.getOrDefault(2, List.of()),
                    schemeCount.debtCount(),
                    debtAmount
            );
        }

        // ── If debt is empty, give its allocation to equity ──
        BigDecimal effectiveEquityAmount = debtSchemes.isEmpty() ? lumpsumAmount : equityAmount;

        if (debtSchemes.isEmpty() && debtAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("No affordable debt Lumpsum plans found for duration={}m, amount={}. Reallocating to equity.",
                    durationMonths, debtAmount);
        }

        // ── Build equity ──
        List<RecommendedLumpsumSchemeDto> equitySchemes = Collections.emptyList();

        if (effectiveEquityAmount.compareTo(BigDecimal.ZERO) > 0 && schemeCount.equityCount() > 0) {
            equitySchemes = buildLumpsumSchemes(
                    byAsset.getOrDefault(1, List.of()),
                    schemeCount.equityCount(),
                    effectiveEquityAmount
            );
        }

        log.info("Lumpsum result — equity schemes: {}, debt schemes: {}",
                equitySchemes.size(), debtSchemes.size());

        List<RecommendedLumpsumSchemeDto> result = new ArrayList<>();
        result.addAll(equitySchemes);
        result.addAll(debtSchemes);
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE BUILDERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds SIP scheme list from a pre-scored, pre-ordered candidate pool.
     *
     * Steps:
     *  1. Candidates are already sorted by score DESC (DB did this via DISTINCT ON).
     *  2. Filter: remove plans where min_sip is NULL or amount-per-scheme < min_sip.
     *     per-scheme amount = total amount ÷ maxSchemes (tentative split).
     *  3. Limit to maxSchemes.
     *  4. Recompute per-scheme amount using actual count (may be < maxSchemes if
     *     some were filtered). Amount is split equally across winners.
     *
     * Why filter AFTER taking top N by score?
     *  Score already ranked the best plans. We just remove the ones the user
     *  literally cannot afford. No re-sorting needed.
     */
    private List<RecommendedSipSchemeDto> buildSipSchemes(
            List<TopSipPlanProjection> candidates,
            int maxSchemes,
            int durationMonths,
            BigDecimal totalAmount
    ) {
        if (candidates.isEmpty() || maxSchemes <= 0) return Collections.emptyList();

        // Tentative per-scheme amount to use for the min_sip affordability check
        BigDecimal tentativePerScheme = totalAmount
                .divide(BigDecimal.valueOf(maxSchemes), 0, RoundingMode.HALF_UP);

        // Candidates are already score-sorted by DB.
        // Just filter by affordability and cap count.
        List<TopSipPlanProjection> affordable = candidates.stream()
                .sorted(Comparator.comparing(TopSipPlanProjection::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .filter(p -> {
                    BigDecimal minSip = p.getMinSipAmount();
                    // Accept plans with no min_sip constraint (treat as ₹0 minimum)
                    if (minSip == null) return true;
                    return tentativePerScheme.compareTo(minSip) >= 0;
                })
                .limit(maxSchemes)
                .toList();

        if (affordable.isEmpty()) {
            log.warn("buildSipSchemes: 0 affordable plans from {} candidates (tentativePerScheme={})",
                    candidates.size(), tentativePerScheme);
            return Collections.emptyList();
        }

        // Final per-scheme amount: split equally across whoever survived
        BigDecimal perScheme = totalAmount
                .divide(BigDecimal.valueOf(affordable.size()), 0, RoundingMode.HALF_UP);

        int sipDay = resolveSipDay();

        List<RecommendedSipSchemeDto> result = affordable.stream()
                .map(p -> new RecommendedSipSchemeDto(
                        p.getPlanId(),
                        p.getPlanName(),
                        sipDay,
                        durationMonths,
                        perScheme
                ))
                .toList();

        log.debug("buildSipSchemes: {} schemes selected, ₹{} each, sipDay={}",
                result.size(), perScheme, sipDay);

        return result;
    }

    /**
     * Builds Lumpsum scheme list from a pre-scored, pre-ordered candidate pool.
     *
     * Steps:
     *  1. Candidates are already sorted by score DESC (DB did this via DISTINCT ON).
     *  2. Compute tentative per-scheme amount = total ÷ maxSchemes.
     *  3. Filter: remove plans where min_investment > tentative per-scheme.
     *  4. Limit to maxSchemes.
     *  5. Recompute per-scheme using actual count of affordable plans.
     *
     * Note: We compute tentative per-scheme BEFORE filtering (unlike old code which
     * computed it after). This ensures the affordability check uses a realistic
     * per-plan amount — checking against the total would pass plans that fail
     * when the money is actually split.
     */
    private List<RecommendedLumpsumSchemeDto> buildLumpsumSchemes(
            List<TopLumpsumPlanProjection> candidates,
            int maxSchemes,
            BigDecimal totalAmount
    ) {
        if (candidates.isEmpty() || maxSchemes <= 0) return Collections.emptyList();

        // Tentative per-scheme amount for affordability check
        BigDecimal tentativePerScheme = totalAmount
                .divide(BigDecimal.valueOf(maxSchemes), 0, RoundingMode.HALF_UP);

        List<TopLumpsumPlanProjection> affordable = candidates.stream()
                .sorted(Comparator.comparing(TopLumpsumPlanProjection::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .filter(p -> {
                    BigDecimal minInvestment = p.getMinInvestment();
                    // Accept plans with no minimum constraint
                    if (minInvestment == null) return true;
                    return tentativePerScheme.compareTo(minInvestment) >= 0;
                })
                .limit(maxSchemes)
                .toList();

        if (affordable.isEmpty()) {
            log.warn("buildLumpsumSchemes: 0 affordable plans from {} candidates (tentativePerScheme={})",
                    candidates.size(), tentativePerScheme);
            return Collections.emptyList();
        }

        // Recompute with actual winner count
        BigDecimal perScheme = totalAmount
                .divide(BigDecimal.valueOf(affordable.size()), 0, RoundingMode.HALF_UP);

        List<RecommendedLumpsumSchemeDto> result = affordable.stream()
                .map(p -> new RecommendedLumpsumSchemeDto(
                        p.getPlanId(),
                        p.getPlanName(),
                        perScheme
                ))
                .toList();

        log.debug("buildLumpsumSchemes: {} schemes selected, ₹{} each",
                result.size(), perScheme);

        return result;
    }

    // HELPERS

    /**
     * SIP Day Rule:
     *  - Day 1–28  → use today's date (safe for all months)
     *  - Day 29–31 → use 1st (Feb and short months don't have these dates)
     *
     * Called at request time, NOT at startup — avoids the static field bug
     * where the day was frozen to the server start date.
     */
    private int resolveSipDay() {
        int tomorrow = OffsetDateTime.now().plusDays(1).getDayOfMonth();
        return (tomorrow <= 28) ? tomorrow : 1;
    }
}

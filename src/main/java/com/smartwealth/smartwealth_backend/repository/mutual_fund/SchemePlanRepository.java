package com.smartwealth.smartwealth_backend.repository.mutual_fund;

import com.smartwealth.smartwealth_backend.entity.mutual_fund.SchemePlan;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SchemePlanRepository extends JpaRepository<SchemePlan, Integer> {

    boolean existsByPlanId(Integer planId);

    @Query("""
        SELECT p.isin AS isin,
               p.planId AS planId
        FROM SchemePlan p
        WHERE p.isin IS NOT NULL
    """)
    List<IsinAndPlanIdProjection> findIsinAndPlanIdMap();

    @Query(value = """
        SELECT
            p.plan_id        AS planId,
            p.scheme_id      AS schemeId,
            p.plan_type      AS planType,
            opt.option_name  AS optionType
        FROM scheme_plans p
        JOIN plan_options_type opt ON opt.option_id = p.option_type_id
        WHERE p.scheme_id IN (:schemeIds)
        ORDER BY p.plan_type, opt.option_name
        """, nativeQuery = true)
    List<PlanProjection> findPlansBySchemeIds(@Param("schemeIds") List<Integer> schemeIds);

    @Query(value = """
        SELECT
            p.plan_id       AS planId,
            p.plan_name     AS planName,
            p.scheme_id     AS schemeId,
            p.plan_type     AS planType,
            opt.option_name AS optionType
        FROM scheme_plans p
        JOIN plan_options_type opt ON opt.option_id = p.option_type_id
        WHERE p.scheme_id IN (:schemeIds)
          AND (:optionTypeId IS NULL OR p.option_type_id = :optionTypeId)
        ORDER BY p.plan_type, opt.option_name
        """, nativeQuery = true)
    List<PlanProjection> findPlansBySchemeIdsAndOptionType(
            @Param("schemeIds") List<Integer> schemeIds,
            @Param("optionTypeId") Integer optionTypeId
    );

    @Query(value = """
        SELECT
            p.plan_id               AS planId,
            p.plan_name             AS planName,
            p.plan_type             AS planType,
            opt.option_name         AS optionType,
            p.is_recommended        AS isRecommended,
            s.scheme_id             AS schemeId,
            s.scheme_name           AS schemeName,
            a.amc_name              AS amcName,
            ast.asset_name          AS assetName,
            c.category_short_name   AS categoryName,
            a.website               AS website,
            p.expense_ratio         AS expenseRatio,
            p.min_investment        AS minInvestment,
            p.min_sip               AS minSip,
            p.is_sip_allowed        AS isSipAllowed,
            p.exit_load             AS exitLoad,
            p.return_1y             AS return1y,
            p.return_3y             AS return3y,
            p.return_5y             AS return5y,
            b.benchmark_id          AS benchmarkId,
            b.benchmark_name        AS benchmarkName
        FROM scheme_plans p
        JOIN plan_options_type opt  ON opt.option_id = p.option_type_id
        JOIN mutual_fund_schemes s  ON s.scheme_id = p.scheme_id
        JOIN amc a                  ON a.amc_id = s.amc_id
        JOIN asset_master ast       ON ast.asset_id = s.asset_id
        JOIN fund_categories c      ON c.category_id = s.category_id
        LEFT JOIN benchmark_master b ON b.benchmark_id = s.benchmark_id
        WHERE p.plan_id = :planId
        """, nativeQuery = true)
    Optional<PlanDetailProjection> findPlanDetailById(@Param("planId") Integer planId);

    @Query("""
        SELECT p.minSip AS minSip, p.isSipAllowed AS isSipAllowed
        FROM SchemePlan p
        WHERE p.planId = :planId
    """)
    Optional<PlanSipConfigProjection> findSipConfigByPlanId(@Param("planId") Integer planId);

    @Query("""
        SELECT p.minInvestment
        FROM SchemePlan p
        WHERE p.planId = :planId
    """)
    Optional<BigDecimal> findMinLumpsumAmountByPlanId(@Param("planId") Integer planId);

    @Query("""
        SELECT s.categoryId
        FROM SchemePlan sp
        JOIN MutualFundScheme s ON sp.schemeId = s.schemeId
        WHERE sp.planId = :planId
    """)
    Optional<Integer> findCategoryIdByPlanId(@Param("planId") Integer planId);

    // RECOMMENDATION QUERIES

    /**
     * Returns the best-scored plan per (asset × category) bucket for SIP.
     *
     * Design decisions:
     *  - DISTINCT ON (asset_id, category_id): one winner per category bucket.
     *    The ORDER BY drives which plan wins — highest score within the bucket.
     *  - No is_recommended filter: score already encodes quality (returns − expense).
     *    is_recommended was causing zero-result debt pools for niche categories.
     *    after grouping. Filtering in SQL would silently drop entire asset classes.
     *  - score IS NOT NULL: only plans that have gone through nightly recalculation.
     *    Plans without NAV history never get a score and are safely excluded.
     *  - option_type_id = 1: Direct Growth plans only — lowest cost option.
     *  - Duration filter via fund_categories: category declares its own suitability
     *    window (min_months..max_months). Clean separation of concerns.
     *
     * Returns ALL eligible plans across both asset classes (equity + debt).
     * Java groups them by asset_id and applies amount-based affordability filtering.
     */
    @Query(value = """
        SELECT DISTINCT ON (s.asset_id, s.category_id)
            p.plan_id      AS planId,
            p.plan_name    AS planName,
            s.asset_id     AS assetId,
            p.min_sip      AS minSipAmount,
            p.score         AS score,
            p.return_1y AS return1y,
            p.return_3y AS return3y,
            p.return_5y AS    return5y
        FROM scheme_plans p
        JOIN mutual_fund_schemes s ON s.scheme_id = p.scheme_id
        JOIN fund_categories c     ON c.category_id = s.category_id
        JOIN category_risk_mapping crm on crm.risk_id = :riskId and crm.category_id = c.category_id
        WHERE s.asset_id     IN (1, 2)
          AND c.is_active     = true
          AND :durationMonths BETWEEN c.min_months AND c.max_months
          AND p.option_type_id = 1
          AND p.score          IS NOT NULL
          AND p.return_1y      IS NOT NULL
          AND p.is_sip_allowed  = true
        ORDER BY s.asset_id, s.category_id, p.score DESC
    """, nativeQuery = true)
    List<TopSipPlanProjection> findAllTopSipPlans(@Param("durationMonths") Integer durationMonths, @Param("riskId") Integer riskId);

    /**
     * Returns the best-scored plan per (asset × category) bucket for Lumpsum.
     *
     * Same design decisions as findAllTopSipPlans.
     * min_investment affordability check is done in Java after grouping.
     */
    @Query(value = """
        SELECT DISTINCT ON (s.asset_id, s.category_id)
            p.plan_id        AS planId,
            p.plan_name      AS planName,
            s.asset_id       AS assetId,
            p.min_investment AS minInvestment,
            p.score         AS score
        FROM scheme_plans p
        JOIN mutual_fund_schemes s ON s.scheme_id = p.scheme_id
        JOIN fund_categories c     ON c.category_id = s.category_id
        JOIN category_risk_mapping crm on crm.risk_id = :riskId and crm.category_id = c.category_id
        WHERE s.asset_id     IN (1, 2)
          AND c.is_active     = true
          AND :durationMonths BETWEEN c.min_months AND c.max_months
          AND p.option_type_id = 1
          AND p.score          IS NOT NULL
          AND p.return_1y      IS NOT NULL
        ORDER BY s.asset_id, s.category_id, p.score DESC
    """, nativeQuery = true)
    List<TopLumpsumPlanProjection> findAllTopLumpsumPlans(@Param("durationMonths") Integer durationMonths, @Param("riskId") Integer riskId);

    // NIGHTLY RECALCULATION  —  Step 2 + Step 3

    /**
     * Step 2 — Recalculates return_1y / return_3y / return_5y from nav_anchors.
     *
     * Formulas:
     *   return_1y = simple % change over 1 year
     *   return_3y = CAGR over 3 years  →  (nav_today / nav_3y_ago)^(1/3) - 1
     *   return_5y = CAGR over 5 years  →  (nav_today / nav_5y_ago)^(1/5) - 1
     *
     * ELSE keeps the existing seeded value for plans not yet in nav_anchors.
     */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE scheme_plans sp
        SET
            return_1y = CASE
                WHEN a.nav_1y_ago IS NOT NULL AND a.nav_1y_ago > 0
                THEN ROUND(((a.nav_today - a.nav_1y_ago) / a.nav_1y_ago) * 100, 4)
                ELSE sp.return_1y
            END,
            return_3y = CASE
                WHEN a.nav_3y_ago IS NOT NULL AND a.nav_3y_ago > 0
                THEN ROUND((POWER(a.nav_today / a.nav_3y_ago, 1.0 / 3) - 1) * 100, 4)
                ELSE sp.return_3y
            END,
            return_5y = CASE
                WHEN a.nav_5y_ago IS NOT NULL AND a.nav_5y_ago > 0
                THEN ROUND((POWER(a.nav_today / a.nav_5y_ago, 1.0 / 5) - 1) * 100, 4)
                ELSE sp.return_5y
            END,
            updated_at = now()
        FROM nav_anchors a
        WHERE sp.plan_id = a.plan_id
    """, nativeQuery = true)
    void recalculateReturnsFromAnchors();

    /**
     * Step 3 — Recalculates composite score from returns + expense_ratio.
     *
     * Formula (all 3 return periods available):
     *   score = (return_5y × 0.50) + (return_3y × 0.30) + (return_1y × 0.20)
     *           − (expense_ratio × 2)
     *
     * Graceful degradation for newer funds with fewer return periods:
     *   Only 1y + 3y available → (return_3y × 0.60) + (return_1y × 0.40) − penalty
     *   Only 1y available      → return_1y − penalty
     *
     * Expense ratio × 2 = a 1% cost difference = 2 score point penalty.
     * This is significant but won't fully override a genuinely better-returning fund.
     */
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE scheme_plans sp
        SET score = CASE
            WHEN sp.return_1y IS NOT NULL
             AND sp.return_3y IS NOT NULL
             AND sp.return_5y IS NOT NULL
            THEN ROUND(
                (sp.return_5y * 0.50)
              + (sp.return_3y * 0.30)
              + (sp.return_1y * 0.20)
              - (COALESCE(sp.expense_ratio, 0) * 2),
              4)
            WHEN sp.return_1y IS NOT NULL
             AND sp.return_3y IS NOT NULL
            THEN ROUND(
                (sp.return_3y * 0.60)
              + (sp.return_1y * 0.40)
              - (COALESCE(sp.expense_ratio, 0) * 2),
              4)
            WHEN sp.return_1y IS NOT NULL
            THEN ROUND(
                sp.return_1y
              - (COALESCE(sp.expense_ratio, 0) * 2),
              4)
            ELSE sp.score
        END,
        updated_at = now()
    """, nativeQuery = true)
    void recalculateAllScores();
}

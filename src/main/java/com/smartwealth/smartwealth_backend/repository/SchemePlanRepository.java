package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.SchemePlan;
import com.smartwealth.smartwealth_backend.repository.projection.IsinAndPlanIdProjection;
import com.smartwealth.smartwealth_backend.repository.projection.PlanDetailProjection;
import com.smartwealth.smartwealth_backend.repository.projection.PlanProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(
            value = """
        SELECT
            p.plan_id        AS planId,
            p.scheme_id      AS schemeId,
            p.plan_type      AS planType,
            opt.option_name  AS optionType
        FROM scheme_plans p
        JOIN plan_options_type opt
            ON opt.option_id = p.option_type_id
        WHERE p.scheme_id IN (:schemeIds)
        ORDER BY p.plan_type, opt.option_name
        """,
            nativeQuery = true
    )
    List<PlanProjection> findPlansBySchemeIds(
            @Param("schemeIds") List<Integer> schemeIds
    );

    @Query(
            value = """
        SELECT
            p.plan_id        AS planId,
            p.scheme_id      AS schemeId,
            p.plan_type      AS planType,
            opt.option_name  AS optionType
        FROM scheme_plans p
        JOIN plan_options_type opt
            ON opt.option_id = p.option_type_id
        WHERE p.scheme_id IN (:schemeIds)
          AND (:optionTypeId IS NULL OR p.option_type_id = :optionTypeId)
        ORDER BY p.plan_type, opt.option_name
        """,
            nativeQuery = true
    )
    List<PlanProjection> findPlansBySchemeIdsAndOptionType(
            @Param("schemeIds") List<Integer> schemeIds,
            @Param("optionTypeId") Integer optionTypeId
    );

    @Query(
            value = """
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

            p.expense_ratio         AS expenseRatio,
            p.min_investment        AS minInvestment,
            p.min_sip               AS minSip,
            p.exit_load             AS exitLoad,

            p.return_1y             AS return1y,
            p.return_3y             AS return3y,
            p.return_5y             AS return5y,

            b.benchmark_id          AS benchmarkId,
            b.benchmark_name        AS benchmarkName

        FROM scheme_plans p

        JOIN plan_options_type opt
            ON opt.option_id = p.option_type_id

        JOIN mutual_fund_schemes s
            ON s.scheme_id = p.scheme_id

        JOIN amc a
            ON a.amc_id = s.amc_id

        JOIN asset_master ast
            ON ast.asset_id = s.asset_id

        JOIN fund_categories c
            ON c.category_id = s.category_id

        LEFT JOIN benchmark_master b
            ON b.benchmark_id = s.benchmark_id

        WHERE p.plan_id = :planId
        """,
            nativeQuery = true
    )
    Optional<PlanDetailProjection> findPlanDetailById(
            @Param("planId") Integer planId
    );

}

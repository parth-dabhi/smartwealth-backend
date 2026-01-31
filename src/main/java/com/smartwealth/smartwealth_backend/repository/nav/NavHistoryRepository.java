package com.smartwealth.smartwealth_backend.repository.nav;

import com.smartwealth.smartwealth_backend.entity.mutual_fund.NavHistory;
import com.smartwealth.smartwealth_backend.repository.nav.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NavHistoryRepository extends JpaRepository<NavHistory, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
        INSERT INTO nav_history (plan_id, nav_date, nav_value)
        VALUES (:planId, :navDate, :navValue)
        ON CONFLICT (plan_id, nav_date) DO NOTHING
        """,
            nativeQuery = true
    )
    int insertNav(
            @Param("planId") Integer planId,
            @Param("navDate") LocalDate navDate,
            @Param("navValue") BigDecimal navValue
    );

    @Modifying
    @Query(value = """
    INSERT INTO nav_history (plan_id, nav_date, nav_value)
    SELECT * FROM UNNEST(
        :planIds,
        :navDates,
        :navValues
    )
    ON CONFLICT (plan_id, nav_date) DO NOTHING
    """, nativeQuery = true)
    int bulkInsertNav(
            @Param("planIds") Integer[] planIds,
            @Param("navDates") LocalDate[] navDates,
            @Param("navValues") BigDecimal[] navValues
    );


    @Query(
            value = """
            SELECT
                nh.nav_date  AS navDate,
                nh.nav_value AS navValue
            FROM nav_history nh
            WHERE nh.plan_id = :planId
            ORDER BY nh.nav_date DESC
            LIMIT 1
        """,
            nativeQuery = true
    )
    Optional<LatestNavProjection> findLatestNavByPlanId(
            @Param("planId") Integer planId
    );

    @Query(
            value = """
        SELECT
            nh.nav_date  AS navDate,
            nh.nav_value AS navValue
        FROM nav_history nh
        WHERE nh.plan_id = :planId
        ORDER BY nh.nav_date DESC
        """,
            nativeQuery = true
    )
    Optional<List<NavHistoryProjection>> findAllNavByPlanId(
            @Param("planId") Integer planId
    );

    @Query(
            value = """
        SELECT
            nh.nav_date  AS navDate,
            nh.nav_value AS navValue
        FROM nav_history nh
        WHERE nh.plan_id = :planId
          AND nh.nav_date = :navDate
        """,
            nativeQuery = true
    )
    Optional<LatestNavProjection> findByPlanIdAndNavDate(
            Integer planId,
            LocalDate navDate
    );

    @Query(
            value = """
        SELECT DISTINCT ON (nh.plan_id)
               nh.plan_id   AS planId,
               nh.nav_value AS navValue
        FROM nav_history nh
        WHERE nh.plan_id IN (:planIds)
        ORDER BY nh.plan_id, nh.nav_date DESC
        """,
            nativeQuery = true
    )
    List<LatestNavProjectionWithPlanId> findLatestNavByPlanIds(
            @Param("planIds") Set<Integer> planIds
    );

    // For internal purpose Only
    @Query(
            value = """
        SELECT
            sp.plan_name AS planName,
            nh.nav_value AS navValue
        FROM nav_history nh
        JOIN scheme_plans sp ON nh.plan_id = sp.plan_id
        WHERE nh.nav_date = :navDate
        ORDER BY planName
        """,
            nativeQuery = true
    )
    List<PlanNavViewProjection> findPlanNavsByDate(
            @Param("navDate") LocalDate navDate
    );

    @Query(
            value = """
    SELECT
        nh.plan_id   AS planId,
        nh.nav_value AS navValue,
        nh.nav_date  AS navDate
    FROM nav_history nh
    WHERE nh.nav_date = :navDate
      AND nh.plan_id IN (:planIds)
    """,
            nativeQuery = true
    )
    List<PlanNavProjection> findNavsByDateAndPlanIds(
            @Param("navDate") LocalDate navDate,
            @Param("planIds") Set<Integer> planIds
    );
}

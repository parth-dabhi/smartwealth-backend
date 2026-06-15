package com.smartwealth.smartwealth_backend.repository.nav;

import com.smartwealth.smartwealth_backend.entity.mutual_fund.NavHistory;
import com.smartwealth.smartwealth_backend.repository.nav.projection.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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

    // NAV ANCHORS  —  used for nightly return + score recalculation
    /**
     * Step 1 of nightly job.
     *
     * Refreshes nav_anchors table — one row per plan — with 4 anchor NAV values:
     *   nav_today  : most recent NAV in nav_history
     *   nav_1y_ago : closest NAV on or before (today - 1 year)
     *   nav_3y_ago : closest NAV on or before (today - 3 years)
     *   nav_5y_ago : closest NAV on or before (today - 5 years)
     *
     * Uses LATERAL joins so each subquery runs once per plan — much faster
     * than a correlated subquery in a SET clause.
     *
     * ON CONFLICT DO UPDATE means this is safe to run repeatedly.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO nav_anchors (plan_id, nav_today, nav_1y_ago, nav_3y_ago, nav_5y_ago, anchored_on)
        SELECT
            p.plan_id,
            n0.nav_value   AS nav_today,
            n1.nav_value   AS nav_1y_ago,
            n3.nav_value   AS nav_3y_ago,
            n5.nav_value   AS nav_5y_ago,
            :latestNavDate AS anchored_on
        FROM scheme_plans p
 
        -- NAV on exactly the latest real trading day
        LEFT JOIN LATERAL (
            SELECT nav_value FROM nav_history
            WHERE plan_id = p.plan_id
              AND nav_date = :latestNavDate
            LIMIT 1
        ) n0 ON true
 
        -- closest NAV on/before (latestNavDate - 1 year)
        LEFT JOIN LATERAL (
            SELECT nav_value FROM nav_history
            WHERE plan_id = p.plan_id
              AND nav_date <= CAST(:latestNavDate AS date) - INTERVAL '1 year'
            ORDER BY nav_date DESC
            LIMIT 1
        ) n1 ON true
 
        -- closest NAV on/before (latestNavDate - 3 years)
        LEFT JOIN LATERAL (
            SELECT nav_value FROM nav_history
            WHERE plan_id = p.plan_id
              AND nav_date <= CAST(:latestNavDate AS date) - INTERVAL '3 years'
            ORDER BY nav_date DESC
            LIMIT 1
        ) n3 ON true
 
        -- closest NAV on/before (latestNavDate - 5 years)
        LEFT JOIN LATERAL (
            SELECT nav_value FROM nav_history
            WHERE plan_id = p.plan_id
              AND nav_date <= CAST(:latestNavDate AS date) - INTERVAL '5 years'
            ORDER BY nav_date DESC
            LIMIT 1
        ) n5 ON true
        ON CONFLICT (plan_id) DO UPDATE SET
            nav_today   = EXCLUDED.nav_today,
            nav_1y_ago  = EXCLUDED.nav_1y_ago,
            nav_3y_ago  = EXCLUDED.nav_3y_ago,
            nav_5y_ago  = EXCLUDED.nav_5y_ago,
            anchored_on = EXCLUDED.anchored_on
    """, nativeQuery = true)
    void refreshNavAnchors(@Param("latestNavDate") LocalDate latestNavDate);
}

package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.NavHistory;
import com.smartwealth.smartwealth_backend.repository.projection.NavHistoryProjection;
import com.smartwealth.smartwealth_backend.repository.projection.NavLatestProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    Optional<NavLatestProjection> findLatestNavByPlanId(
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
}

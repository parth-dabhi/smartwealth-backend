package com.smartwealth.smartwealth_backend.repository.holding;

import com.smartwealth.smartwealth_backend.entity.holding.UserHolding;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingPortfolioProjection;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserHoldingRepository extends JpaRepository<UserHolding, Long> {

    <T> Optional<T> findByHoldingId(
            Long holdingId,
            Class<T> type
    );

    @Query(
            value = """
            SELECT
                h.holding_id                     AS holdingId,
                h.folio_number                   AS folioNumber,
                h.plan_id                        AS planId,
                h.total_units                    AS totalUnits,
                h.total_invested_amount          AS totalInvestedAmount,
                h.total_redeemed_amount          AS totalRedeemedAmount,
                h.is_active                      AS isActive,

                sp.plan_name                     AS planName,
                amc.amc_name                     AS amcName,
                asset.asset_name                 AS assetName,
                cat.category_short_name          AS categoryName

            FROM user_holdings h
            JOIN scheme_plans sp
                ON sp.plan_id = h.plan_id
            JOIN mutual_fund_schemes s
                ON s.scheme_id = sp.scheme_id
            JOIN amc
                ON amc.amc_id = s.amc_id
            JOIN asset_master asset
                ON asset.asset_id = s.asset_id
            JOIN fund_categories cat
                ON cat.category_id = s.category_id

            WHERE h.user_id = :userId
        """,
            nativeQuery = true
    )
    Optional<List<UserHoldingPortfolioProjection>> findPortfolioByUserId(Long userId);

    @Query(
            value = """
            SELECT
                h.holding_id                     AS holdingId,
                h.folio_number                  AS folioNumber,
                h.plan_id                        AS planId,
                h.total_units                    AS totalUnits,
                h.total_invested_amount          AS totalInvestedAmount,
                h.total_redeemed_amount          AS totalRedeemedAmount,
                h.is_active                      AS isActive,

                sp.plan_name                     AS planName,
                amc.amc_name                     AS amcName,
                asset.asset_name                 AS assetName,
                cat.category_short_name          AS categoryName

            FROM user_holdings h
            JOIN scheme_plans sp
                ON sp.plan_id = h.plan_id
            JOIN mutual_fund_schemes s
                ON s.scheme_id = sp.scheme_id
            JOIN amc
                ON amc.amc_id = s.amc_id
            JOIN asset_master asset
                ON asset.asset_id = s.asset_id
            JOIN fund_categories cat
                ON cat.category_id = s.category_id

            WHERE h.user_id = :userId
              AND h.folio_number = :folioNumber
        """,
            nativeQuery = true
    )
    Optional<UserHoldingPortfolioProjection> findByUserIdAndFolioNumber(
            Long userId,
            String folioNumber
    );

    @Query(
            value = """
            SELECT
                h.holding_id                     AS holdingId,
                h.folio_number                 AS folioNumber,
                h.plan_id                        AS planId,
                h.total_units                    AS totalUnits,
                h.total_invested_amount          AS totalInvestedAmount,
                h.total_redeemed_amount          AS totalRedeemedAmount,
                h.is_active                      AS isActive,

                sp.plan_name                     AS planName,
                amc.amc_name                     AS amcName,
                asset.asset_name                 AS assetName,
                cat.category_short_name          AS categoryName

            FROM user_holdings h
            JOIN scheme_plans sp
                ON sp.plan_id = h.plan_id
            JOIN mutual_fund_schemes s
                ON s.scheme_id = sp.scheme_id
            JOIN amc
                ON amc.amc_id = s.amc_id
            JOIN asset_master asset
                ON asset.asset_id = s.asset_id
            JOIN fund_categories cat
                ON cat.category_id = s.category_id

            WHERE h.user_id = :userId
              AND h.holding_id = :holdingId
        """,
            nativeQuery = true
    )
    Optional<UserHoldingPortfolioProjection> findByUserIdAndHoldingId(
            Long userId,
            Long holdingId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserHolding h
            SET h.totalUnits = h.totalUnits + :units,
                h.totalInvestedAmount = h.totalInvestedAmount + :amount,
                h.updatedAt = :time
            WHERE h.holdingId = :holdingId
                AND h.userId = :userId
                AND h.planId = :planId
            """)
    int updateUserHoldingUnitsForBuy(
            @Param("holdingId") Long holdingId,
            @Param("userId") Long userId,
            @Param("planId") Integer planId,
            @Param("units") BigDecimal units,
            @Param("amount") BigDecimal amount,
            @Param("time") OffsetDateTime time
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE UserHolding h
    SET h.totalUnits = h.totalUnits - :units,
        h.totalRedeemedAmount = h.totalRedeemedAmount + :redeemedAmount,
        h.updatedAt = :time
    WHERE h.holdingId = :holdingId
""")
    int updateUserHoldingUnitsForSell(
            @Param("holdingId") Long holdingId,
            @Param("units") BigDecimal units,
            @Param("redeemedAmount") BigDecimal redeemedAmount,
            @Param("time") OffsetDateTime time
    );

    @Query("""
    SELECT h.folioNumber
    FROM UserHolding h
    WHERE h.userId = :userId
      AND h.planId = :planId
""")
    List<String> findFoliosByUserAndPlan(
            @Param("userId") Long userId,
            @Param("planId") Integer planId
    );

    @Query("""
    SELECT h.holdingId as holdingId,
    h.planId as planId
    FROM UserHolding h
    WHERE h.folioNumber = :folioNumber
      AND h.userId = :userId
""")
    Optional<UserHoldingProjection> findHoldingByFolioNumber(
            @Param("folioNumber") String folioNumber,
            @Param("userId") Long userId
    );

    @Query("""
    SELECT h.holdingId as holdingId
    FROM UserHolding h
    WHERE h.folioNumber = :folioNumber
      AND h.userId = :userId
""")
    Optional<Long> findHoldingIdByFolioNumber(
            @Param("folioNumber") String folioNumber,
            @Param("userId") Long userId
    );
}

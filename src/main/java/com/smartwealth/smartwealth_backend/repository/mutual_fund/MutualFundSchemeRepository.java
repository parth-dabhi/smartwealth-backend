package com.smartwealth.smartwealth_backend.repository.mutual_fund;

import com.smartwealth.smartwealth_backend.entity.mutual_fund.MutualFundScheme;
import com.smartwealth.smartwealth_backend.repository.mutual_fund.projection.SchemeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MutualFundSchemeRepository extends JpaRepository<MutualFundScheme, Integer> {
    @Query(value = """
            SELECT DISTINCT
                s.scheme_id           AS schemeId,
                s.scheme_name         AS schemeName,
                a.amc_name            AS amcName,
                ast.asset_name        AS assetName,
                c.category_short_name AS categoryName
            FROM mutual_fund_schemes s
            JOIN amc a
                ON a.amc_id = s.amc_id
            JOIN asset_master ast
                ON ast.asset_id = s.asset_id
            JOIN fund_categories c
                ON c.category_id = s.category_id
            JOIN scheme_plans p
                ON p.scheme_id = s.scheme_id
            WHERE (:amcId IS NULL OR s.amc_id = :amcId)
              AND (:assetId IS NULL OR s.asset_id = :assetId)
              AND (:categoryId IS NULL OR s.category_id = :categoryId)
              AND (:optionTypeId IS NULL OR p.option_type_id = :optionTypeId)
              AND (:search IS NULL
                   OR LOWER(s.scheme_name) LIKE LOWER(CONCAT('%', :search, '%')))
            """, countQuery = """
            SELECT COUNT(DISTINCT s.scheme_id)
            FROM mutual_fund_schemes s
            JOIN scheme_plans p
                ON p.scheme_id = s.scheme_id
            WHERE (:amcId IS NULL OR s.amc_id = :amcId)
              AND (:assetId IS NULL OR s.asset_id = :assetId)
              AND (:categoryId IS NULL OR s.category_id = :categoryId)
              AND (:optionTypeId IS NULL OR p.option_type_id = :optionTypeId)
              AND (:search IS NULL
                   OR LOWER(s.scheme_name) LIKE LOWER(CONCAT('%', :search, '%')))
            """, nativeQuery = true)
    Page<SchemeProjection> findSchemes(
            @Param("amcId") Integer amcId,
            @Param("assetId") Integer assetId,
            @Param("categoryId") Integer categoryId,
            @Param("optionTypeId") Integer optionTypeId,
            @Param("search") String search,
            Pageable pageable
    );
}

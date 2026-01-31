package com.smartwealth.smartwealth_backend.repository.mutual_fund;

import com.smartwealth.smartwealth_backend.dto.common.CategoryFilterOption;
import com.smartwealth.smartwealth_backend.dto.common.FilterOption;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FilterLookupDao {

    private final JdbcTemplate jdbcTemplate;

    public List<FilterOption> findAllAmcs() {
        return jdbcTemplate.query(
                """
                SELECT amc_id AS value, amc_name AS label
                FROM amc
                ORDER BY amc_name
                """,
                (rs, rowNum) ->
                        new FilterOption(
                                rs.getString("label"),
                                rs.getInt("value")
                        )
        );
    }

    public List<FilterOption> findAllAssets() {
        return jdbcTemplate.query(
                """
                SELECT asset_id AS value, asset_name AS label
                FROM asset_master
                ORDER BY asset_id
                """,
                (rs, rowNum) ->
                        new FilterOption(
                                rs.getString("label"),
                                rs.getInt("value")
                        )
        );
    }

    public List<CategoryFilterOption> findAllCategories() {
        return jdbcTemplate.query("""
                SELECT
                    category_id AS value,
                    category_short_name AS label,
                    SPLIT_PART(category_name, ':', 1) AS group_name
                FROM fund_categories
                WHERE is_active = true
                ORDER BY asset_id, category_id
                """, (rs, rowNum) -> new CategoryFilterOption(rs.getString("group_name"), rs.getString("label"), rs.getInt("value"))
        );
    }


    public List<FilterOption> findOptionTypes() {
        return jdbcTemplate.query(
                """
                SELECT option_id AS value,
                       option_name AS label
                FROM plan_options_type
                ORDER BY option_id
                """,
                (rs, rowNum) ->
                        new FilterOption(
                                rs.getString("label"),
                                rs.getInt("value")
                        )
        );
    }
}

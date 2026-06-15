package com.smartwealth.smartwealth_backend.repository.user;

import com.smartwealth.smartwealth_backend.dto.common.RiskProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RiskProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public List<RiskProfileDto> findAll() {

        String sql = """
            SELECT risk_id,
                risk_profile_name,
                risk_level,
                equity_percent,
                debt_percent,
                hybrid_percent,
                commodities_percent
            FROM risk_profiles
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                RiskProfileDto.builder()
                        .id(rs.getInt("risk_id"))
                        .name(rs.getString("risk_profile_name"))
                        .level(rs.getInt("risk_level"))
                        .equityPercent(rs.getBigDecimal("equity_percent"))
                        .debtPercent(rs.getBigDecimal("debt_percent"))
                        .hybridPercent(rs.getBigDecimal("hybrid_percent"))
                        .commoditiesPercent(rs.getBigDecimal("commodities_percent"))
                        .build()
        );
    }
}

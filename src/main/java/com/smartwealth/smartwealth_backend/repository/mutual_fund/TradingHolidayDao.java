package com.smartwealth.smartwealth_backend.repository.mutual_fund;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TradingHolidayDao {

    private final JdbcTemplate jdbcTemplate;

    public boolean existsByDate(LocalDate date) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM trading_holidays
                WHERE holiday_date = ?
            )
        """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, date)
        );
    }

    public int saveAll(List<LocalDate> holidays) {

        String sql = """
        INSERT INTO trading_holidays (holiday_date)
        VALUES (?)
        ON CONFLICT (holiday_date) DO NOTHING
    """;

        int[][] results = jdbcTemplate.batchUpdate(
                sql,
                holidays,
                holidays.size(),
                (ps, date) -> ps.setObject(1, date)
        );

        int insertedCount = 0;

        for (int[] batch : results) {
            for (int r : batch) {
                if (r > 0) {
                    insertedCount++;
                }
            }
        }

        return insertedCount;
    }
}


package com.smartwealth.smartwealth_backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerIdGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    public String generateCustomerId() {
        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('customer_id_seq')", Long.class);

        // 8-digit formatting
        return String.format("%08d", nextVal);
    }
}

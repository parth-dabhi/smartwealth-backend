package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.service.CustomerIdGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerIdGeneratorServiceImpl implements CustomerIdGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String generateCustomerId() {
        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('customer_id_seq')", Long.class);

        // 8-digit formatting
        String id = String.format("%08d", nextVal);

        log.info("Generated Customer ID: {}", id);

        return id;
    }
}

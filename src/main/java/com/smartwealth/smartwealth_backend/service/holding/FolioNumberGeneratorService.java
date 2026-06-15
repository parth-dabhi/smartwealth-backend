package com.smartwealth.smartwealth_backend.service.holding;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolioNumberGeneratorService {

    private final JdbcTemplate jdbcTemplate;

    public String generateFolioNumber() {
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT nextval('folio_number_seq')", Long.class
        );
        return String.format("SW-F-%08d", nextVal);
    }
}

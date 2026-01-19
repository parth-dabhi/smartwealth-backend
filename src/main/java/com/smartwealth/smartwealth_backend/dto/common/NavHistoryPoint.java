package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class NavHistoryPoint {

    private LocalDate date;
    private BigDecimal value;
}


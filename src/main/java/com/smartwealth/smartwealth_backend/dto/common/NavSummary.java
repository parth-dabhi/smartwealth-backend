package com.smartwealth.smartwealth_backend.dto.common;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class NavSummary {

    private LocalDate latestDate;
    private BigDecimal latestValue;
    private String historyLink;
}

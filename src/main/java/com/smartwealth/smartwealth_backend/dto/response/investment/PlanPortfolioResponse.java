package com.smartwealth.smartwealth_backend.dto.response.investment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanPortfolioResponse {

    private Integer planId;
    private String planName;

    private String amcName;
    private String assetName;
    private String categoryName;

    private BigDecimal investedAmount;
    private BigDecimal marketValue;
    private BigDecimal gain;
    private BigDecimal units;

    private BigDecimal latestNav;
    private LocalDate navDate;

    private Boolean isActive;
}


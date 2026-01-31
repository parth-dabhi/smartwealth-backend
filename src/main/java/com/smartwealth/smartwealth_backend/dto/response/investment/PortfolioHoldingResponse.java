package com.smartwealth.smartwealth_backend.dto.response.investment;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioHoldingResponse {

    private Integer planId;
    private String planName;

    private String amcName;
    private String assetName;
    private String categoryName;

    private BigDecimal netInvestedAmount;
    private BigDecimal marketValue;
    private BigDecimal gain;
    private String gainPercentage;
}


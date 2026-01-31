package com.smartwealth.smartwealth_backend.dto.response.investment;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetAllocationResponse {

    private String assetName;   // Equity, Debt, Hybrid, Commodities
    private BigDecimal marketValue;
    private String percentage;  // "79.89%"
}

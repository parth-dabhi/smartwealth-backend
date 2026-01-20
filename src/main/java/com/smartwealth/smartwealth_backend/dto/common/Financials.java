package com.smartwealth.smartwealth_backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Financials {

    private BigDecimal expenseRatio;
    private BigDecimal minInvestment;
    private BigDecimal minSip;
    private String exitLoad;
}


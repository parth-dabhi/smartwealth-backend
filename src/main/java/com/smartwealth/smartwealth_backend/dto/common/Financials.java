package com.smartwealth.smartwealth_backend.dto.common;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Financials {

    private BigDecimal expenseRatio;
    private BigDecimal minInvestment;
    private BigDecimal minSip;
    private String exitLoad;
}


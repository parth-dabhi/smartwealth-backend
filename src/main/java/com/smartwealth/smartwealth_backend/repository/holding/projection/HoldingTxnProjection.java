package com.smartwealth.smartwealth_backend.repository.holding.projection;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface HoldingTxnProjection {

    HoldingTxnType getTxnType();   // BUY / SELL
    InvestmentMode getInvestmentMode();
    BigDecimal getUnits();
    BigDecimal getAmount();
    BigDecimal getNav();
    LocalDate getNavDate();
    LocalDate getTransactionDate();
}


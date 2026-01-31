package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;

import java.math.BigDecimal;

public interface HoldingTransactionService {
    void createHoldingTransactionRecord(
            Long holdingId,
            Long investmentOrderId,
            BigDecimal units,
            BigDecimal amount,
            HoldingTxnType txnType,
            InvestmentMode investmentMode,
            PlanNavProjection nav
    );
}

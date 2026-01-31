package com.smartwealth.smartwealth_backend.dto.response.investment;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentType;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingTransactionResponse {

    private HoldingTxnType type; // BUY / SELL
    private InvestmentMode investmentMode;
    private BigDecimal units;
    private BigDecimal nav;
    private LocalDate navDate;
    private BigDecimal amount;
    private LocalDate transactionDate;
}


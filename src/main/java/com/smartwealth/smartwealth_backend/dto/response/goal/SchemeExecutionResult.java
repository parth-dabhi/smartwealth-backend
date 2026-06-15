package com.smartwealth.smartwealth_backend.dto.response.goal;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemeExecutionResult {

    private Integer planId;
    private String schemeName;

    private Long sipMandateId;
    private Long investmentOrderId;

    private InvestmentMode type;   // SIP | LUMPSUM
    private TransactionStatus status; // SUCCESS | FAILED

    private BigDecimal sipAmount;
    private BigDecimal lumpsumAmount;

    private String error;  // null if success
}


package com.smartwealth.smartwealth_backend.dto.response.investment;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentOrderHistoryResponse {

    private Long investmentOrderId;
    private Integer planId;
    private String planName;

    private InvestmentType investmentType;   // BUY / SELL
    private InvestmentMode investmentMode; // SIP / LUMPSUM

    private BigDecimal amount;
    private BigDecimal units;

    private LocalDate applicableNavDate;
    private String status;

    private OffsetDateTime orderTime;
}

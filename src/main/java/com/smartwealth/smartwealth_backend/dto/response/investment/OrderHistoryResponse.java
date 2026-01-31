package com.smartwealth.smartwealth_backend.dto.response.investment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponse {

    private Long investmentOrderId;
    private String planName;

    private String investmentType;   // BUY | SELL
    private String investmentMode;   // SIP | LUMPSUM

    private String orderStatus;       // PENDING | ALLOTTED | FAILED
    private String paymentStatus;

    private BigDecimal units;         // NULL until allotted
    private BigDecimal amount;

    private BigDecimal nav;           // NULL until allotted
    private LocalDate navDate;        // NULL until allotted

    private OffsetDateTime orderTime;
}


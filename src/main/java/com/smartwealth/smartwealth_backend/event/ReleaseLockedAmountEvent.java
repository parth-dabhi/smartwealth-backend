package com.smartwealth.smartwealth_backend.event;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class ReleaseLockedAmountEvent {
    Long orderId;
    Long userId;
    Long sipMandateId;
    OffsetDateTime failedAt;
    BigDecimal amount;
    String idempotencyKeyPrefix;
    InvestmentMode investmentMode;
    Exception ex;
}

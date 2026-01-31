package com.smartwealth.smartwealth_backend.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RefundAmountEvent {
    Long userId;
    BigDecimal amount;
    Long orderId;
    String releaseKeyPrefix;
}

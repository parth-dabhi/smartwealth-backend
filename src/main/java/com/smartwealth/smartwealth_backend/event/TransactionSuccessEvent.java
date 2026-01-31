package com.smartwealth.smartwealth_backend.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TransactionSuccessEvent {
    private final Long transactionId;
    private final BigDecimal balanceAfter;
    private final String description;
    private final String idempotencyKey;
}

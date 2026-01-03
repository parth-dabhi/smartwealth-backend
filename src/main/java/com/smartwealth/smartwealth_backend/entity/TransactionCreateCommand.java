package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionCreateCommand(User user, Wallet wallet, BigDecimal amount, TransactionType transactionType,
                                       TransactionStatus status, String idempotencyKey, String referenceId,
                                       String description) {

}

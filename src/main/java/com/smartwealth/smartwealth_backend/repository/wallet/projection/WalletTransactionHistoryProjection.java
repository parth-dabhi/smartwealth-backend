package com.smartwealth.smartwealth_backend.repository.wallet.projection;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface WalletTransactionHistoryProjection {
    String getReferenceId();
    TransactionType getTransactionType();
    TransactionCategory getTransactionCategory();
    BigDecimal getAmount();
    TransactionStatus getStatus();
    BigDecimal getBalanceAfter();
    String getDescription();
    OffsetDateTime getCreatedAt();
}

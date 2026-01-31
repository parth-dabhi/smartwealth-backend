package com.smartwealth.smartwealth_backend.exception.wallet;

public enum WalletTransactionFailureType {
    INSUFFICIENT_BALANCE,
    LIMIT_EXCEEDED,
    WALLET_NOT_FOUND,
    WALLET_SUSPENDED,
    IDEMPOTENCY_KEY_EXPIRED,
    TRANSACTION_FAILED,
    DATABASE_ERROR,
    UNKNOWN_ERROR;

    public boolean shouldIncrementFailureCount() {
        return switch (this) {
            case INSUFFICIENT_BALANCE, LIMIT_EXCEEDED, WALLET_NOT_FOUND, WALLET_SUSPENDED -> true;
            case TRANSACTION_FAILED, DATABASE_ERROR, UNKNOWN_ERROR, IDEMPOTENCY_KEY_EXPIRED -> false;
        };
    }
}

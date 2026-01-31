package com.smartwealth.smartwealth_backend.exception.wallet;

import lombok.Getter;

@Getter
public class WalletTransactionException extends RuntimeException {
    private final WalletTransactionFailureType failureType;
    private final Throwable cause;

    public WalletTransactionException(
            WalletTransactionFailureType failureType,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.failureType = failureType;
        this.cause = cause;
    }

    public boolean shouldIncrementFailureCount() {
        return failureType.shouldIncrementFailureCount();
    }
}

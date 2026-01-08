package com.smartwealth.smartwealth_backend.exception;

public class WalletLimitExceededException extends RuntimeException {
    public WalletLimitExceededException(String message) {
        super(message);
    }
}

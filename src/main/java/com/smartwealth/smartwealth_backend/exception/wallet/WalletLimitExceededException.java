package com.smartwealth.smartwealth_backend.exception.wallet;

public class WalletLimitExceededException extends RuntimeException {
    public WalletLimitExceededException(String message) {
        super(message);
    }
}

package com.smartwealth.smartwealth_backend.exception.wallet;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

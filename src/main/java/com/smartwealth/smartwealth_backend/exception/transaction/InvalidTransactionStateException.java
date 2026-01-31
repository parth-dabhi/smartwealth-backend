package com.smartwealth.smartwealth_backend.exception.transaction;

public class InvalidTransactionStateException extends RuntimeException {
    public InvalidTransactionStateException(String message) {
        super(message);
    }
}

package com.smartwealth.smartwealth_backend.exception;

public class IdempotencyKeyExpiredException extends RuntimeException {
    public IdempotencyKeyExpiredException(String message) {
        super(message);
    }
}

package com.smartwealth.smartwealth_backend.exception.user;

public class KycVerificationException extends RuntimeException {
    public KycVerificationException(String message) {
        super(message);
    }
}

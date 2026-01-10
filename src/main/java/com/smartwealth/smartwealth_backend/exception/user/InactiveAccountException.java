package com.smartwealth.smartwealth_backend.exception.user;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(String message) {
        super(message);
    }
}

package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class HoldingNotFoundException extends RuntimeException {
    public HoldingNotFoundException(String message) {
        super(message);
    }
}

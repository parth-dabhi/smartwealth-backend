package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(String message) {
        super(message);
    }
}

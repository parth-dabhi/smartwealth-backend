package com.smartwealth.smartwealth_backend.exception.plan;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(String message) {
        super(message);
    }
}

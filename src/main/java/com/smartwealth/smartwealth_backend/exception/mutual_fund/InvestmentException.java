package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class InvestmentException extends RuntimeException {
    public InvestmentException(String message) {
        super(message);
    }

    public InvestmentException(String message, Throwable cause) {
        super(message, cause);
    }
}

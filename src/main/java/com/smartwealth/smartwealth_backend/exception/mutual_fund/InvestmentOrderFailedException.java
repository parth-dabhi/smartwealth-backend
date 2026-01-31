package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class InvestmentOrderFailedException extends RuntimeException {
    public InvestmentOrderFailedException(String message) {
        super(message);
    }
}

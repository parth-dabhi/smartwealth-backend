package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class InvalidInvestmentOrderException extends RuntimeException {
    public InvalidInvestmentOrderException(String message) {
        super(message);
    }
}

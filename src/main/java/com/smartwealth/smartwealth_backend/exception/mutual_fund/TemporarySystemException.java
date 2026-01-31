package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class TemporarySystemException extends InvestmentException {

    public TemporarySystemException(String message) {
        super(message);
    }

    public TemporarySystemException(String message, Throwable cause) {
        super(message, cause);
    }
}

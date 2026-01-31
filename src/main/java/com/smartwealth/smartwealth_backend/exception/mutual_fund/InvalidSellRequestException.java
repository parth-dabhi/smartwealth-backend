package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class InvalidSellRequestException extends RuntimeException {
    public InvalidSellRequestException(String message) {
        super(message);
    }
}

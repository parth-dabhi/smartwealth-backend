package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class HoldingUpdateFailedException extends RuntimeException {
    public HoldingUpdateFailedException(String message) {
        super(message);
    }
}

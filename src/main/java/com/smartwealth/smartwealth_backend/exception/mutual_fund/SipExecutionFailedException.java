package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class SipExecutionFailedException extends RuntimeException {
    public SipExecutionFailedException(String message) {
        super(message);
    }
}

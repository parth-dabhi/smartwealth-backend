package com.smartwealth.smartwealth_backend.exception.mutual_fund;

public class SipNotAllowedException extends RuntimeException {
    public SipNotAllowedException(String message) {
        super(message);
    }
}

package com.smartwealth.smartwealth_backend.exception.transaction;

public class TransactionFailedException extends RuntimeException {



    public TransactionFailedException(String message) {
        super(message);
    }
}

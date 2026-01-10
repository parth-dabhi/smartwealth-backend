package com.smartwealth.smartwealth_backend.exception.wallet;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}

package com.smartwealth.smartwealth_backend.exception.wallet;

public class WalletSuspendedException extends RuntimeException {
    public WalletSuspendedException(String message) {
        super(message);
    }
}

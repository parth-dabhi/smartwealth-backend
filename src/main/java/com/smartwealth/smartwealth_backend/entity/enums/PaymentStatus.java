package com.smartwealth.smartwealth_backend.entity.enums;

public enum PaymentStatus {
    // BUY FLOW
    FUNDS_LOCKED,     // wallet amount locked
    FUNDS_DEBITED,    // money sent to fund
    FUNDS_REVERSED,    // lock released and/or refund completed

    // SELL FLOW
    PENDING_CREDIT,    // sell validated, waiting for NAV & settlement
    FUNDS_CREDITED,     // redemption amount credited to wallet
    CREDIT_FAILED      // redemption failed (wallet / settlement failure)
}

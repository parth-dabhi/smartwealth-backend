package com.smartwealth.smartwealth_backend.entity.enums;

public enum InvestmentMode {
    LUMPSUM,
    SIP
}

/*
BUY + SIP → SIP installment
BUY + LUMPSUM → one-time purchase
SELL + LUMPSUM → redemption
SELL + SIP  (not allowed)
*/
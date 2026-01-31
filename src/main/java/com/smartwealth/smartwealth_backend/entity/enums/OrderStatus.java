package com.smartwealth.smartwealth_backend.entity.enums;

public enum OrderStatus {
    PENDING,     // Order created, not yet attempted
    NAV_PENDING, // NAV date reached but NAV not published
    ALLOTTED,    // NAV applied, units/money finalized
    FAILED,      // error, insufficient balance, etc. Permanent failure
    CANCELLED    // user/system cancelled before allotment
}

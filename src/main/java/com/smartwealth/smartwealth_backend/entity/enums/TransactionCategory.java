package com.smartwealth.smartwealth_backend.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionCategory {
    TOP_UP("Wallet top-up"),
    WITHDRAWAL("Wallet withdrawal"),
    INVESTMENT("Investment transaction"),
    REDEMPTION("Investment redemption"),
    REFUND("Refund transaction");

    private final String description;

    TransactionCategory(String description) {
        this.description = description;
    }
}

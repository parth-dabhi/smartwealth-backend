package com.smartwealth.smartwealth_backend.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionType {

    CREDIT("WALLET-CREDIT"),
    DEBIT("WALLET-DEBIT"),
    LOCK("WALLET-LOCK"),
    UNLOCK("WALLET-UNLOCK"),
    DBT_LOCKED("WALLET-DEBIT-LOCKED");

    private final String operationPrefix;

    TransactionType(String operationPrefix) {
        this.operationPrefix = operationPrefix;
    }
}

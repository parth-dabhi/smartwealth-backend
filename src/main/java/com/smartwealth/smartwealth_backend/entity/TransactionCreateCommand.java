package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TransactionCreateCommand {

    private User user;
    private Wallet wallet;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String idempotencyKey;
    private String referenceId;
    private String description;

    public TransactionCreateCommand(
            User user,
            Wallet wallet,
            BigDecimal amount,
            TransactionType transactionType,
            String idempotencyKey,
            String referenceId,
            String description
    ) {
        this.user = user;
        this.wallet = wallet;
        this.amount = amount;
        this.transactionType = transactionType;
        this.idempotencyKey = idempotencyKey;
        this.referenceId = referenceId;
        this.description = description;
    }

    public static TransactionCreateCommand from(
            BigDecimal amount,
            String idempotencyKey,
            User user,
            Wallet wallet,
            TransactionType transactionType
    ) {

        String operationPrefix = (transactionType == TransactionType.CREDIT) ? "WALLET-CREDIT" : "WALLET-DEBIT";

        return TransactionCreateCommand.builder()
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .transactionType(transactionType)
                .idempotencyKey(idempotencyKey)
                .referenceId(operationPrefix + "-" + System.currentTimeMillis())
                .build();
    }
}

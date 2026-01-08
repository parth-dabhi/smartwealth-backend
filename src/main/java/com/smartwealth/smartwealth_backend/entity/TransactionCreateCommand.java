package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TransactionCreateCommand {

    private final User user;
    private final Wallet wallet;
    private final BigDecimal amount;
    private final TransactionType transactionType;
    private final TransactionStatus status;
    private final String idempotencyKey;
    private final String referenceId;
    private final String description;

    public TransactionCreateCommand(
            User user,
            Wallet wallet,
            BigDecimal amount,
            TransactionType transactionType,
            TransactionStatus status,
            String idempotencyKey,
            String referenceId,
            String description
    ) {
        this.user = user;
        this.wallet = wallet;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.referenceId = referenceId;
        this.description = description;
    }

    public static TransactionCreateCommand from(
            BigDecimal amount,
            String idempotencyKey,
            User user,
            Wallet wallet,
            TransactionType transactionType,
            TransactionStatus status) {

        String operationPrefix = (transactionType == TransactionType.CREDIT) ? "WALLET-CREDIT" : "WALLET-DEBIT";
        String descriptionText = (transactionType == TransactionType.CREDIT) ? "Wallet credit" : "Wallet debit";

        return TransactionCreateCommand.builder()
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .transactionType(transactionType)
                .status(status)
                .idempotencyKey(idempotencyKey)
                .referenceId(operationPrefix + "-" + System.currentTimeMillis())
                .description(descriptionText)
                .build();
    }
}

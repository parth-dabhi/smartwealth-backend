package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
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
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private TransactionType transactionType;
    private TransactionCategory transactionCategory;
    private String idempotencyKey;
    private String referenceId;
    private String description;

    public static TransactionCreateCommand from(
            User user,
            Wallet wallet,
            BigDecimal amount,
            String idempotencyKey,
            TransactionType transactionType,
            TransactionCategory transactionCategory
    ) {

        String operationPrefix =
                (transactionType == TransactionType.CREDIT) ? "WALLET-CREDIT" : "WALLET-DEBIT";

        return TransactionCreateCommand.builder()
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .transactionType(transactionType)
                .transactionCategory(transactionCategory)
                .idempotencyKey(idempotencyKey)
                .referenceId(operationPrefix + "-" + System.currentTimeMillis())
                .build();
    }
}
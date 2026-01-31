package com.smartwealth.smartwealth_backend.entity.transaction;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TransactionCreateCommand {

    private Long userId;
    private Long walletId;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private TransactionType transactionType;
    private TransactionCategory transactionCategory;
    private String idempotencyKey;
    private String referenceId;
    private String description;

    public static TransactionCreateCommand from(
            Long userId,
            Long walletId,
            BigDecimal amount,
            String idempotencyKey,
            TransactionType transactionType,
            TransactionCategory transactionCategory
    ) {

        String operationPrefix = transactionType.getOperationPrefix();

        return TransactionCreateCommand.builder()
                .userId(userId)
                .walletId(walletId)
                .amount(amount)
                .transactionType(transactionType)
                .transactionCategory(transactionCategory)
                .idempotencyKey(idempotencyKey)
                .referenceId(operationPrefix + "-" + UUID.randomUUID())
                .build();
    }
}
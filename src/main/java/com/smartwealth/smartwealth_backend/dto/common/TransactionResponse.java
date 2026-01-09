package com.smartwealth.smartwealth_backend.dto.common;

import com.smartwealth.smartwealth_backend.entity.Transaction;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    Long id;
    TransactionType transactionType;
    TransactionCategory transactionCategory;
    BigDecimal amount;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    String referenceId;
    BigDecimal balance;
    BigDecimal lockedBalance;
    String message;

    public static TransactionResponse fromEntity(Transaction transaction, BigDecimal balance, BigDecimal lockedBalance, String message) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .transactionCategory(transaction.getTransactionCategory())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .referenceId(transaction.getReferenceId())
                .balance(balance)
                .lockedBalance(lockedBalance)
                .message(message)
                .build();
    }
}

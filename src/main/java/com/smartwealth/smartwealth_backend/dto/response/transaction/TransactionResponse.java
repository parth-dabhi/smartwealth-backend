package com.smartwealth.smartwealth_backend.dto.response.transaction;

import com.smartwealth.smartwealth_backend.entity.transaction.Transaction;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    String referenceId;
    TransactionStatus status;
    TransactionType transactionType;
    TransactionCategory transactionCategory;
    BigDecimal amount;
    BigDecimal balanceBefore;
    BigDecimal balanceAfter;
    BigDecimal totalBalance;
    BigDecimal lockedBalance;
    BigDecimal netBalance;
    String message;

    public static TransactionResponse fromEntity(Transaction transaction, BigDecimal balance, BigDecimal lockedBalance, String message) {
        return TransactionResponse.builder()
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .transactionCategory(transaction.getTransactionCategory())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .referenceId(transaction.getReferenceId())
                .totalBalance(balance)
                .lockedBalance(lockedBalance)
                .netBalance(balance.subtract(lockedBalance))
                .message(message)
                .build();
    }
}

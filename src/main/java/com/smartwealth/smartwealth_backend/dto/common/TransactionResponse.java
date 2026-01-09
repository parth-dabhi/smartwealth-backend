package com.smartwealth.smartwealth_backend.dto.common;

import com.smartwealth.smartwealth_backend.entity.Transaction;
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
    BigDecimal amount;
    String referenceId;
    BigDecimal balance;
    BigDecimal lockedBalance;
    String message;

    public static TransactionResponse fromEntity(Transaction transaction, BigDecimal balance, BigDecimal lockedBalance, String message) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .balance(balance)
                .lockedBalance(lockedBalance)
                .message(message)
                .build();
    }
}

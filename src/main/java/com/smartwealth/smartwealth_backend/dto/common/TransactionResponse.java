package com.smartwealth.smartwealth_backend.dto.common;

import com.smartwealth.smartwealth_backend.entity.Transaction;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    Long id;
    TransactionType transactionType;
    TransactionStatus transactionStatus;
    BigDecimal amount;
    String referenceId;
    String description;
    BigDecimal balance;
    BigDecimal lockedBalance;

    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getStatus())
                .amount(transaction.getAmount())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .balance(transaction.getWallet().getBalance())
                .lockedBalance(transaction.getWallet().getLockedBalance())
                .build();
    }
}

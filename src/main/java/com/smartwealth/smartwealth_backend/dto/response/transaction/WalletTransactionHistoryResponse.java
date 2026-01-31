package com.smartwealth.smartwealth_backend.dto.response.transaction;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.repository.wallet.projection.WalletTransactionHistoryProjection;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class WalletTransactionHistoryResponse {
    private String referenceId;
    private TransactionType transactionType;
    private TransactionCategory transactionCategory;
    private BigDecimal amount;
    private TransactionStatus status;
    private BigDecimal balanceAfter;
    private String description;
    private OffsetDateTime createdAt;

    public static WalletTransactionHistoryResponse fromProjection(
            WalletTransactionHistoryProjection p
    ) {
        return WalletTransactionHistoryResponse.builder()
                .referenceId(p.getReferenceId())
                .transactionType(p.getTransactionType())
                .transactionCategory(p.getTransactionCategory())
                .amount(p.getAmount())
                .status(p.getStatus())
                .balanceAfter(p.getBalanceAfter())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

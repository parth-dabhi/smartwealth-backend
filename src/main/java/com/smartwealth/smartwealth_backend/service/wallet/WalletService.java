package com.smartwealth.smartwealth_backend.service.wallet;

import com.smartwealth.smartwealth_backend.dto.request.transaction.TransactionFilterRequest;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.transaction.WalletTransactionHistoryResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;

import java.math.BigDecimal;

public interface WalletService {
    TransactionResponse creditWallet(String customerId, BigDecimal amount, String idempotencyKey);
    TransactionResponse creditWallet(Long userId, BigDecimal amount, String idempotencyKey);
    TransactionResponse debitWallet(String customerId, BigDecimal amount, String idempotencyKey);

    WalletBalanceResponse getWalletBalance(String customerId);

    TransactionResponse lockAmountInWallet(Long userId, BigDecimal amount, String idempotencyKey);
    TransactionResponse unlockAmountInWallet(Long userId, BigDecimal amount, String idempotencyKey);
    TransactionResponse debitLockedAmountInWallet(Long userId, BigDecimal amount, String idempotencyKey);
    TransactionResponse refundDebitedAmountInWallet(Long userId, BigDecimal amount, String idempotencyKey);

    PaginationResponse<WalletTransactionHistoryResponse> getWalletTransactions(
            String customerId,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            TransactionFilterRequest filter
    );
}

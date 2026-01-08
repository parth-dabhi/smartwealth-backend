package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;

import java.math.BigDecimal;

public interface WalletService {
    WalletBalanceResponse creditWallet(String customerId, BigDecimal amount, String idempotencyKey);
    WalletBalanceResponse debitWallet(String customerId, BigDecimal amount, String idempotencyKey);
    WalletBalanceResponse getWalletBalance(String customerId);
}

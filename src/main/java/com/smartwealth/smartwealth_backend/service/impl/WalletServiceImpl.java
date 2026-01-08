package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.Wallet;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.exception.InactiveAccountException;
import com.smartwealth.smartwealth_backend.exception.KycVerificationException;
import com.smartwealth.smartwealth_backend.exception.WalletNotFoundException;
import com.smartwealth.smartwealth_backend.repository.WalletRepository;
import com.smartwealth.smartwealth_backend.service.TransactionService;
import com.smartwealth.smartwealth_backend.service.UserService;
import com.smartwealth.smartwealth_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserService userService;
    private final TransactionService transactionService;
    private final WalletRepository walletRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletBalanceResponse getWalletBalance(String customerId) {
        User user = userService.getUserByCustomerId(customerId);
        Wallet wallet = getWalletByUser(user);
        return WalletBalanceResponse.from(wallet.getBalance(), wallet.getLockedBalance(), "Wallet balance retrieved successfully");
    }

    @Override
    @Transactional
    public WalletBalanceResponse creditWallet(String customerId, BigDecimal amount, String idempotencyKey) {
        validatePositiveAmount(amount);
        User user = userService.getUserByCustomerId(customerId);
        validateUserEligibility(user.isActive(), user.getKycStatus());

        Wallet wallet = getWalletByUser(user);

        TransactionCreateCommand command = TransactionCreateCommand
                .from(amount, idempotencyKey, user, wallet, TransactionType.CREDIT, TransactionStatus.SUCCESS);
        TransactionResponse savedTransaction = transactionService.createTransaction(command);

        if (savedTransaction.getTransactionStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Transaction failed, wallet not credited");
        }

        log.info("Wallet credited. customerId={}, amount={}", customerId, amount);

        return WalletBalanceResponse.from(savedTransaction.getBalance(), savedTransaction.getLockedBalance(), "Wallet credited successfully");
    }

    @Override
    @Transactional
    public WalletBalanceResponse debitWallet(String customerId, BigDecimal amount, String idempotencyKey) {
        validatePositiveAmount(amount);
        User user = userService.getUserByCustomerId(customerId);
        validateUserEligibility(user.isActive(), user.getKycStatus());

        Wallet wallet = getWalletByUser(user);

        TransactionCreateCommand command = TransactionCreateCommand
                .from(amount, idempotencyKey, user, wallet, TransactionType.DEBIT, TransactionStatus.SUCCESS);
        TransactionResponse savedTransaction = transactionService.createTransaction(command);

        if (savedTransaction.getTransactionStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Transaction failed, wallet not debited");
        }

        log.info("Wallet debited. customerId={}, amount={}", customerId, amount);

        return WalletBalanceResponse.from(savedTransaction.getBalance(), savedTransaction.getLockedBalance(), "Wallet debited successfully");
    }

    private Wallet getWalletByUser(User user) {
        log.info("Fetching wallet for user id: {}", user.getId());
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user id: " + user.getId()));
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    private void validateUserEligibility(boolean isActive, KycStatus kycStatus) {
        if (!isActive) {
            throw new InactiveAccountException("User account is not active, can't credit or debit wallet");
        }
        if (kycStatus != KycStatus.VERIFIED) {
            throw new KycVerificationException("KYC verification is " + kycStatus.toString().toLowerCase() + ", can't credit or debit wallet");
        }
    }
}

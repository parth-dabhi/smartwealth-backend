package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.Wallet;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.exception.InactiveAccountException;
import com.smartwealth.smartwealth_backend.exception.KycVerificationException;
import com.smartwealth.smartwealth_backend.exception.WalletNotFoundException;
import com.smartwealth.smartwealth_backend.repository.WalletRepository;
import com.smartwealth.smartwealth_backend.repository.projection.WalletBalanceProjection;
import com.smartwealth.smartwealth_backend.service.UserService;
import com.smartwealth.smartwealth_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final UserService userService;
    private final WalletRepository walletRepository;
    private final WalletTransactionExecutor walletTransactionExecutor;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * READ-ONLY operation.
     * Uses projection to avoid entity loading & persistence context overhead.
     */
    @Override
    @Transactional(readOnly = true, label = "WALLET_BALANCE_RETRIEVAL_OPERATION")
    public WalletBalanceResponse getWalletBalance(String customerId) {
        User user = userService.getUserByCustomerId(customerId);
        WalletBalanceProjection projection = walletRepository
                .findBalanceByUser(user)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for userId=" + user.getId()));
        return WalletBalanceResponse.from(
                projection.getBalance(),
                projection.getLockedBalance(),
                "Wallet balance retrieved successfully"
        );
    }

    /**
     * CREDIT orchestration.
     * NO transaction boundary here.
     */
    @Override
    public WalletBalanceResponse creditWallet(String customerId, BigDecimal amount, String idempotencyKey) {
        String redisKey = "tx:idempotency:" + idempotencyKey;
        // First check is retry within TTL
        TransactionResponse cachedResponse = redisIdempotencyCheck(redisKey, idempotencyKey);
        if (cachedResponse != null) {
            return WalletBalanceResponse.from(
                    cachedResponse.getBalance(),
                    cachedResponse.getLockedBalance(),
                    cachedResponse.getMessage()
            );
        }
        // If not then proceed to create new transaction
        TransactionCreateCommand command = getTransactionCreateCommand(customerId, amount, idempotencyKey, TransactionType.CREDIT);
        return walletTransactionExecutor.creditWalletExecutor(command);
    }

    /**
     * DEBIT orchestration.
     * NO transaction boundary here.
     */
    @Override
    public WalletBalanceResponse debitWallet(String customerId, BigDecimal amount, String idempotencyKey) {
        String redisKey = "tx:idempotency:" + idempotencyKey;
        // First check is retry within TTL
        TransactionResponse cachedResponse = redisIdempotencyCheck(redisKey, idempotencyKey);
        if (cachedResponse != null) {
            return WalletBalanceResponse.from(
                    cachedResponse.getBalance(),
                    cachedResponse.getLockedBalance(),
                    cachedResponse.getMessage()
            );
        }
        // If not then proceed to create new transaction
        TransactionCreateCommand command = getTransactionCreateCommand(customerId, amount, idempotencyKey, TransactionType.DEBIT);
        return walletTransactionExecutor.debitWalletExecutor(command);
    }

    /**
     * Redis-based idempotency check.
     * 1. If key exists, return cached response.
     * 2. If key does not exist, proceed.
     */
    private TransactionResponse redisIdempotencyCheck(String redisKey, String idempotencyKey) {
        // 1. Retry within TTL then return cached transaction response & handle redis failures
        try {
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached instanceof TransactionResponse cachedResponse) {
                log.info("Idempotency HIT (Redis). key={}", idempotencyKey);
                cachedResponse.setMessage(cachedResponse.getMessage() + " (from cache)");
                return cachedResponse;
            }
        } catch (Exception ex) {
            log.warn("Redis unavailable during idempotency check. key={}", idempotencyKey, ex);
        }
        return null;
    }

    private TransactionCreateCommand getTransactionCreateCommand(String customerId, BigDecimal amount, String idempotencyKey, TransactionType transactionType) {
        validatePositiveAmount(amount);
        User user = userService.getUserByCustomerId(customerId);
        validateUserEligibility(user.isActive(), user.getKycStatus());
        Wallet wallet = getWalletByUser(user);
        return TransactionCreateCommand.from(
                amount,
                idempotencyKey,
                user,
                wallet,
                transactionType
        );
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    private void validateUserEligibility(boolean isActive, KycStatus kycStatus) {
        if (!isActive) {
            throw new InactiveAccountException("User account is not active");
        }
        if (kycStatus != KycStatus.VERIFIED) {
            throw new KycVerificationException("KYC verification is " + kycStatus.name().toLowerCase() + ". Transaction not allowed.");
        }
    }

    private Wallet getWalletByUser(User user) {
        log.info("Fetching wallet for user id: {}", user.getId());
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for userId=" + user.getId()));
    }
}

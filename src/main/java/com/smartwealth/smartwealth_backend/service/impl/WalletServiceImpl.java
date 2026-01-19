package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.entity.enums.WalletStatus;
import com.smartwealth.smartwealth_backend.exception.user.InactiveAccountException;
import com.smartwealth.smartwealth_backend.exception.user.KycVerificationException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletNotFoundException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletSuspendedException;
import com.smartwealth.smartwealth_backend.repository.WalletRepository;
import com.smartwealth.smartwealth_backend.repository.projection.UserEligibilityProjection;
import com.smartwealth.smartwealth_backend.repository.projection.WalletProjection;
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
        Long userId = userService.getUserIdByCustomerId(customerId);
        WalletProjection projection = getWalletProjectionByUserId(userId);
        if (projection.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletSuspendedException("Wallet is not active for customerId=" + customerId);
        }
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
        UserEligibilityProjection userEligibility = userService.getUserEligibilityByCustomerId(customerId);
        Long userId = userEligibility.getId();
        validateUserEligibility(userEligibility.getIsActive(), userEligibility.getKycStatus());

        WalletProjection walletProjection = getWalletProjectionByUserId(userId);
        Long walletId = walletProjection.getId();
        WalletStatus walletStatus = walletProjection.getStatus();
        if (walletStatus != WalletStatus.ACTIVE) {
            throw new WalletSuspendedException("Wallet is not active for customerId=" + customerId + "Can't perform " + transactionType.name() + " transaction.");
        }

        TransactionCategory category = (transactionType == TransactionType.CREDIT) ? TransactionCategory.TOP_UP : TransactionCategory.WITHDRAWAL;

        return TransactionCreateCommand.from(
                userId,
                walletId,
                amount,
                idempotencyKey,
                transactionType,
                category
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

    private WalletProjection getWalletProjectionByUserId(Long userId) {
        log.info("Fetching wallet projection for user id: {}", userId);
        return walletRepository.findWalletProjectionByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for userId=" + userId));
    }
}

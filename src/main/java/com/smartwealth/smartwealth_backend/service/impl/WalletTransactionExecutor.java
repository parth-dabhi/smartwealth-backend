package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.TransactionResponse;
import com.smartwealth.smartwealth_backend.dto.response.wallet.WalletBalanceResponse;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionExecutor {
    private final TransactionService transactionService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(5);

    /**
     * CREDIT wallet
     * SINGLE transaction boundary (DB is source of truth)
     */
    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_CREDIT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public WalletBalanceResponse creditWalletExecutor(TransactionCreateCommand command) {
        String redisKey = "tx:idempotency:" + command.getIdempotencyKey();
        TransactionResponse response = transactionService.createTransaction(command);
        registerAfterCommitRedis(redisKey, response);
        return WalletBalanceResponse.from(
                response.getBalance(),
                response.getLockedBalance(),
                response.getMessage()
        );
    }

    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_DEBIT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public WalletBalanceResponse debitWalletExecutor(TransactionCreateCommand command) {
        String redisKey = "tx:idempotency:" + command.getIdempotencyKey();
        TransactionResponse response = transactionService.createTransaction(command);
        registerAfterCommitRedis(redisKey, response);
        return WalletBalanceResponse.from(
                response.getBalance(),
                response.getLockedBalance(),
                response.getMessage()
        );
    }

    private void registerAfterCommitRedis(String redisKey, TransactionResponse response) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    redisTemplate.opsForValue().set(redisKey, response, IDEMPOTENCY_TTL);
                } catch (Exception ex) {
                    log.error("Redis write failed after commit. key={}", redisKey, ex);
                }
            }
        });
    }
}

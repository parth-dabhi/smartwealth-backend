package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.Transaction;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.exception.IdempotencyKeyExpiredException;
import com.smartwealth.smartwealth_backend.exception.InsufficientBalanceException;
import com.smartwealth.smartwealth_backend.exception.WalletLimitExceededException;
import com.smartwealth.smartwealth_backend.repository.TransactionRepository;
import com.smartwealth.smartwealth_backend.repository.WalletRepository;
import com.smartwealth.smartwealth_backend.service.TransactionService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("${smartwealth.limits.max-wallet-balance}")
    private BigDecimal maxBalanceLimit;

    private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(2);

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionCreateCommand command) {

        log.info(
                "Processing transaction. idempotencyKey={}, walletId={}, type={}, amount={}",
                command.getIdempotencyKey(),
                command.getWallet().getId(),
                command.getTransactionType(),
                command.getAmount()
        );

        String idempotencyKey = command.getIdempotencyKey();
        String redisKey = "tx:idempotency:" + idempotencyKey;

        // 1. Retry within TTL then return cached transaction
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached instanceof TransactionResponse cachedTx) {
            log.info("Idempotency HIT (within TTL). key={}", idempotencyKey);
            return cachedTx;
        }

        // 2. TTL expired but key reused then reject
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.warn("Idempotency EXPIRED. key={}", idempotencyKey);
            throw new IdempotencyKeyExpiredException(
                    "Idempotency key expired. Please retry with a new request."
            );
        }

        // 3. Apply wallet update
        applyWalletUpdate(command.getWallet().getId(), command.getAmount(), command.getTransactionType());

        // Ensure wallet entity reflects DB state
        entityManager.refresh(command.getWallet());

        // 4. Persist immutable transaction
        Transaction transaction = Transaction.create(command);
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction recorded. txId={}, walletId={}", savedTransaction.getId(), savedTransaction.getWallet().getId());
        TransactionResponse response = TransactionResponse.fromEntity(savedTransaction);

        // 5. Cache final result for retry window
        redisTemplate.opsForValue().set(
                redisKey,
                response,
                IDEMPOTENCY_TTL
        );
        log.info("Idempotency cache set. key={}, ttl={} seconds", idempotencyKey, IDEMPOTENCY_TTL.getSeconds());

        log.info(
                "Transaction SUCCESS. txId={}, walletId={}, type={}, amount={}, newBalance={}",
                savedTransaction.getId(),
                savedTransaction.getWallet().getId(),
                savedTransaction.getTransactionType(),
                savedTransaction.getAmount(),
                savedTransaction.getWallet().getBalance()
        );

        return response;
    }

    public void applyWalletUpdate(Long walletId, BigDecimal amount, TransactionType type) {
        log.info("Applying wallet update. walletId={}, type={}, amount={}", walletId, type, amount);
        int rowsUpdated = switch (type) {
            case CREDIT -> walletRepository.secureCredit(walletId, amount, maxBalanceLimit);
            case DEBIT -> walletRepository.secureDebit(walletId, amount);
        };

        if (rowsUpdated == 0) {
            throw (type == TransactionType.DEBIT)
                    ? new InsufficientBalanceException("Debit failed: Insufficient balance")
                    : new WalletLimitExceededException("Credit failed: Total balance cannot exceed ₹10,00,000");
        }
        log.info("Wallet update applied successfully. walletId={}, type={}, amount={}", walletId, type, amount);
    }
}
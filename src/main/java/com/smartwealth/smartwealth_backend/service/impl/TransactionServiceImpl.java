package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.Transaction;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.exception.transaction.IdempotencyKeyExpiredException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.exception.wallet.InsufficientBalanceException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletLimitExceededException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletNotFoundException;
import com.smartwealth.smartwealth_backend.repository.TransactionRepository;
import com.smartwealth.smartwealth_backend.repository.WalletRepository;
import com.smartwealth.smartwealth_backend.repository.projection.WalletProjection;
import com.smartwealth.smartwealth_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("${smartwealth.limits.max-wallet-balance}")
    private BigDecimal maxBalanceLimit;

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionLifecycleService transactionLifecycleService;

    /**
     * Creates a transaction and applies wallet update.
     * Transaction boundary is owned by WalletTransactionFacade.
     */
    @Override
    public TransactionResponse createTransaction(TransactionCreateCommand command) {
        log.info("Processing transaction. idempotencyKey={}, walletId={}, type={}, category={}, amount={}",
                command.getIdempotencyKey(),
                command.getWalletId(),
                command.getTransactionType(),
                command.getTransactionCategory(),
                command.getAmount()
        );

        String idempotencyKey = command.getIdempotencyKey();

        // 1. DB-based idempotency check
        try {
            if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
                log.warn("Idempotency EXPIRED. key={}", idempotencyKey);
                throw new IdempotencyKeyExpiredException(
                        "Idempotency key expired. Please retry with a new request."
                );
            }
        } catch (DataIntegrityViolationException ex) {
            log.warn("Idempotency EXPIRED or DUPLICATE. key={}", idempotencyKey);
            throw new IdempotencyKeyExpiredException("Idempotency key already used or expired");
        }

        // 2. Create PENDING transaction
        Transaction transaction = transactionLifecycleService.createPending(command);

        // 3. Apply wallet update
        WalletProjection updatedWallet = null;
        try {
            updatedWallet = applyWalletUpdate(command.getWalletId(), command.getAmount(), command.getTransactionType());
        } catch (InsufficientBalanceException | WalletLimitExceededException ex) {
            // Mark transaction FAILED in a NEW transaction
            transactionLifecycleService.markFailed(transaction.getId(), ex.getMessage());
            log.info("Transaction FAILED due to business rule. txId={}, walletId={}, type={}, amount={}, reason={}",
                    transaction.getId(), command.getWalletId(), command.getTransactionType(), command.getAmount(), ex.getMessage());
            throw new TransactionFailedException(ex.getMessage());
        } catch (Exception ex) {
            transactionLifecycleService.markFailed(transaction.getId(), "Unexpected error during wallet update: " + ex.getMessage());
            log.error("Unexpected error during wallet update. txId={}, walletId={}, type={}, amount={}",
                    transaction.getId(), command.getWalletId(), command.getTransactionType(), command.getAmount(), ex);
            throw ex;
        }

        // 4. mark transaction SUCCESS
        transactionLifecycleService.markSuccess(transaction.getId(), updatedWallet.getBalance(),
                "Wallet " + (command.getTransactionType() == TransactionType.CREDIT ? "credited" : "debited") + " successfully");

        log.info("Transaction SUCCESS. txId={}, walletId={}, type={}, amount={}, newBalance={}", transaction.getId(),
                transaction.getWalletId(), transaction.getTransactionType(), transaction.getAmount(), updatedWallet.getBalance());

        return TransactionResponse.fromEntity(
                transaction,
                updatedWallet.getBalance(),
                updatedWallet.getLockedBalance(),
                "Wallet" + (command.getTransactionType() == TransactionType.CREDIT ? " credited " : " debited ")
                        + "successfully, of category " + command.getTransactionCategory()
        );
    }

    /**
     * Wallet mutation is purely atomic SQL.
     * NO entity locking, NO pessimistic locks.
     */
    private WalletProjection applyWalletUpdate(Long walletId, BigDecimal amount, TransactionType type) {
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

        // Refresh wallet entity state
        return walletRepository.findWalletProjectionByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found after update. walletId=" + walletId));
    }
}
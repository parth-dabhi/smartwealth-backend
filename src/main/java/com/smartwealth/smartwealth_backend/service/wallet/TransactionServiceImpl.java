package com.smartwealth.smartwealth_backend.service.wallet;

import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.transaction.Transaction;
import com.smartwealth.smartwealth_backend.entity.transaction.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import com.smartwealth.smartwealth_backend.exception.transaction.IdempotencyKeyExpiredException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.exception.wallet.InsufficientBalanceException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletLimitExceededException;
import com.smartwealth.smartwealth_backend.exception.wallet.WalletNotFoundException;
import com.smartwealth.smartwealth_backend.repository.wallet.TransactionRepository;
import com.smartwealth.smartwealth_backend.repository.wallet.WalletRepository;
import com.smartwealth.smartwealth_backend.repository.wallet.projection.WalletProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
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
     * Transaction boundary is owned by WalletTransactionExecutor.
     *
     * Flow:
     * 1. Check idempotency (DB check)
     * 2. Create PENDING transaction (commits in new TX)
     * 3. Apply wallet update (in parent TX)
     * 4. Mark SUCCESS via event (commits after parent TX commits)
     *
     * If wallet update fails:
     * - Transaction is marked FAILED immediately (new TX)
     * - Exception is thrown to parent
     *
     * If parent TX rolls back after wallet update:
     * - Event listener marks transaction FAILED
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
        checkIdempotency(idempotencyKey);

        // 2. Create PENDING transaction
        Transaction transaction = transactionLifecycleService.createPending(command);

        // 3. Apply wallet update
        WalletProjection updatedWallet = updateWallet(transaction, command);

        String description = getDescriptionMessage(
                command.getTransactionType(),
                command.getTransactionCategory()
        );

        // 4. Schedule marking transaction SUCCESS (via event)
        transactionLifecycleService.markSuccess(
                transaction,
                updatedWallet.getBalance(),
                description,
                idempotencyKey
        );

        log.info("Transaction SUCCESS. txId={}, walletId={}, type={}, amount={}, newBalance={}", transaction.getId(),
                transaction.getWalletId(), transaction.getTransactionType(), transaction.getAmount(), updatedWallet.getBalance());

        return TransactionResponse.fromEntity(
                transaction,
                updatedWallet.getBalance(),
                updatedWallet.getLockedBalance(),
                description
        );
    }

    private void checkIdempotency(String idempotencyKey) {
        try {
            if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
                log.warn("Idempotency key already exists. key={}", idempotencyKey);
                throw new IdempotencyKeyExpiredException(
                        "Idempotency key expired. Please retry with a new request."
                );
            }
        } catch (IdempotencyKeyExpiredException ex) {
            // Re-throw our custom exception
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Idempotency key constraint violation. key={}", idempotencyKey);
            throw new IdempotencyKeyExpiredException("Idempotency key already used or expired");
        } catch (DataAccessException ex) {
            log.error("Database error during idempotency check. key={}", idempotencyKey, ex);
            throw new TransactionFailedException("Database error during idempotency check");
        }
    }

    private WalletProjection updateWallet(
            Transaction transaction,
            TransactionCreateCommand command
    ) {
        WalletProjection updatedWallet;

        try {
            updatedWallet = applyWalletUpdate(
                    command.getWalletId(),
                    command.getAmount(),
                    command.getTransactionType()
            );

        } catch (InsufficientBalanceException ex) {
            // Mark transaction FAILED immediately (commits in REQUIRES_NEW)
            transactionLifecycleService.markFailed(transaction, ex.getMessage());

            log.warn("Transaction FAILED: Insufficient balance. txId={}, walletId={}, type={}, amount={}",
                    transaction.getId(),
                    command.getWalletId(),
                    command.getTransactionType(),
                    command.getAmount());

            // Preserve original exception type for upstream handling
            throw ex;

        } catch (WalletLimitExceededException ex) {
            // Mark transaction FAILED immediately (commits in REQUIRES_NEW)
            transactionLifecycleService.markFailed(transaction, ex.getMessage());

            log.warn("Transaction FAILED: Wallet limit exceeded. txId={}, walletId={}, type={}, amount={}",
                    transaction.getId(),
                    command.getWalletId(),
                    command.getTransactionType(),
                    command.getAmount());

            // Preserve original exception type for upstream handling
            throw ex;

        } catch (WalletNotFoundException ex) {
            // Mark transaction FAILED immediately (commits in REQUIRES_NEW)
            transactionLifecycleService.markFailed(transaction, ex.getMessage());

            log.error("Transaction FAILED: Wallet not found. txId={}, walletId={}, type={}, amount={}",
                    transaction.getId(),
                    command.getWalletId(),
                    command.getTransactionType(),
                    command.getAmount());

            // Preserve original exception type for upstream handling
            throw ex;

        } catch (DataAccessException ex) {
            // Mark transaction FAILED immediately (commits in REQUIRES_NEW)
            transactionLifecycleService.markFailed(
                    transaction,
                    "Database error during wallet update: " + ex.getMessage()
            );

            log.error("Transaction FAILED: Database error. txId={}, walletId={}, type={}, amount={}",
                    transaction.getId(),
                    command.getWalletId(),
                    command.getTransactionType(),
                    command.getAmount(),
                    ex);

            // Wrap database exceptions for upstream handling
            throw ex;

        } catch (Exception ex) {
            // Mark transaction FAILED immediately (commits in REQUIRES_NEW)
            transactionLifecycleService.markFailed(
                    transaction,
                    "Unexpected error during wallet update: " + ex.getMessage()
            );

            log.error("Transaction FAILED: Unexpected error. txId={}, walletId={}, type={}, amount={}",
                    transaction.getId(),
                    command.getWalletId(),
                    command.getTransactionType(),
                    command.getAmount(),
                    ex);

            // Wrap unexpected exceptions
            throw new TransactionFailedException("Unexpected error during wallet update: " + ex.getMessage());
        }

        return updatedWallet;
    }

    private String getDescriptionMessage(TransactionType type, TransactionCategory category) {
        return switch (type) {
            case CREDIT -> category.getDescription() + " credited to wallet";
            case DEBIT -> category.getDescription() + " debited from wallet";
            case LOCK -> "Funds locked for " + category.getDescription();
            case UNLOCK -> "Locked funds released for " + category.getDescription();
            case DBT_LOCKED -> "Locked funds debited for " + category.getDescription();
        };

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
            case LOCK-> walletRepository.secureLockFunds(walletId, amount);
            case UNLOCK -> walletRepository.secureUnlockFunds(walletId, amount);
            case DBT_LOCKED -> walletRepository.secureDebitLockedFunds(walletId, amount);
        };

        if (rowsUpdated == 0) {
            throw switch (type) {
                case CREDIT -> new WalletLimitExceededException("Wallet credit limit exceeded. walletId=" + walletId);
                case DEBIT -> new InsufficientBalanceException("Insufficient wallet balance. walletId=" + walletId);
                case LOCK -> new InsufficientBalanceException("Insufficient wallet balance to lock funds. walletId=" + walletId);
                case UNLOCK -> new InsufficientBalanceException("Insufficient locked balance to unlock funds. walletId=" + walletId);
                case DBT_LOCKED -> new InsufficientBalanceException("Insufficient locked balance to debit locked funds. walletId=" + walletId);
            };
        }
        log.info("Wallet update applied successfully. walletId={}, type={}, amount={}", walletId, type, amount);

        // Refresh wallet entity state
        return walletRepository.findWalletProjectionByWalletId(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found after update. walletId=" + walletId));
    }
}
package com.smartwealth.smartwealth_backend.service.wallet;

import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.transaction.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.exception.transaction.IdempotencyKeyExpiredException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.exception.wallet.*;
import com.smartwealth.smartwealth_backend.service.common.AfterCommitRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletTransactionExecutor {
    private final TransactionService transactionService;
    private final AfterCommitRedisService afterCommitRedisService;

    private static final String KEY_PREFIX_IDEMPOTENCY = "tx:idempotency:";

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
    public TransactionResponse creditWalletExecutor(TransactionCreateCommand command) {
        return execute(command);
    }

    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_DEBIT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public TransactionResponse debitWalletExecutor(TransactionCreateCommand command) {
        return execute(command);
    }

    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_LOCK_AMOUNT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public TransactionResponse lockAmountInWalletExecutor(TransactionCreateCommand command) {
        return execute(command);
    }

    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_UNLOCK_AMOUNT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public TransactionResponse unlockAmountInWalletExecutor(TransactionCreateCommand command) {
        return execute(command);
    }

    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_DEBIT_LOCKED_AMOUNT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public TransactionResponse debitLockedAmountInWalletExecutor(TransactionCreateCommand command) {
        return execute(command);
    }

    @Transactional(
            rollbackFor = Exception.class,
            timeout = 5,
            label = "WALLET_REFUND_DEBITED_AMOUNT_OPERATION",
            isolation = Isolation.READ_COMMITTED
    )
    public TransactionResponse refundDebitedAmountInWalletExecutor(TransactionCreateCommand command) {
        return execute(command);
    }

    // COMMON EXECUTION

    private TransactionResponse execute(TransactionCreateCommand command) {
        String redisKey = KEY_PREFIX_IDEMPOTENCY + command.getIdempotencyKey();
        String operationType = command.getTransactionType().name();

        try {
            TransactionResponse response = transactionService.createTransaction(command);

            // Cache successful response
            afterCommitRedisService.putAfterCommit(redisKey, response);

            return response;

        } catch (InsufficientBalanceException ex) {
            log.error(
                    "Insufficient balance for {} operation. userId={}, walletId={}, amount={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getAmount(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.INSUFFICIENT_BALANCE,
                    "Insufficient balance for " + operationType + " operation",
                    ex
            );

        } catch (WalletLimitExceededException ex) {
            log.error(
                    "Wallet limit exceeded for {} operation. userId={}, walletId={}, amount={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getAmount(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.LIMIT_EXCEEDED,
                    "Wallet limit exceeded for " + operationType + " operation",
                    ex
            );

        } catch (WalletNotFoundException ex) {
            log.error(
                    "Wallet not found for {} operation. userId={}, walletId={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.WALLET_NOT_FOUND,
                    "Wallet not found for " + operationType + " operation",
                    ex
            );

        } catch (WalletSuspendedException ex) {
            log.error(
                    "Wallet suspended for {} operation. userId={}, walletId={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.WALLET_SUSPENDED,
                    "Wallet suspended for " + operationType + " operation",
                    ex
            );

        } catch (IdempotencyKeyExpiredException ex) {
            log.warn(
                    "Idempotency key expired for {} operation. userId={}, walletId={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.IDEMPOTENCY_KEY_EXPIRED,
                    "Idempotency key expired for " + operationType + " operation",
                    ex
            );

        } catch (TransactionFailedException ex) {
            log.error(
                    "Transaction failed for {} operation. userId={}, walletId={}, amount={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getAmount(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.TRANSACTION_FAILED,
                    "Transaction failed for " + operationType + " operation",
                    ex
            );

        } catch (DataAccessException ex) {
            log.error(
                    "Database error during {} operation. userId={}, walletId={}, amount={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getAmount(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.DATABASE_ERROR,
                    "Database error during " + operationType + " operation",
                    ex
            );

        } catch (Exception ex) {
            log.error(
                    "Unexpected error during {} operation. userId={}, walletId={}, amount={}, idempotencyKey={}",
                    operationType,
                    command.getUserId(),
                    command.getWalletId(),
                    command.getAmount(),
                    command.getIdempotencyKey(),
                    ex
            );
            throw new WalletTransactionException(
                    WalletTransactionFailureType.UNKNOWN_ERROR,
                    "Unexpected error during " + operationType + " operation: " + ex.getMessage(),
                    ex
            );
        }
    }
}

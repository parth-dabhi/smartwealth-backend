package com.smartwealth.smartwealth_backend.event.handler;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.event.TransactionSuccessEvent;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionNotFoundException;
import com.smartwealth.smartwealth_backend.repository.wallet.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

/**
 * Handles transaction lifecycle events.
 *
 * AFTER_COMMIT: Marks transaction SUCCESS when parent TX commits
 * AFTER_ROLLBACK: Marks transaction FAILED when parent TX rolls back
 *
 * This ensures transaction records always have final status (SUCCESS or FAILED),
 * never stuck in PENDING state.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventHandler {
    private final TransactionRepository transactionRepository;

    /**
     * Executes AFTER WalletTransactionExecutor commits successfully.
     * Marks transaction as SUCCESS.
     *
     * Scenarios:
     * 1. User API call → WalletTransactionExecutor commits → This runs
     * 2. Scheduler → InvestmentAllotment → WalletTransactionExecutor commits → This runs
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTransactionSuccess(TransactionSuccessEvent event) {
        int updated = transactionRepository.updateTransactionAsSuccess(
                event.getTransactionId(),
                TransactionStatus.SUCCESS,
                event.getBalanceAfter(),
                event.getDescription()
        );

        if (updated == 0) {
            log.error("Failed to mark transaction SUCCESS. txId={}", event.getTransactionId());
            throw new TransactionNotFoundException(
                    "Transaction not found while marking SUCCESS. txId=" + event.getTransactionId()
            );
        }

        log.info("Transaction marked SUCCESS. txId={}, balanceAfter={}",
                event.getTransactionId(), event.getBalanceAfter());
    }

    /**
     * Executes AFTER WalletTransactionExecutor OR InvestmentAllotmentService rolls back.
     * Marks transaction as FAILED.
     *
     * Scenarios:
     * 1. WalletTransactionExecutor fails and rolls back → This runs (but transaction already marked FAILED immediately)
     * 2. WalletTransactionExecutor commits → InvestmentAllotment fails and rolls back → This runs (critical case)
     *
     * The second scenario is why we need this listener:
     * - Wallet was debited (committed)
     * - markSuccess() was called (event published)
     * - Allotment logic failed
     * - This listener ensures transaction is marked FAILED to reflect the rollback
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTransactionRollback(TransactionSuccessEvent event) {
        Long txnId = event.getTransactionId();

        // Check current status to avoid overwriting if already FAILED
        TransactionStatus currentStatus = transactionRepository.findStatusById(txnId);

        if (currentStatus == TransactionStatus.FAILED) {
            log.info("Transaction already FAILED, skipping rollback handler. txId={}", txnId);
            return;
        }

        BigDecimal balanceBefore =
                transactionRepository.findBalanceBefore(txnId);

        int updated = transactionRepository.markOrderFailed(
                event.getTransactionId(),
                TransactionStatus.FAILED,
                balanceBefore,
                "Parent transaction rolled back"
        );

        if (updated == 0) {
            log.error("Failed to mark transaction FAILED after parent rollback. txId={}",
                    event.getTransactionId());
            throw new TransactionNotFoundException(
                    "Transaction not found while marking FAILED on rollback. txId=" + event.getTransactionId()
            );
        }

        log.warn("Transaction marked FAILED due to parent rollback. txId={}",
                event.getTransactionId());
    }
}

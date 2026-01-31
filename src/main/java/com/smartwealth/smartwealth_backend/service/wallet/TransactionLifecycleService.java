package com.smartwealth.smartwealth_backend.service.wallet;

import com.smartwealth.smartwealth_backend.entity.transaction.Transaction;
import com.smartwealth.smartwealth_backend.entity.transaction.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.event.TransactionSuccessEvent;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionNotFoundException;
import com.smartwealth.smartwealth_backend.repository.wallet.TransactionRepository;
import com.smartwealth.smartwealth_backend.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLifecycleService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a PENDING transaction in a NEW transaction.
     * Ensures visibility to other REQUIRES_NEW transactions.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createPending(TransactionCreateCommand command) {

        BigDecimal currentBalance =
                walletRepository.findWalletBalance(command.getWalletId());

        command.setBalanceBefore(currentBalance);
        command.setBalanceAfter(currentBalance);

        Transaction transaction = transactionRepository.save(
                Transaction.createPending(command, currentBalance)
        );

        log.info("Transaction created PENDING. txId={}, walletId={}, amount={}",
                transaction.getId(),
                transaction.getWalletId(),
                transaction.getAmount());

        return transaction;
    }

    /**
     * Marks transaction as FAILED in a NEW transaction.
     * Survives rollback of main business flow.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Transaction transaction, String reason) {
        Long transactionId = transaction.getId();

        BigDecimal balanceBefore =
                transactionRepository.findBalanceBefore(transactionId);

        int updated = transactionRepository.markOrderFailed(
                transactionId,
                TransactionStatus.FAILED,
                balanceBefore,
                reason
        );

        if (updated == 0) {
            log.error("Failed to mark transaction FAILED. txId={}", transactionId);
            throw new TransactionNotFoundException(
                    "Transaction not found while marking FAILED. txId=" + transactionId
            );
        }

        transaction.setBalanceAfter(balanceBefore);
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setDescription(reason);

        log.warn("Transaction marked FAILED. txId={}, reason={}",
                transactionId, reason);
    }

    /**
     * Publishes event to mark transaction as SUCCESS.
     * Event will be processed AFTER parent transaction commits.
     * If parent rolls back (e.g., scheduler failure), transaction will be marked FAILED instead.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void markSuccess(Transaction transaction, BigDecimal balanceAfter, String description, String idempotencyKey) {
        // Publish event - will be handled after commit or rollback
        eventPublisher.publishEvent(
                new TransactionSuccessEvent(
                        transaction.getId(),
                        balanceAfter,
                        description,
                        idempotencyKey
                )
        );

        transaction.setBalanceAfter(balanceAfter);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(description);

        log.info("Transaction marked SUCCESS. txId={}", transaction.getId());
    }
}

package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.entity.Transaction;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionLifecycleService {

    private final TransactionRepository transactionRepository;

    /**
     * Creates a PENDING transaction in a NEW transaction.
     * Ensures visibility to other REQUIRES_NEW transactions.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createPending(TransactionCreateCommand command) {

        BigDecimal currentBalance =
                transactionRepository.findWalletBalance(command.getWalletId());

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
    public void markFailed(Long transactionId, String reason) {

        BigDecimal balanceBefore =
                transactionRepository.findBalanceBefore(transactionId);

        int updated = transactionRepository.updateStatusDescriptionAndBalanceAfter(
                transactionId,
                TransactionStatus.FAILED,
                balanceBefore,
                reason
        );

        if (updated == 0) {
            log.error("Failed to mark transaction FAILED. txId={}", transactionId);
            throw new IllegalStateException(
                    "Transaction not found while marking FAILED. txId=" + transactionId
            );
        }

        log.warn("Transaction marked FAILED. txId={}, reason={}",
                transactionId, reason);
    }

    /**
     * Marks transaction as SUCCESS.
     * Usually called from the main transaction (NOT REQUIRES_NEW).
     */
    @Transactional
    public void markSuccess(Long transactionId, BigDecimal balanceAfter, String description) {

        int updated = transactionRepository.updateStatusDescriptionAndBalanceAfter(
                transactionId,
                TransactionStatus.SUCCESS,
                balanceAfter,
                description
        );

        if (updated == 0) {
            log.error("Failed to mark transaction SUCCESS. txId={}", transactionId);
            throw new IllegalStateException(
                    "Transaction not found while marking SUCCESS. txId=" + transactionId
            );
        }

        log.info("Transaction marked SUCCESS. txId={}", transactionId);
    }
}

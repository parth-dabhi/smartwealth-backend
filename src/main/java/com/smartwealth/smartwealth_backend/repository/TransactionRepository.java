package com.smartwealth.smartwealth_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.smartwealth.smartwealth_backend.entity.Transaction;
import com.smartwealth.smartwealth_backend.entity.User;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Fetch transactions of a user with pagination.
     * Used for "View Transactions" screens.
     */
    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Idempotency check.
     * Used before creating a transaction to prevent duplicates.
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}

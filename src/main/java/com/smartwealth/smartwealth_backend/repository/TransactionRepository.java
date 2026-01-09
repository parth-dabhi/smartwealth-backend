package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.smartwealth.smartwealth_backend.entity.Transaction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Idempotency check.
     * Used before creating a transaction to prevent duplicates.
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("""
    UPDATE Transaction t
    SET t.status = :status,
        t.description = :description
    WHERE t.id = :id
""")
    int updateStatusAndDescription(
            @Param("id") Long id,
            @Param("status") TransactionStatus status,
            @Param("description") String description
    );
}

package com.smartwealth.smartwealth_backend.repository.wallet;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.Optional;
import com.smartwealth.smartwealth_backend.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Idempotency check.
     * Used before creating a transaction to prevent duplicates.
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Modifying(flushAutomatically = true)
    @Query("""
    UPDATE Transaction t
    SET t.status = :status,
        t.description = :description,
        t.balanceAfter = :balanceAfter
    WHERE t.id = :id
""")
    int updateTransactionAsSuccess(
            @Param("id") Long id,
            @Param("status") TransactionStatus status,
            @Param("balanceAfter") BigDecimal balanceAfter,
            @Param("description") String description
    );

    @Modifying(flushAutomatically = true)
    @Query("""
    UPDATE Transaction t
    SET t.status = :status,
        t.description = :description,
        t.balanceAfter = :balanceAfter
    WHERE t.id = :id
""")
    int markOrderFailed(
            @Param("id") Long id,
            @Param("status") TransactionStatus status,
            @Param("balanceAfter") BigDecimal balanceAfter,
            @Param("description") String description
    );

    @Query("""
    SELECT t.balanceBefore
    FROM Transaction t
    WHERE t.id = :txId
""")
    BigDecimal findBalanceBefore(@Param("txId") Long txId);

    @Query("""
    SELECT t.status
    FROM Transaction t
    WHERE t.id = :txId
""")
    TransactionStatus findStatusById(@Param("txId") Long txId);
}

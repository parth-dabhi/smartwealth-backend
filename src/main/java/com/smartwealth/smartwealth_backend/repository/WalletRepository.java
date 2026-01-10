package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.Wallet;
import com.smartwealth.smartwealth_backend.repository.projection.WalletProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Fetch wallet by owner.
     * Primary access path for wallet.
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Used for existence checks / safety.
     */
    boolean existsByUserId(Long userId);

    /**
     * Atomic CREDIT operation.
     * Prevents race conditions & enforces max balance.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Wallet w
        SET w.balance = w.balance + :amount
        WHERE w.id = :walletId
          AND (w.balance + :amount) <= :maxBalance
    """)
    int secureCredit(
            @Param("walletId") Long walletId,
            @Param("amount") BigDecimal amount,
            @Param("maxBalance") BigDecimal maxBalance
    );

    /**
     * Atomic DEBIT operation.
     * Prevents overdraft & double-spend.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Wallet w
        SET w.balance = w.balance - :amount
        WHERE w.id = :walletId
          AND (w.balance - w.lockedBalance) >= :amount
    """)
    int secureDebit(
            @Param("walletId") Long walletId,
            @Param("amount") BigDecimal amount
    );

    @Query("""
    SELECT w.id AS id,
           w.balance AS balance,
           w.lockedBalance AS lockedBalance,
           w.status AS status
    FROM Wallet w
    WHERE w.userId = :userId
""")
    Optional<WalletProjection> findWalletProjectionByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT w.id AS id,
           w.balance AS balance,
           w.lockedBalance AS lockedBalance,
           w.status AS status
    FROM Wallet w
    WHERE w.id = :walletId
""")
    Optional<WalletProjection> findWalletProjectionByWalletId(@Param("walletId") Long walletId);
}
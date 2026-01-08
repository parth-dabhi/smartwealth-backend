package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Fetch wallet by owner.
     * Primary access path for wallet.
     */
    Optional<Wallet> findByUser(User user);

    /**
     * Used for existence checks / safety.
     */
    boolean existsByUser(User user);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount " +
            "WHERE w.id = :id " +
            "AND (w.balance + :amount) <= :maxBalance")
    int secureCredit(Long id, BigDecimal amount, BigDecimal maxBalance);

    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance - :amount " +
            "WHERE w.id = :id " +
            "AND (w.balance - w.lockedBalance) >= :amount")
    int secureDebit(Long id, BigDecimal amount);
}
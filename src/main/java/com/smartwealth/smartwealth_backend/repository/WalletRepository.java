package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
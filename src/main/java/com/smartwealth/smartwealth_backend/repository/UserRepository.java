package com.smartwealth.smartwealth_backend.repository;

import com.smartwealth.smartwealth_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByCustomerId(String customerId);
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNumber(String mobileNumber);

    // Exists checks - (used in validations)
    boolean existsByCustomerId(String customerId);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
}

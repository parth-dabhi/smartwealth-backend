package com.smartwealth.smartwealth_backend.repository.user;

import com.smartwealth.smartwealth_backend.entity.user.User;
import com.smartwealth.smartwealth_backend.repository.user.projection.UserEligibilityProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByCustomerId(String customerId);
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNumber(String mobileNumber);
    @Query("""
        select u.id
        from User u
        where u.customerId = :customerId
    """)
    Optional<Long> findUserIdByCustomerId(@Param("customerId") String customerId);

    @Query("""
        SELECT
            u.isActive AS isActive,
            u.kycStatus AS kycStatus
        FROM User u
        WHERE u.id = :userId
    """)
    Optional<UserEligibilityProjection> findEligibilityByCustomerId(
            @Param("userId") Long userId
    );

    // Exists checks - (used in validations)
    boolean existsByCustomerId(String customerId);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
}

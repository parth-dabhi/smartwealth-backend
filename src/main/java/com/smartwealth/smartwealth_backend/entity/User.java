package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_customer_id", columnList = "customer_id"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_mobile_number", columnList = "mobile_number")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", unique = true, length = 8, updatable = false)
    private String customerId; // 8-digit Customer ID - to be filled by service before insert.

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name="mobile_number", nullable=false, length=10, unique=true)
    private String mobileNumber;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Column(name="full_name", nullable=false, length=100)
    private String fullName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="kyc_status", nullable=false)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="risk_profile", nullable=false)
    private RiskProfile riskProfile = RiskProfile.MODERATE;

    @Builder.Default
    @Column(name="is_active", nullable=false)
    private boolean isActive = true;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Column(name="last_login_at")
    private Instant lastLoginAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

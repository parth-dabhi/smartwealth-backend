package com.smartwealth.smartwealth_backend.entity;

import com.smartwealth.smartwealth_backend.entity.enums.Gender;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_kyc_active", columnList = "kyc_status, is_active")
})
public class User {

    // Primary & External Identifiers

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true, length = 8, updatable = false)
    private String customerId; // 8-digit Customer ID - to be filled by service before insert.

    // Authentication & Identity

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name="mobile_number", nullable=false, length=10, unique=true)
    private String mobileNumber;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Column(name="full_name", nullable=false, length=100)
    private String fullName;

    // Profile Information

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Embedded
    private Address address;

    // Authorization & Compliance

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserRole role = UserRole.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="kyc_status", nullable=false)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name="risk_profile", nullable=false)
    private RiskProfile riskProfile = RiskProfile.MODERATE;

    // Audit Fields

    @Builder.Default
    @Column(name="is_active", nullable=false)
    private boolean isActive = true;

    @Column(name="created_at", nullable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Column(name="last_login_at")
    private OffsetDateTime lastLoginAt;

    // Lifecycle Hooks

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}

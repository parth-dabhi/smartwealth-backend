package com.smartwealth.smartwealth_backend.dto.response;

import com.smartwealth.smartwealth_backend.entity.enums.Gender;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class AdminUserDetailResponse {

    private String customerId;
    private String fullName;
    private String email;
    private String mobileNumber;

    private LocalDate dateOfBirth;
    private Gender gender;
    private AddressResponse address;

    private UserRole role;
    private KycStatus kycStatus;
    private RiskProfile riskProfile;

    private boolean isActive;
    private Instant createdAt;
    private Instant lastLoginAt;

    /**
     * Hypermedia links for admin navigation
     */
    private Map<String, String> _links;
}
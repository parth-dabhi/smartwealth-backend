package com.smartwealth.smartwealth_backend.dto.response.auth;

import com.smartwealth.smartwealth_backend.dto.response.user.AddressResponse;
import com.smartwealth.smartwealth_backend.entity.enums.Gender;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthResponse {
    private String customerId;
    private String email;
    private String mobileNumber;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private AddressResponse address;
    private UserRole role;
    private KycStatus kycStatus;
    private RiskProfile riskProfile;
    private boolean isActive;
    private Instant lastLoginAt;
}

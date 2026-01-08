package com.smartwealth.smartwealth_backend.dto.response.auth;

import com.smartwealth.smartwealth_backend.dto.response.user.AddressResponse;
import com.smartwealth.smartwealth_backend.entity.User;
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

    public static UserAuthResponse toResponse(User user, Instant lastLoginAt) {
        return UserAuthResponse.builder()
                .customerId(user.getCustomerId())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(AddressResponse.fromEntity(user.getAddress()))
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .riskProfile(user.getRiskProfile())
                .isActive(user.isActive())
                .lastLoginAt(lastLoginAt)
                .build();
    }
}

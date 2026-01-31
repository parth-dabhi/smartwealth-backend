package com.smartwealth.smartwealth_backend.dto.response.admin;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.response.user.AddressResponse;
import com.smartwealth.smartwealth_backend.entity.user.User;
import com.smartwealth.smartwealth_backend.entity.enums.Gender;
import com.smartwealth.smartwealth_backend.entity.enums.KycStatus;
import com.smartwealth.smartwealth_backend.entity.enums.RiskProfile;
import com.smartwealth.smartwealth_backend.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;

    /**
     * Hypermedia links for admin navigation
     */
    private Map<String, String> _links;

    public static AdminUserDetailResponse from(User user) {
        return AdminUserDetailResponse.builder()
                .customerId(user.getCustomerId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(
                        user.getAddress() == null ? null :
                                AddressResponse.builder()
                                        .addressLine1(user.getAddress().getAddressLine1())
                                        .addressLine2(user.getAddress().getAddressLine2())
                                        .city(user.getAddress().getCity())
                                        .state(user.getAddress().getState())
                                        .country(user.getAddress().getCountry())
                                        .postalCode(user.getAddress().getPostalCode())
                                        .build()
                )
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .riskProfile(user.getRiskProfile())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                ._links(Map.of(
                        "self", ApiPaths.API_ADMIN_USERS  + "/" + user.getCustomerId(),
                        "list", ApiPaths.API_ADMIN_USERS
                ))
                .build();
    }
}
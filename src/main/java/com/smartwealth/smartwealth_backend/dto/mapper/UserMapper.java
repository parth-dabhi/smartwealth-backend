package com.smartwealth.smartwealth_backend.dto.mapper;

import com.smartwealth.smartwealth_backend.dto.request.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.UserResponse;
import com.smartwealth.smartwealth_backend.entity.User;

public class UserMapper {

    public static User toEntity(UserCreateRequest dto) {
        return User.builder()
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .fullName(dto.getFullName())
                .passwordHash(dto.getPassword()) // will be hashed in user service
                .build();
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .customerId(user.getCustomerId())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .fullName(user.getFullName())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .riskProfile(user.getRiskProfile())
                .isActive(user.isActive())
                .build();
    }
}

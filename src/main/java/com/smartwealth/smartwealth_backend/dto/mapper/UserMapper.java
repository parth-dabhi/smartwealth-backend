package com.smartwealth.smartwealth_backend.dto.mapper;

import com.smartwealth.smartwealth_backend.dto.request.user.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.UserAuthResponse;
import com.smartwealth.smartwealth_backend.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(UserCreateRequest dto) {
        return User.builder()
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .fullName(dto.getFullName())
                .passwordHash(dto.getPassword()) // will be hashed in user service
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .address(AddressMapper.toEntity(dto.getAddress()))
                .build();
    }

    public static UserAuthResponse toResponse(User user) {
        return UserAuthResponse.builder()
                .customerId(user.getCustomerId())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(AddressMapper.toResponse(user.getAddress()))
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .riskProfile(user.getRiskProfile())
                .isActive(user.isActive())
                .build();
    }
}

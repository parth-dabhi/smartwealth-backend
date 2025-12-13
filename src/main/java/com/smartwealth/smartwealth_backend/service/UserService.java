package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.request.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.UserResponse;
import com.smartwealth.smartwealth_backend.entity.User;

import java.util.Optional;

public interface UserService {

    Optional<UserResponse> createUser(UserCreateRequest request);
    Optional<UserResponse> getUserByCustomerId(String customerId);
    Optional<UserResponse> getUserByEmail(String email);
}

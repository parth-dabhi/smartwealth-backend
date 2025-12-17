package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.request.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.UserAuthResponse;

import java.util.Optional;

public interface UserService {

    Optional<UserAuthResponse> createUser(UserCreateRequest request);
    Optional<UserAuthResponse> getUserByCustomerId(String customerId);
    Optional<UserAuthResponse> getUserByEmail(String email);
}

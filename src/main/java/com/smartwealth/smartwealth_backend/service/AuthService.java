package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.request.RefreshTokenRequest;
import com.smartwealth.smartwealth_backend.dto.request.UserLoginRequest;
import com.smartwealth.smartwealth_backend.dto.response.AuthResponse;
import com.smartwealth.smartwealth_backend.dto.response.RefreshTokenResponse;

public interface AuthService {
    AuthResponse login(UserLoginRequest request);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}

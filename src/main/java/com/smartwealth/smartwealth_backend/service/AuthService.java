package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.request.auth.RefreshTokenRequest;
import com.smartwealth.smartwealth_backend.dto.request.auth.UserLoginRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.AuthResponse;
import com.smartwealth.smartwealth_backend.dto.response.auth.RefreshTokenResponse;

public interface AuthService {
    AuthResponse login(UserLoginRequest request);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}

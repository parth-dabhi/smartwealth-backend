package com.smartwealth.smartwealth_backend.controller;

import com.smartwealth.smartwealth_backend.dto.request.RefreshTokenRequest;
import com.smartwealth.smartwealth_backend.dto.request.UserLoginRequest;
import com.smartwealth.smartwealth_backend.dto.response.AuthResponse;
import com.smartwealth.smartwealth_backend.dto.response.RefreshTokenResponse;
import com.smartwealth.smartwealth_backend.service.AuthService;
import com.smartwealth.smartwealth_backend.api.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_AUTH)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * LOGIN API
     * Public endpoint
     * Authenticates user using customerId + password
     */

    @PostMapping(ApiPaths.AUTH_LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request for customerId={}", request.getCustomerId());
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * REFRESH TOKEN API
     * Public endpoint
     * Issues new access token using valid refresh token
     */
    @PostMapping(ApiPaths.AUTH_REFRESH)
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received refresh token request");
        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}

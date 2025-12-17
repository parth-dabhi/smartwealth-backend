package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.request.RefreshTokenRequest;
import com.smartwealth.smartwealth_backend.dto.request.UserLoginRequest;
import com.smartwealth.smartwealth_backend.dto.response.AuthResponse;
import com.smartwealth.smartwealth_backend.dto.response.RefreshTokenResponse;
import com.smartwealth.smartwealth_backend.dto.response.UserAuthResponse;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.exception.AuthenticationException;
import com.smartwealth.smartwealth_backend.repository.UserRepository;
import com.smartwealth.smartwealth_backend.security.JwtTokenProvider;
import com.smartwealth.smartwealth_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true, timeout = 5)
    public AuthResponse login(UserLoginRequest request) {

        log.info("Attempting login for customerId={}", request.getCustomerId());

        User user = userRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Login failed: customerId {} not found", request.getCustomerId());
                    return new AuthenticationException("Invalid customer ID or password");
                });

        // Correct password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: incorrect password for customerId={}", request.getCustomerId());
            throw new AuthenticationException("Invalid customer ID or password");
        }

        String accessToken =
                jwtTokenProvider.generateAccessToken(
                        user.getCustomerId(),
                        user.getRole().name()
                );

        String refreshToken =
                jwtTokenProvider.generateRefreshToken(user.getCustomerId());

        log.info("Login successful for customerId={}", user.getCustomerId());

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                .user(buildUserAuthResponse(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true, timeout = 5)
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {

        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String customerId =
                jwtTokenProvider.getCustomerIdFromRefreshToken(request.getRefreshToken());

        User user = userRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        return RefreshTokenResponse.builder()
                .accessToken(
                        jwtTokenProvider.generateAccessToken(
                                user.getCustomerId(),
                                user.getRole().name()
                        )
                )
                .refreshToken(
                        jwtTokenProvider.generateRefreshToken(user.getCustomerId())
                )
                .build();
    }

    private UserAuthResponse buildUserAuthResponse(User user) {
        return UserAuthResponse.builder()
                .customerId(user.getCustomerId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .kycStatus(user.getKycStatus())
                .riskProfile(user.getRiskProfile())
                .isActive(user.isActive())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}

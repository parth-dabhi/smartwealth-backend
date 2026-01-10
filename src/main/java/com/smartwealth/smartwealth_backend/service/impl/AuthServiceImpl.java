package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.request.auth.RefreshTokenRequest;
import com.smartwealth.smartwealth_backend.dto.request.auth.UserLoginRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.AuthResponse;
import com.smartwealth.smartwealth_backend.dto.response.auth.RefreshTokenResponse;
import com.smartwealth.smartwealth_backend.dto.response.auth.UserAuthResponse;
import com.smartwealth.smartwealth_backend.entity.User;
import com.smartwealth.smartwealth_backend.exception.auth.AuthenticationException;
import com.smartwealth.smartwealth_backend.repository.UserRepository;
import com.smartwealth.smartwealth_backend.security.JwtTokenProvider;
import com.smartwealth.smartwealth_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(timeout = 5)
    public AuthResponse login(UserLoginRequest request) {

        log.info("Attempting login for customerId={}", request.getCustomerId());

        User user = userRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Login failed: customerId {} not found", request.getCustomerId());
                    return new AuthenticationException("Invalid customer ID or password");
                });

        // TODO: Rate limiting to prevent brute-force attacks
        // TODO: Implement account lockout after multiple failed attempts -  max 3 attempts

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

        OffsetDateTime previousLoginAt = user.getLastLoginAt();

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user); // Update last login time to DB

        log.info("Updated lastLoginAt for customerId={}", user.getCustomerId());

        return AuthResponse.fromDetails(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiry(),
                UserAuthResponse.toResponse(user, previousLoginAt)
        );
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

        log.info("Refresh token successful for customerId={}", user.getCustomerId());

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
}

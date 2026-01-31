package com.smartwealth.smartwealth_backend.controller.auth;

import com.smartwealth.smartwealth_backend.dto.request.auth.RefreshTokenRequest;
import com.smartwealth.smartwealth_backend.dto.request.auth.UserLoginRequest;
import com.smartwealth.smartwealth_backend.dto.response.auth.AuthResponse;
import com.smartwealth.smartwealth_backend.dto.response.auth.RefreshTokenResponse;
import com.smartwealth.smartwealth_backend.service.auth.AuthService;
import com.smartwealth.smartwealth_backend.api.ApiPaths;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

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

//    @PostMapping(ApiPaths.AUTH_LOGIN)
//    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
//        log.info("Received login request for customerId={}", request.getCustomerId());
//        AuthResponse response = authService.login(request);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }

    @PostMapping(ApiPaths.AUTH_LOGIN)
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse httpResponse
    ) {
        log.info("Received login request for customerId={}", request.getCustomerId());

        AuthResponse response = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", response.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
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

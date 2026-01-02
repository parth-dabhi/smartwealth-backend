package com.smartwealth.smartwealth_backend.dto.response.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;  // in seconds
    private UserAuthResponse user;
}

package com.smartwealth.smartwealth_backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
}

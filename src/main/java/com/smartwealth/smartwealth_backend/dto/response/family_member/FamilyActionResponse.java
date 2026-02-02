package com.smartwealth.smartwealth_backend.dto.response.family_member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyActionResponse {

    private String status;        // SUCCESS / FAILED
    private String action;        // REQUEST_SENT / ACCEPTED / REVOKED
    private String message;       // Human-readable
    private OffsetDateTime timestamp;
}


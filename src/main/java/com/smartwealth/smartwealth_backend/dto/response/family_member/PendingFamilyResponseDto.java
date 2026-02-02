package com.smartwealth.smartwealth_backend.dto.response.family_member;

import com.smartwealth.smartwealth_backend.repository.family_member.projection.PendingFamilyRequestProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingFamilyResponseDto {
    Long requestId;
    Long requesterId;
    String requesterName;
    OffsetDateTime requestedAt;

    public static PendingFamilyResponseDto fromProjection(
            PendingFamilyRequestProjection projection
    ) {
        PendingFamilyResponseDto dto = new PendingFamilyResponseDto();
        dto.setRequestId(projection.getId());
        dto.setRequesterId(projection.getRequesterId());
        dto.setRequesterName(projection.getRequesterName());
        dto.setRequestedAt(projection.getCreatedAt());
        return dto;
    }
}

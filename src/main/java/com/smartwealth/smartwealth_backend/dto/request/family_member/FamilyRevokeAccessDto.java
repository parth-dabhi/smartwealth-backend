package com.smartwealth.smartwealth_backend.dto.request.family_member;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyRevokeAccessDto {

    @NotNull(message = "Family member ID is required")
    Long familyMemberId;
}


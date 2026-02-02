package com.smartwealth.smartwealth_backend.dto.response.family_member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class FamilyMemberListResponse {
    private Long familyMemberId;
    private String memberName;
}

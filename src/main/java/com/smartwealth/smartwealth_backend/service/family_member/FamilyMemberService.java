package com.smartwealth.smartwealth_backend.service.family_member;

import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyActionResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyMemberListResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.PendingFamilyResponseDto;

import java.util.List;

public interface FamilyMemberService {
    List<FamilyMemberListResponse> getAllFamilyMemberWhoIHaveAccess(String viewerCustomerId);
    List<FamilyMemberListResponse> getAllFamilyMembersWhoHaveAccessToMe(String ownerCustomerId);
    FamilyActionResponse sendRequest(String requesterCustomerId, String memberCustomerId);
    FamilyActionResponse  acceptRequest(String memberCustomerId, Long requestId);
    List<PendingFamilyResponseDto> getPendingRequests(String memberCustomerId);
    FamilyActionResponse  removeAccessibleFamilyMember(Long familyMemberId);
    FamilyActionResponse revokeFamilyMemberAccessToMe(Long familyMemberId);
}
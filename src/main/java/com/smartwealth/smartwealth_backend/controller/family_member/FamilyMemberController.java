package com.smartwealth.smartwealth_backend.controller.family_member;

import com.smartwealth.smartwealth_backend.api.ApiPaths;
import com.smartwealth.smartwealth_backend.dto.request.family_member.FamilyAccessRequestDto;
import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyActionResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyMemberListResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.PendingFamilyResponseDto;
import com.smartwealth.smartwealth_backend.service.family_member.FamilyMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.API_FAMILY)
@RequiredArgsConstructor
@Slf4j
public class FamilyMemberController {
    
    private final FamilyMemberService familyMemberService;

    @PostMapping(ApiPaths.REQUEST)
    public ResponseEntity<FamilyActionResponse > sendRequest(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody FamilyAccessRequestDto dto
    ) {
        return ResponseEntity.ok(
                familyMemberService.sendRequest(customerId, dto.getMemberCustomerId())
        );
    }

    @GetMapping(ApiPaths.PENDING_REQUESTS)
    public ResponseEntity<List<PendingFamilyResponseDto>> pendingRequests(
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(
                familyMemberService.getPendingRequests(customerId)
        );
    }

    @PostMapping(ApiPaths.ACCEPT_REQUEST)
    public ResponseEntity<FamilyActionResponse > accept(
            @AuthenticationPrincipal String customerId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                familyMemberService.acceptRequest(customerId, id)
        );
    }

    @PostMapping(ApiPaths.REVOKE)
    public ResponseEntity<FamilyActionResponse> revokeAccess(
            @AuthenticationPrincipal String ownerCustomerId,
            @RequestBody FamilyAccessRequestDto dto
    ) {
        return ResponseEntity.ok(
                familyMemberService.revokeAccess(ownerCustomerId, dto.getMemberCustomerId())
        );
    }

    @GetMapping(ApiPaths.MEMBERS)
    public ResponseEntity<List<FamilyMemberListResponse>> getFamilyMembers(
            @AuthenticationPrincipal String customerId
    ) {
        log.info("Fetching family members");
        return ResponseEntity.ok(
            familyMemberService.getAllFamilyMember(customerId)
        );
    }
}

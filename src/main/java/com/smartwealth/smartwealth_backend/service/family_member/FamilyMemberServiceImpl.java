package com.smartwealth.smartwealth_backend.service.family_member;

import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyActionResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyMemberListResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.PendingFamilyResponseDto;
import com.smartwealth.smartwealth_backend.entity.enums.FamilyRequestStatus;
import com.smartwealth.smartwealth_backend.entity.family_member.FamilyMember;
import com.smartwealth.smartwealth_backend.entity.family_member.FamilyMemberRequest;
import com.smartwealth.smartwealth_backend.repository.family_member.FamilyMemberRepository;
import com.smartwealth.smartwealth_backend.repository.family_member.FamilyMemberRequestRepository;
import com.smartwealth.smartwealth_backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRequestRepository requestRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserService userService;

    private static final int COOLDOWN_DAYS = 3;

    @Transactional
    public FamilyActionResponse sendRequest(String requesterCustomerId, String memberCustomerId) {

        if (requesterCustomerId.equals(memberCustomerId)) {
            log.warn("Requester and member cannot be the same. requesterCustomerId={}", requesterCustomerId);
            throw new IllegalArgumentException("Cannot send request to oneself");
        }

        Long requesterId = userService.getUserIdByCustomerId(requesterCustomerId);
        Long memberId = userService.getUserIdByCustomerId(memberCustomerId);

        log.info("Family access request initiated. requester={}, member={}",
                requesterId, memberId);

        if (familyMemberRepository.existsByViewerIdAndOwnerId(requesterId, memberId)) {
            log.warn("Access already granted. requester={}, member={}",
                    requesterId, memberId);
            throw new IllegalStateException("Access already granted");
        }

        Optional<FamilyMemberRequest> familyMemberRequestOpt =
                requestRepository.findByRequesterIdAndMemberId(requesterId, memberId);

        if (familyMemberRequestOpt.isPresent()) {

            OffsetDateTime nextAllowed =
                    familyMemberRequestOpt.get().getLastRequestedAt().plusDays(COOLDOWN_DAYS);

            if (OffsetDateTime.now().isBefore(nextAllowed)) {
                log.warn("Cooldown active. requester={}, member={}",
                        requesterId, memberId);
                return FamilyActionResponse.builder()
                        .status("FAILED")
                        .action("REQUEST_COOLDOWN")
                        .message("You can resend the request after " + nextAllowed.toLocalDate())
                        .timestamp(OffsetDateTime.now())
                        .build();
            }

            log.info("Resetting existing request to PENDING. requester={}, member={}",
                    requesterId, memberId);

            int updatedRows = requestRepository
                    .resetToPending(
                            requesterId,
                            memberId,
                            FamilyRequestStatus.PENDING,
                            OffsetDateTime.now()
                    );

            if (updatedRows == 0) {
                log.error("Failed to reset request to PENDING. requester={}, member={}",
                        requesterId, memberId);
                throw new IllegalStateException("Failed to reset request");
            }

            return success("REQUEST_RESENT", "Family access request resent successfully");
        }

        log.info("Creating new family access request. requester={}, member={}",
                requesterId, memberId);

        requestRepository.save(
                FamilyMemberRequest.builder()
                        .requesterId(requesterId)
                        .memberId(memberId)
                        .requestStatus(FamilyRequestStatus.PENDING)
                        .lastRequestedAt(OffsetDateTime.now())
                        .createdAt(OffsetDateTime.now())
                        .respondedAt(null)
                        .build()
        );

        log.info("Family access request sent successfully");

        return success("REQUEST_SENT", "Family access request sent successfully");
    }

    @Transactional
    public FamilyActionResponse acceptRequest(String memberCustomerId, Long requestId) {

        Long memberId = userService.getUserIdByCustomerId(memberCustomerId);

        FamilyMemberRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (!request.getMemberId().equals(memberId)) {
            throw new SecurityException("Unauthorized accept attempt");
        }

        log.info("Accepting family access request. requestId={}", requestId);

        int rowsAffected = requestRepository.markAsAccepted(
                request.getId(),
                FamilyRequestStatus.ACCEPTED,
                OffsetDateTime.now()
        );

        if (rowsAffected == 0) {
            log.error("Failed to mark request as ACCEPTED. requestId={}", requestId);
            throw new IllegalStateException("Failed to accept request");
        }

        familyMemberRepository.save(
                FamilyMember.builder()
                        .ownerId(request.getMemberId())
                        .viewerId(request.getRequesterId())
                        .createdAt(OffsetDateTime.now())
                        .build()
        );

        log.info("Family access granted. viewer={}, owner={}",
                request.getRequesterId(), request.getMemberId());

        return success("REQUEST_ACCEPTED", "Family access request accepted");
    }

    @Transactional(readOnly = true)
    public List<PendingFamilyResponseDto> getPendingRequests(String memberCustomerId) {
        Long memberId = userService.getUserIdByCustomerId(memberCustomerId);
        log.debug("Fetching pending family requests for member={}", memberId);
        return requestRepository.findPendingRequestsForMember(memberId)
                .stream()
                .map(PendingFamilyResponseDto::fromProjection).toList();
    }

    @Transactional
    public FamilyActionResponse revokeAccess(String ownerCustomerId, String viewerCustomerId) {

        Long ownerId = userService.getUserIdByCustomerId(ownerCustomerId);
        Long viewerId = userService.getUserIdByCustomerId(viewerCustomerId);

        log.warn("Revoking family access. owner={}, viewer={}", ownerId, viewerId);
        familyMemberRepository.deleteByViewerIdAndOwnerId(viewerId, ownerId);

        return success("ACCESS_REVOKED", "Family access revoked successfully");
    }

    @Override
    public List<FamilyMemberListResponse> getAllFamilyMember(String viewerCustomerId) {
        Long viewerId = userService.getUserIdByCustomerId(viewerCustomerId);
        log.info("Fetching all family members for ownerCustomerId={}", viewerCustomerId);
        return familyMemberRepository.findAllFamilyMembersByViewerId(viewerId)
                .stream()
                .map(fm -> new FamilyMemberListResponse(
                        fm.getFamilyMemberId(),
                        fm.getMemberName()
                ))
                .toList();
    }

    private FamilyActionResponse success(String action, String message) {
        return FamilyActionResponse.builder()
                .status("SUCCESS")
                .action(action)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}

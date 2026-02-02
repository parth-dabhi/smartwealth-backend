package com.smartwealth.smartwealth_backend.repository.family_member;

import com.smartwealth.smartwealth_backend.entity.enums.FamilyRequestStatus;
import com.smartwealth.smartwealth_backend.entity.family_member.FamilyMemberRequest;
import com.smartwealth.smartwealth_backend.repository.family_member.projection.PendingFamilyRequestProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FamilyMemberRequestRepository extends JpaRepository<FamilyMemberRequest, Long> {

    Optional<FamilyMemberRequest> findByRequesterIdAndMemberId(
            Long requesterId,
            Long memberId
    );

    @Query("""
        SELECT
            r.id as id,
            r.requesterId as requesterId,
            u.fullName as requesterName,
            r.createdAt as createdAt
        FROM FamilyMemberRequest r
        JOIN User u ON u.id = r.requesterId
        WHERE r.memberId = :memberId
          AND r.requestStatus = 'PENDING'
    """)
    List<PendingFamilyRequestProjection> findPendingRequestsForMember(
            @Param("memberId") Long memberId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE FamilyMemberRequest r
        SET r.requestStatus = :status,
            r.lastRequestedAt = :now,
            r.respondedAt = NULL
        WHERE r.requesterId = :requesterId
          AND r.memberId = :memberId
    """)
    int resetToPending(
            @Param("requesterId") Long requesterId,
            @Param("memberId") Long memberId,
            @Param("status") FamilyRequestStatus status,
            @Param("now") OffsetDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE FamilyMemberRequest r
        SET r.requestStatus = :status,
            r.respondedAt = :now
        WHERE r.id = :requestId
          AND r.requestStatus = 'PENDING'
    """)
    int markAsAccepted(
            @Param("requestId") Long requestId,
            @Param("status") FamilyRequestStatus status,
            @Param("now") OffsetDateTime now
    );
}

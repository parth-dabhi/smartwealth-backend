package com.smartwealth.smartwealth_backend.entity.family_member;

import com.smartwealth.smartwealth_backend.entity.enums.FamilyRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "family_member_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_family_request_pair",
                        columnNames = {"requester_id", "member_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_family_requests_target_status",
                        columnList = "member_id, request_status"
                ),
                @Index(
                        name = "idx_family_requests_requester",
                        columnList = "requester_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FamilyMemberRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false)
    private FamilyRequestStatus requestStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @Column(name = "last_requested_at", nullable = false)
    private OffsetDateTime lastRequestedAt;
}

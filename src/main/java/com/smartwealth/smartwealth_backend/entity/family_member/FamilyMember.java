package com.smartwealth.smartwealth_backend.entity.family_member;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "family_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_family_access_pair",
                        columnNames = {"viewer_id", "owner_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_family_access_viewer_owner",
                        columnList = "viewer_id, owner_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_member_id")
    private Long familyMemberId;

    @Column(name = "viewer_id", nullable = false)
    private Long viewerId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}

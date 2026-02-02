package com.smartwealth.smartwealth_backend.repository.family_member;

import com.smartwealth.smartwealth_backend.entity.family_member.FamilyMember;
import com.smartwealth.smartwealth_backend.repository.family_member.projection.FamilyMemberListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    boolean existsByViewerIdAndOwnerId(Long viewerId, Long ownerId);

    void deleteByViewerIdAndOwnerId(Long viewerId, Long ownerId);

    @Query("""
        SELECT
            fm.familyMemberId AS familyMemberId,
            fm.ownerId AS ownerId,
            u.fullName AS memberName
        FROM FamilyMember fm
        JOIN User u ON u.id = fm.ownerId
        WHERE fm.viewerId = :viewerId
        ORDER BY u.fullName ASC
    """)
    List<FamilyMemberListProjection> findAllFamilyMembersByViewerId(Long viewerId);
}

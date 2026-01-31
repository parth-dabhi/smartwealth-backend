package com.smartwealth.smartwealth_backend.repository.sip;

import com.smartwealth.smartwealth_backend.entity.investment.SipMandate;
import com.smartwealth.smartwealth_backend.entity.enums.SipStatus;
import com.smartwealth.smartwealth_backend.repository.sip.projection.SipMandateResponseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SipMandateRepository extends JpaRepository<SipMandate, Long> {

    @Query("""
    SELECT
        s.sipMandateId AS sipMandateId,
        s.planId AS planId,
        s.status AS status,
        s.sipAmount AS sipAmount,
        s.sipDay AS sipDay,
        s.totalInstallments AS totalInstallments,
        s.completedInstallments AS completedInstallments,
        s.startDate AS startDate,
        s.endDate AS endDate,
        s.nextRunAt AS nextRunAt
    FROM SipMandate s
    WHERE s.userId = :userId
    ORDER BY s.createdAt DESC
""")
    List<SipMandateResponseProjection> findAllSipMandatesByUserId(
            @Param("userId") Long userId
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    UPDATE SipMandate s
    SET s.status = :status,
        s.updatedAt = :updatedAt,
        s.nextRunAt = :nextRunAt
    WHERE s.sipMandateId = :sipMandateId
      AND s.userId = :userId
""")
    int updateSipStatusBySipMandateIdAndUserId(
            @Param("sipMandateId") Long sipMandateId,
            @Param("userId") Long userId,
            @Param("status") SipStatus status,
            @Param("nextRunAt") OffsetDateTime nextRunAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    <T> Optional<T> findBySipMandateIdAndUserId(
            Long sipMandateId,
            Long userId,
            Class<T> type
    );

    // for future scheduler use
    @Query("""
    SELECT s
    FROM SipMandate s
    WHERE s.status = :status
      AND s.nextRunAt <= :now
""")
    List<SipMandate> findDueSipMandatesByStatusAndNextRunAt(
            @Param("status") SipStatus status,
            @Param("now") OffsetDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE SipMandate s
    SET s.status = 'SUSPENDED',
        s.nextRunAt = null
    WHERE s.sipMandateId = :sipMandateId
        AND s.userId = :userId
""")
    int suspendSip(
            @Param("sipMandateId") Long sipMandateId,
            @Param("userId") Long userId
    );
}


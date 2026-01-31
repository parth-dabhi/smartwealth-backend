package com.smartwealth.smartwealth_backend.repository.investment;

import com.smartwealth.smartwealth_backend.entity.enums.FailureReasonType;
import com.smartwealth.smartwealth_backend.entity.enums.PaymentStatus;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.entity.enums.OrderStatus;
import com.smartwealth.smartwealth_backend.repository.investment.projection.OrderHistoryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface InvestmentOrderRepository extends JpaRepository<InvestmentOrder, Long> {

    @Query("""
        SELECT o
        FROM InvestmentOrder o
        WHERE o.status = :status
          AND o.applicableNavDate <= :navDate
    """)
    List<InvestmentOrder> findByStatus(
            @Param("status") OrderStatus status,
            @Param("navDate") LocalDate navDate
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE InvestmentOrder o
        SET o.status = :status,
            o.paymentStatus = :paymentStatus,
            o.paymentReferenceId = :paymentReferenceId,
            o.failureReasonType = :failureReasonType,
            o.failureReason = :failureReason,
            o.failedAt = :failedAt,
            o.updatedAt = :updatedAt
        WHERE o.investmentOrderId = :orderId
    """)
    int markOrderAsFailed(
            @Param("orderId") Long orderId,
            @Param("status") OrderStatus status,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("paymentReferenceId") String paymentReferenceId,
            @Param("failureReasonType") FailureReasonType failureReasonType,
            @Param("failureReason") String failureReason,
            @Param("failedAt") OffsetDateTime failedAt,
            @Param("updatedAt") OffsetDateTime updatedAt
    );

    @Query("""
    SELECT
        io.investmentOrderId AS investmentOrderId,
        sp.planName AS planName,
        io.investmentType AS investmentType,
        io.investmentMode AS investmentMode,
        io.status AS orderStatus,
        io.paymentStatus AS paymentStatus,
        ht.units AS units,
        io.amount AS amount,
        ht.nav AS nav,
        ht.navDate AS navDate,
        io.orderTime AS orderTime
    FROM InvestmentOrder io
    JOIN SchemePlan sp
         ON io.planId = sp.planId
    LEFT JOIN HoldingTransaction ht
         ON ht.investmentOrderId = io.investmentOrderId
    WHERE io.userId = :userId
    ORDER BY io.orderTime DESC
""")
    Page<OrderHistoryProjection> findOrderHistory(
            @Param("userId") Long userId,
            Pageable pageable
    );


}

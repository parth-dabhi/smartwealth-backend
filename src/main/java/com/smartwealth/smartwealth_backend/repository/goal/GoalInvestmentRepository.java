package com.smartwealth.smartwealth_backend.repository.goal;

import com.smartwealth.smartwealth_backend.entity.goal.GoalInvestment;
import com.smartwealth.smartwealth_backend.repository.goal.projection.GoalInvestmentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoalInvestmentRepository extends JpaRepository<GoalInvestment, Long> {

    List<GoalInvestmentProjection> findAllByGoalId(Long goalId);

    @Query("SELECT gi.goalId FROM GoalInvestment gi WHERE gi.sipMandateId = :sipMandateId AND gi.planId = :planId")
    Optional<Long> findGoalIdBySipMandateIdAndPlanId(
            @Param("sipMandateId") Long sipMandateId,
            @Param("planId") Integer planId
    );

    @Query("SELECT gi.goalId FROM GoalInvestment gi WHERE gi.investmentOrderId = :orderId AND gi.planId = :planId")
    Optional<Long> findGoalIdByInvestmentOrderIdAndPlanId(
            @Param("orderId") Long investmentOrderId,
            @Param("planId") Integer planId
    );

    @Modifying
    @Query("UPDATE GoalInvestment gi SET gi.holdingId = :holdingId WHERE gi.investmentOrderId = :orderId AND gi.planId = :planId")
    void updateHoldingIdByInvestmentOrderIdAndPlanId(
            @Param("holdingId") Long holdingId,
            @Param("orderId") Long investmentOrderId,
            @Param("planId") Integer planId
    );

    @Modifying
    @Query("UPDATE GoalInvestment gi SET gi.holdingId = :holdingId WHERE gi.sipMandateId = :sipMandateId AND gi.planId = :planId")
    void updateHoldingIdBySipMandateIdAndPlanId(
            @Param("holdingId") Long holdingId,
            @Param("sipMandateId") Long sipMandateId,
            @Param("planId") Integer planId
    );
}

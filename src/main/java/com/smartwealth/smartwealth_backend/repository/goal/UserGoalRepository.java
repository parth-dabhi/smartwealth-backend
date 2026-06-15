package com.smartwealth.smartwealth_backend.repository.goal;

import com.smartwealth.smartwealth_backend.entity.enums.GoalStatus;
import com.smartwealth.smartwealth_backend.entity.goal.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {

    Optional<UserGoal> findByGoalIdAndUserId(Long goalId, Long userId);

    boolean existsByGoalIdAndUserId(Long goalId, Long userId);

    @Query("""
        SELECT g
        FROM UserGoal g
        WHERE g.userId = :userId
          AND g.status = :status
    """)
    List<UserGoal> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") GoalStatus status
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE UserGoal g
        SET g.status = :status,
            g.updatedAt = CURRENT_TIMESTAMP
        WHERE g.goalId = :goalId
          AND g.userId = :userId
    """)
    void updateGoalStatusByUser(
            @Param("goalId") Long goalId,
            @Param("userId") Long userId,
            @Param("status") GoalStatus status
    );

    @Modifying
    @Query("""
    UPDATE UserGoal g
    SET g.totalInvested = g.totalInvested + :amount,
        g.updatedAt     = CURRENT_TIMESTAMP
    WHERE g.goalId = :goalId
""")
    int updateTrackingOnAllotment(
            @Param("goalId") Long goalId,
            @Param("amount") BigDecimal amount
    );
}

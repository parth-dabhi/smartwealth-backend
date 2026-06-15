package com.smartwealth.smartwealth_backend.entity.goal;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "goal_investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "sip_mandate_id")
    private Long sipMandateId;

    @Column(name = "investment_order_id")
    private Long investmentOrderId;

    @Column(name = "holding_id")
    private Long holdingId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}

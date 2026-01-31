package com.smartwealth.smartwealth_backend.entity.mutual_fund;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "scheme_plans",
        indexes = {
                @Index(name = "idx_plan_scheme_id", columnList = "scheme_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SchemePlan {

    @Id
    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "scheme_id", nullable = false)
    private Integer schemeId;

    @Column(name = "isin", nullable = false, unique = true, length = 20)
    private String isin;

    @Column(name = "plan_name", nullable = false, length = 255)
    private String planName;

    @Column(name = "scheme_code")
    private Integer schemeCode;

    @Column(name = "plan_type", nullable = false, length = 50)
    private String planType;

    @Column(name = "option_type_id", nullable = false)
    private Integer optionTypeId;

    @Column(name = "expense_ratio", precision = 5, scale = 2)
    private BigDecimal expenseRatio;

    @Column(name = "min_investment", precision = 12, scale = 2)
    private BigDecimal minInvestment;

    @Column(name = "min_sip", precision = 12, scale = 2)
    private BigDecimal minSip;

    @Column(name = "return_1y", precision = 10, scale = 4)
    private BigDecimal return1y;

    @Column(name = "return_3y", precision = 10, scale = 4)
    private BigDecimal return3y;

    @Column(name = "return_5y", precision = 10, scale = 4)
    private BigDecimal return5y;

    @Column(name = "is_sip_allowed", nullable = false)
    private Boolean isSipAllowed = false;

    @Column(name = "exit_load", columnDefinition = "TEXT")
    private String exitLoad;

    @Column(name = "is_recommended", nullable = false)
    private Boolean isRecommended = false;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}

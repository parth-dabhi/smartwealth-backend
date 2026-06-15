package com.smartwealth.smartwealth_backend.dto.request.goal;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGoalRequest {

    @NotNull
    @Size(min = 1, max = 200)
    private String goalName;

    @NotNull
    @DecimalMin(value = "50000.00", message = "Target amount must be > = 50,000", inclusive = false)
    @DecimalMax(value = "1000000000.00", message = "Target amount must be < = 1,000,000,000", inclusive = false)
    private BigDecimal targetAmount;

    @NotNull
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 30, message = "Duration cannot exceed 50 years")
    private Integer durationYears;
}

package com.smartwealth.smartwealth_backend.dto.response.goal;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoalInvestmentResult {

    private int successSip;
    private int failedSip;

    private int successLumpsum;
    private int failedLumpsum;

    private List<SchemeExecutionResult> results;
}


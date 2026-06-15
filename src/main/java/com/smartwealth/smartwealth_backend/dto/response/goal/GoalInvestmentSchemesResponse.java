package com.smartwealth.smartwealth_backend.dto.response.goal;

import com.smartwealth.smartwealth_backend.dto.response.investment.PlanPortfolioResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoalInvestmentSchemesResponse {

    List<PlanPortfolioResponse> investments;
}

package com.smartwealth.smartwealth_backend.controller.goal;

import com.smartwealth.smartwealth_backend.dto.request.goal.CreateGoalRequest;
import com.smartwealth.smartwealth_backend.dto.request.goal.InvestInRecommendedSchemesRequest;
import com.smartwealth.smartwealth_backend.dto.request.goal.UpdateGoalRequest;
import com.smartwealth.smartwealth_backend.dto.response.goal.DetailGoalResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.GoalInvestmentResult;
import com.smartwealth.smartwealth_backend.dto.response.goal.GoalInvestmentSchemesResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.GoalResponse;
import com.smartwealth.smartwealth_backend.service.goal.UserGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Slf4j
public class UserGoalController {

    private final UserGoalService goalService;

    // Create Goal
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody CreateGoalRequest request
    ) {
        GoalResponse response = goalService.createGoal(customerId, request);
        return ResponseEntity.ok(response);
    }

    // Get all goals of user
    @GetMapping
    public ResponseEntity<List<GoalResponse>> getUserGoals(
            @AuthenticationPrincipal String customerId
    ) {
        return ResponseEntity.ok(goalService.getUserGoals(customerId));
    }

    // Get single goal
    @GetMapping("/{goalId}")
    public ResponseEntity<DetailGoalResponse> getGoal(
            @AuthenticationPrincipal String customerId,
            @PathVariable Long goalId
    ) {
        return ResponseEntity.ok(
                goalService.getGoal(customerId, goalId)
        );
    }

    // Update goal
    @PutMapping("/{goalId}")
    public ResponseEntity<GoalResponse> updateGoal(
            @AuthenticationPrincipal String customerId,
            @PathVariable Long goalId,
            @Valid @RequestBody UpdateGoalRequest request
    ) {
        return ResponseEntity.ok(
                goalService.updateGoal(customerId, goalId, request)
        );
    }

    @PostMapping("/invest")
    public ResponseEntity<GoalInvestmentResult> investInGoal(
            @AuthenticationPrincipal String customerId,
            @RequestParam Long goalId,
            @RequestBody InvestInRecommendedSchemesRequest request

    ) {
        return ResponseEntity.ok(
                goalService.investInGoal(
                        customerId,
                        goalId,
                        request
                )
        );
    }
}

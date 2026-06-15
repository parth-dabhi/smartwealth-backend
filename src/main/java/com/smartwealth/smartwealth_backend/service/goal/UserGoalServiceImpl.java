package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.common.RecommendedLumpsumSchemeDto;
import com.smartwealth.smartwealth_backend.dto.common.RecommendedSipSchemeDto;
import com.smartwealth.smartwealth_backend.dto.request.goal.CreateGoalRequest;
import com.smartwealth.smartwealth_backend.dto.request.goal.InvestInRecommendedSchemesRequest;
import com.smartwealth.smartwealth_backend.dto.request.goal.UpdateGoalRequest;
import com.smartwealth.smartwealth_backend.dto.request.investment.CreateSipMandateRequest;
import com.smartwealth.smartwealth_backend.dto.request.investment.InvestmentBuyRequest;
import com.smartwealth.smartwealth_backend.dto.response.goal.*;
import com.smartwealth.smartwealth_backend.dto.response.investment.InvestmentBuyResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.PlanPortfolioResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.SipMandateResponse;
import com.smartwealth.smartwealth_backend.entity.enums.GoalStatus;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.goal.GoalInvestment;
import com.smartwealth.smartwealth_backend.entity.goal.UserGoal;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.exception.goal.GoalNotFoundException;
import com.smartwealth.smartwealth_backend.repository.goal.UserGoalRepository;
import com.smartwealth.smartwealth_backend.service.investment.InvestmentService;
import com.smartwealth.smartwealth_backend.service.sip.SipMandateService;
import com.smartwealth.smartwealth_backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserGoalServiceImpl implements UserGoalService {

    private final UserService               userService;
    private final UserGoalRepository        userGoalRepository;
    private final SipMandateService         sipMandateService;
    private final InvestmentService         investmentService;
    private final GoalInvestmentService     goalInvestmentService;

    @Override
    public GoalResponse createGoal(
            String customerId,
            CreateGoalRequest request
    ) {

        log.info("Creating goal for customerId={}", customerId);

        Long userId = getUserId(customerId);

        UserGoal goal = new UserGoal();
        goal.setUserId(userId);
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDurationYears(request.getDurationYears());
        goal.setExpectedReturn(BigDecimal.valueOf(12)); // TODO: placeholder, will be updated when user invests and we calculate based on risk+duration
        goal.setCurrentValue(BigDecimal.ZERO);
        goal.setTotalInvested(BigDecimal.ZERO);
        goal.setStatus(GoalStatus.CREATED);
        goal.setCreatedAt(OffsetDateTime.now());
        goal.setUpdatedAt(OffsetDateTime.now());

        userGoalRepository.save(goal);

        log.info("Goal created: goalId={}, userId={}", goal.getGoalId(), userId);

        return GoalResponse.fromEntity(goal);
    }

    // only fetching Active goals.
    @Override
    public List<GoalResponse> getUserGoals(
            String customerId
    ) {
        Long userId = getUserId(customerId);
        return userGoalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE).stream()
                .map(GoalResponse::fromEntity)
                .toList();
    }

    @Override
    public DetailGoalResponse getGoal(
            String customerId,
            Long goalId
    ) {
        Long userId = getUserId(customerId);
        UserGoal goal = userGoalRepository.findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        List<PlanPortfolioResponse> investments = getGoalInvestmentSchemes(customerId, goalId).getInvestments();

        // Sum marketValue across all linked plan holdings — same logic as PortfolioServiceImpl
        BigDecimal currentValue = investments.stream()
                .map(PlanPortfolioResponse::getMarketValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String progress = calculateProgress(currentValue, goal.getTargetAmount());

        return DetailGoalResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .durationYears(goal.getDurationYears())
                .currentValue(currentValue)
                .totalInvested(goal.getTotalInvested())
                .status(goal.getStatus())
                .progress(progress)
                .investments(investments)
                .build();
    }

    @Override
    public GoalResponse updateGoal(
            String customerId,
            Long goalId,
            UpdateGoalRequest request
    ) {

        log.info("Updating goalId={} for customerId={}", goalId, customerId);

        Long userId = getUserId(customerId);
        UserGoal goal = userGoalRepository.findByGoalIdAndUserId(goalId, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (request.getGoalName() != null)     goal.setGoalName(request.getGoalName());
        if (request.getTargetAmount() != null)  goal.setTargetAmount(request.getTargetAmount());
        if (request.getDurationYears() != null) goal.setDurationYears(request.getDurationYears());

        goal.setUpdatedAt(OffsetDateTime.now());
        userGoalRepository.save(goal);

        return GoalResponse.fromEntity(goal);
    }

    // INVEST IN GOAL
    @Override
    public GoalInvestmentResult investInGoal(
            String customerId,
            Long goalId,
            InvestInRecommendedSchemesRequest request
    ) {
        Long userId = getUserId(customerId);

        // Validate goal exists and belongs to this user
        if (!userGoalRepository.existsByGoalIdAndUserId(goalId, userId)) {
            throw new GoalNotFoundException("Goal not found");
        }

        log.info("investInGoal: goalId={}, userId={}, sips={}, lumpsums={}",
                goalId, userId,
                request.getSipSchemes() != null ? request.getSipSchemes().size() : 0,
                request.getLumpsumSchemes() != null ? request.getLumpsumSchemes().size() : 0);

        int successSip = 0, failedSip = 0;
        int successLump = 0, failedLump = 0;

        List<SchemeExecutionResult> results = new ArrayList<>();

        // SIP
        if (request.getSipSchemes() != null) {
            for (RecommendedSipSchemeDto sip : request.getSipSchemes()) {
                try {
                    SipMandateResponse sipResponse = sipMandateService.createSip(
                            customerId,
                            new CreateSipMandateRequest(
                                    sip.getPlanId(),
                                    null,   // folioNumber = null means create new folio for this SIP
                                    sip.getSipAmount(),
                                    sip.getSipDay(),
                                    sip.getInstallments()
                            )
                    );

                    successSip++;
                    results.add(SchemeExecutionResult.builder()
                            .planId(sip.getPlanId())
                            .schemeName(sip.getSchemeName())
                            .sipMandateId(sipResponse.getSipMandateId())
                            .sipAmount(sip.getSipAmount())
                            .type(InvestmentMode.SIP)
                            .status(TransactionStatus.SUCCESS)
                            .build());

                } catch (Exception ex) {
                    failedSip++;
                    results.add(SchemeExecutionResult.builder()
                            .planId(sip.getPlanId())
                            .schemeName(sip.getSchemeName())
                            .type(InvestmentMode.SIP)
                            .status(TransactionStatus.FAILED)
                            .error(ex.getMessage())
                            .build());
                    log.error("SIP failed: goalId={} planId={} — {}", goalId, sip.getPlanId(), ex.getMessage());
                }
            }
        }

        // Lumpsum

        if (request.getLumpsumSchemes() != null) {
            for (RecommendedLumpsumSchemeDto lump : request.getLumpsumSchemes()) {
                try {
                    // Idempotency key: goal + plan ensures no double-buy if retried
                    String idempotencyKey = "goal-" + goalId + "-plan-" + lump.getPlanId();

                    InvestmentBuyResponse buyResponse = investmentService.buy(
                            new InvestmentBuyRequest(
                                    lump.getPlanId(),
                                    null,   // folioNumber = null means create new folio for this buy Lumpsum
                                    lump.getLumpsumAmount()
                            ),
                            customerId,
                            idempotencyKey
                    );

                    successLump++;
                    results.add(SchemeExecutionResult.builder()
                            .planId(lump.getPlanId())
                            .schemeName(lump.getSchemeName())
                            .investmentOrderId(buyResponse.getInvestmentOrderId())
                            .lumpsumAmount(lump.getLumpsumAmount())
                            .type(InvestmentMode.LUMPSUM)
                            .status(TransactionStatus.SUCCESS)
                            .build());

                    log.info("Lumpsum linked: goalId={} orderId={} amount={}",
                            goalId, buyResponse.getInvestmentOrderId(), lump.getLumpsumAmount());

                } catch (Exception ex) {
                    failedLump++;
                    results.add(SchemeExecutionResult.builder()
                            .planId(lump.getPlanId())
                            .schemeName(lump.getSchemeName())
                            .type(InvestmentMode.LUMPSUM)
                            .status(TransactionStatus.FAILED)
                            .error(ex.getMessage())
                            .build());
                    log.error("Lumpsum failed: goalId={} planId={} — {}", goalId, lump.getPlanId(), ex.getMessage());
                }
            }
        }

        // Update goal status

        int totalSuccess = successSip + successLump;
        int totalFailed  = failedSip  + failedLump;

        if (totalSuccess > 0) {

            List<GoalInvestment> investments = getGoalInvestments(results);

            if (!investments.isEmpty()) {
                goalInvestmentService.linkInvestmentsBulk(goalId, investments);
            }

            userGoalRepository.updateGoalStatusByUser(
                    goalId, userId, GoalStatus.ACTIVE
            );
        }

        log.info("investInGoal complete: goalId={} successSip={} failedSip={} successLump={} failedLump={}",
                goalId, successSip, failedSip, successLump, failedLump);

        if (totalFailed > 0 && totalSuccess == 0) {
            log.error("All investments failed for goalId={}", goalId);
        }

        return GoalInvestmentResult.builder()
                .successSip(successSip)
                .failedSip(failedSip)
                .successLumpsum(successLump)
                .failedLumpsum(failedLump)
                .results(results)
                .build();
    }

    /**
     * Called after a successful BUY allotment.
     * Checks if the order's plan/SIP is linked to a goal
     * and updates total_invested + current_value accordingly.
     */
    public void updateGoalOnAllotment(
            InvestmentOrder order,
            BigDecimal amount
    ) {
        try {
            Optional<Long> goalId = goalInvestmentService.resolveGoalId(order);

            if (goalId.isEmpty()) {
                // Not linked to any goal — nothing to update
                return;
            }

            goalInvestmentService.addHoldingIdToGoalInvestment(order, order.getHoldingId());
            int updated = userGoalRepository.updateTrackingOnAllotment(goalId.get(), amount);

            // update

            if (updated > 0) {
                log.info(
                        "Goal tracking updated. goalId={}, orderId={}, amount={}",
                        goalId.get(),
                        order.getInvestmentOrderId(),
                        amount
                );
            } else {
                log.warn(
                        "Goal not found for tracking update. goalId={}, orderId={}",
                        goalId.get(),
                        order.getInvestmentOrderId()
                );
            }

        } catch (Exception ex) {
            log.error(
                    "Goal tracking update failed (non-fatal). orderId={}",
                    order.getInvestmentOrderId(),
                    ex
            );

            // Continue without throwing, as this is a non-critical operation.
        }
    }

    private List<GoalInvestment> getGoalInvestments(
            List<SchemeExecutionResult> results
    ) {
        List<GoalInvestment> investments = new ArrayList<>();

        for (SchemeExecutionResult r : results) {

            if (r.getStatus() != TransactionStatus.SUCCESS) continue;

            GoalInvestment gi = new GoalInvestment();

            if (r.getType() == InvestmentMode.SIP) {
                gi.setSipMandateId(r.getSipMandateId());
            } else if (r.getType() == InvestmentMode.LUMPSUM) {
                gi.setInvestmentOrderId(r.getInvestmentOrderId());
            }

            gi.setPlanId(r.getPlanId());
            investments.add(gi);
        }
        return investments;
    }

    private String calculateProgress(BigDecimal currentValue, BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }
        return currentValue
                .multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private GoalInvestmentSchemesResponse getGoalInvestmentSchemes(
            String customerId,
            Long goalId
    ) {
        return goalInvestmentService.getGoalInvestmentSchemes(goalId, customerId);
    }

    private Long getUserId(
            String customerId
    ) {
        return userService.getUserByCustomerId(customerId).getId();
    }
}

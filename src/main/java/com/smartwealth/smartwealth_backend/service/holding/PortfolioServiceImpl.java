package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.dto.response.investment.*;
import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.HoldingNotFoundException;
import com.smartwealth.smartwealth_backend.repository.holding.HoldingTransactionRepository;
import com.smartwealth.smartwealth_backend.repository.holding.UserHoldingRepository;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingPortfolioProjection;
import com.smartwealth.smartwealth_backend.service.nav.NavHistoryService;
import com.smartwealth.smartwealth_backend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final UserService userService;
    private final UserHoldingRepository userHoldingRepository;
    private final NavHistoryService navHistoryService;
    private final HoldingTransactionRepository holdingTransactionRepository;

    // TOTAL PORTFOLIO
    @Override
//    @Cacheable(value = "portfolioSummary", key = "#customerId", unless = "#result == null")
    public PortfolioSummaryResponse getPortfolio(
            String customerId
    ) {

        Long userId = getUserId(customerId);

        List<UserHoldingPortfolioProjection> holdings =
                userHoldingRepository.findPortfolioByUserId(userId)
                        .orElseThrow(() -> new HoldingNotFoundException("No holdings found for user"));

        BigDecimal totalNetInvestedAmount = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;

        Set<Integer> planIds = holdings.stream()
                .map(UserHoldingPortfolioProjection::getPlanId)
                .collect(Collectors.toSet());

        Map<Integer, BigDecimal> navMap =
                navHistoryService.getLatestNavForPlans(planIds);

        Map<String, BigDecimal> assetMarketValueMap = new HashMap<>();
        List<PortfolioHoldingResponse> holdingResponses = new ArrayList<>();

        for (UserHoldingPortfolioProjection h : holdings) {

            BigDecimal nav = navMap.get(h.getPlanId());
            BigDecimal marketValue = calculateMarketValue(h.getTotalUnits(), nav);

            BigDecimal netInvested = h.getTotalInvestedAmount().subtract(h.getTotalRedeemedAmount());

            BigDecimal gain = calculateGain(netInvested, marketValue);
            String gainPercentage = calculateNetGainPercentage(gain, netInvested);

            totalNetInvestedAmount = totalNetInvestedAmount.add(netInvested);
            totalMarketValue = totalMarketValue.add(marketValue);

            assetMarketValueMap.merge(
                    h.getAssetName(),
                    marketValue,
                    BigDecimal::add
            );

            holdingResponses.add(
                    PortfolioHoldingResponse.builder()
                            .planId(h.getPlanId())
                            .planName(h.getPlanName())
                            .amcName(h.getAmcName())
                            .assetName(h.getAssetName())
                            .categoryName(h.getCategoryName())
                            .netInvestedAmount(netInvested)
                            .marketValue(marketValue)
                            .gain(gain)
                            .gainPercentage(gainPercentage)
                            .build()
            );
        }

        BigDecimal totalNetGain =
                calculateGain(totalNetInvestedAmount, totalMarketValue);

        String totalNetGainPercentage =
                calculateNetGainPercentage(totalNetGain, totalNetInvestedAmount);

        List<AssetAllocationResponse> assetAllocations =
                buildAssetAllocations(assetMarketValueMap, totalMarketValue);

        return PortfolioSummaryResponse.builder()
                .totalNetInvestedAmount(totalNetInvestedAmount)
                .totalMarketValue(totalMarketValue)
                .totalNetGain(totalNetGain)
                .totalNetGainPercentage(totalNetGainPercentage)
                .assetAllocation(assetAllocations)
                .holdings(holdingResponses)
                .build();
    }

    // SINGLE PLAN PORTFOLIO
    @Override
    public PlanPortfolioResponse getPlanPortfolio(
            String customerId,
            Integer planId
    ) {

        Long userId = getUserId(customerId);

        UserHoldingPortfolioProjection holding = userHoldingRepository
                        .findByUserAndPlan(userId, planId)
                        .orElseThrow(() -> new HoldingNotFoundException("No holding found"));

        LatestNavDto nav = navHistoryService.getLatestNav(planId);
        BigDecimal netInvested = holding.getTotalInvestedAmount().subtract(holding.getTotalRedeemedAmount());
        BigDecimal marketValue = calculateMarketValue(holding.getTotalUnits(), nav.getNavValue());
        BigDecimal gain = calculateGain(netInvested, marketValue);

        return PlanPortfolioResponse.builder()
                .planId(planId)
                .planName(holding.getPlanName())
                .amcName(holding.getAmcName())
                .assetName(holding.getAssetName())
                .categoryName(holding.getCategoryName())
                .investedAmount(netInvested)
                .marketValue(marketValue)
                .gain(gain)
                .units(holding.getTotalUnits())
                .latestNav(nav.getNavValue())
                .navDate(nav.getNavDate())
                .isActive(holding.getIsActive())
                .build();
    }

    // TRANSACTION HISTORY
    @Override
    public List<HoldingTransactionResponse> getHoldingTransactions(
            String customerId,
            Integer planId
    ) {

        Long userId = getUserId(customerId);

        UserHoldingPortfolioProjection holding = userHoldingRepository
                        .findByUserAndPlan(userId, planId)
                        .orElseThrow(() -> new HoldingNotFoundException("No holding found"));

        return holdingTransactionRepository
                .findByHoldingId(holding.getHoldingId())
                .stream()
                .map(tx ->
                        HoldingTransactionResponse.builder()
                                .type(tx.getTxnType())
                                .investmentMode(tx.getInvestmentMode())
                                .units(tx.getUnits())
                                .amount(tx.getAmount())
                                .nav(tx.getNav())
                                .navDate(tx.getNavDate())
                                .transactionDate(tx.getTransactionDate())
                                .build()
                )
                .toList();
    }

    private Long getUserId(String customerId) {
        return userService.getUserIdByCustomerId(customerId);
    }

    private BigDecimal calculateMarketValue(
            BigDecimal totalUnits,
            BigDecimal navValue
    ) {
        return totalUnits
                .multiply(navValue)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateGain(
            BigDecimal netInvested,
            BigDecimal marketValue
    ) {
        return marketValue
                .subtract(netInvested)
                .setScale(2, RoundingMode.HALF_UP);

    }

    private List<AssetAllocationResponse> buildAssetAllocations(
            Map<String, BigDecimal> assetMarketValueMap,
            BigDecimal totalMarketValue
    ) {

        List<AssetAllocationResponse> assetAllocations = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : assetMarketValueMap.entrySet()) {

            String percentage =
                    totalMarketValue.compareTo(BigDecimal.ZERO) == 0
                            ? "0%"
                            : entry.getValue()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalMarketValue, 2, RoundingMode.HALF_UP) + "%";

            assetAllocations.add(
                    AssetAllocationResponse.builder()
                            .assetName(entry.getKey())
                            .marketValue(entry.getValue())
                            .percentage(percentage)
                            .build()
            );
        }

        assetAllocations.sort(
                Comparator.comparing(AssetAllocationResponse::getMarketValue).reversed()
        );

        return assetAllocations;
    }

    private String calculateNetGainPercentage(
            BigDecimal totalNetGain,
            BigDecimal totalNetInvestedAmount
    ) {

        if (totalNetInvestedAmount == null || totalNetInvestedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }

        return totalNetGain
                .multiply(BigDecimal.valueOf(100))
                .divide(totalNetInvestedAmount, 2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }
}

package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.dto.common.CombinedTotals;
import com.smartwealth.smartwealth_backend.dto.common.PortfolioData;
import com.smartwealth.smartwealth_backend.dto.response.family_member.FamilyPortfolioSummaryResponse;
import com.smartwealth.smartwealth_backend.dto.response.family_member.IndividualPortfolioResponse;
import com.smartwealth.smartwealth_backend.dto.response.investment.*;
import com.smartwealth.smartwealth_backend.dto.response.nav.LatestNavDto;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.HoldingNotFoundException;
import com.smartwealth.smartwealth_backend.repository.family_member.FamilyMemberRepository;
import com.smartwealth.smartwealth_backend.repository.family_member.projection.FamilyMemberListProjection;
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
    private final FamilyMemberRepository familyMemberRepository;

    // PERSONAL PORTFOLIO
    @Override
    public PortfolioSummaryResponse getPortfolio(String customerId) {
        Long userId = getUserId(customerId);
        List<UserHoldingPortfolioProjection> holdings = getHoldingsForUser(userId);
        return buildPortfolioSummary(holdings);
    }

    // FAMILY PORTFOLIO (COMBINED)
    @Override
    public FamilyPortfolioSummaryResponse getFamilyPortfolio(String viewerCustomerId) {
        Long viewerId = getUserId(viewerCustomerId);

        log.info("Building combined family portfolio for viewer={}", viewerCustomerId);

        // Get personal portfolio
        IndividualPortfolioResponse personalPortfolio = buildIndividualPortfolio(
                viewerId,
                "You",
                true
        );

        // Get all family member portfolios
        List<IndividualPortfolioResponse> familyMemberPortfolios = buildFamilyMemberPortfolios(viewerId);

        // Calculate combined totals
        CombinedTotals combinedTotals = calculateCombinedTotals(personalPortfolio, familyMemberPortfolios);

        return FamilyPortfolioSummaryResponse.builder()
                .totalNetInvestedAmount(combinedTotals.totalInvested())
                .totalMarketValue(combinedTotals.totalMarketValue())
                .totalNetGain(combinedTotals.totalGain())
                .totalNetGainPercentage(combinedTotals.gainPercentage())
                .assetAllocation(combinedTotals.assetAllocation())
                .personalPortfolio(personalPortfolio)
                .familyMemberPortfolios(familyMemberPortfolios)
                .build();
    }

    // PLAN PORTFOLIO
    @Override
    public PlanPortfolioResponse getPlanPortfolio(String customerId, Integer planId) {
        Long userId = getUserId(customerId);
        UserHoldingPortfolioProjection holding = getHoldingByUserAndPlan(userId, planId);
        return buildPlanPortfolioResponse(holding);
    }

    //  TRANSACTION HISTORY
    @Override
    public List<HoldingTransactionResponse> getHoldingTransactions(String customerId, Integer planId) {
        Long userId = getUserId(customerId);
        UserHoldingPortfolioProjection holding = getHoldingByUserAndPlan(userId, planId);
        return buildTransactionList(holding.getHoldingId());
    }

    // PRIVATE BUILDER METHODS

    /**
     * Build individual portfolio for a user (personal or family member)
     */
    private IndividualPortfolioResponse buildIndividualPortfolio(
            Long userId,
            String fullName,
            boolean isPersonal
    ) {
        List<UserHoldingPortfolioProjection> holdings = getHoldingsForUser(userId);
        PortfolioData portfolioData = calculatePortfolioData(holdings);

        return IndividualPortfolioResponse.builder()
                .ownerName(fullName)
                .isPersonal(isPersonal)
                .totalNetInvestedAmount(portfolioData.totalNetInvestedAmount())
                .totalMarketValue(portfolioData.totalMarketValue())
                .totalNetGain(portfolioData.totalNetGain())
                .totalNetGainPercentage(portfolioData.totalNetGainPercentage())
                .assetAllocation(portfolioData.assetAllocations())
                .holdings(portfolioData.holdingResponses())
                .build();
    }

    /**
     * Build portfolio summary (backward compatibility)
     */
    private PortfolioSummaryResponse buildPortfolioSummary(List<UserHoldingPortfolioProjection> holdings) {
        PortfolioData portfolioData = calculatePortfolioData(holdings);

        return PortfolioSummaryResponse.builder()
                .totalNetInvestedAmount(portfolioData.totalNetInvestedAmount())
                .totalMarketValue(portfolioData.totalMarketValue())
                .totalNetGain(portfolioData.totalNetGain())
                .totalNetGainPercentage(portfolioData.totalNetGainPercentage())
                .assetAllocation(portfolioData.assetAllocations())
                .holdings(portfolioData.holdingResponses())
                .build();
    }

    /**
     * Build all family member portfolios for a viewer
     */
    private List<IndividualPortfolioResponse> buildFamilyMemberPortfolios(Long viewerId) {
        List<IndividualPortfolioResponse> familyMemberPortfolios = new ArrayList<>();

        List<FamilyMemberListProjection> familyMemberListProjections = familyMemberRepository
                .findAllFamilyMembersByViewerId(viewerId);

        for (FamilyMemberListProjection memberProj : familyMemberListProjections) {
            Long memberId = memberProj.getOwnerId();
            try {
                IndividualPortfolioResponse memberPortfolio = buildIndividualPortfolio(
                        memberId,
                        memberProj.getMemberName(),
                        false
                );
                familyMemberPortfolios.add(memberPortfolio);
            } catch (HoldingNotFoundException e) {
                log.warn("No holdings found for family member={}", memberId);
            }
        }

        return familyMemberPortfolios;
    }

    /**
     * Build plan portfolio response
     */
    private PlanPortfolioResponse buildPlanPortfolioResponse(UserHoldingPortfolioProjection holding) {
        LatestNavDto nav = navHistoryService.getLatestNav(holding.getPlanId());
        BigDecimal netInvested = holding.getTotalInvestedAmount().subtract(holding.getTotalRedeemedAmount());
        BigDecimal marketValue = calculateMarketValue(holding.getTotalUnits(), nav.getNavValue());
        BigDecimal gain = calculateGain(netInvested, marketValue);

        return PlanPortfolioResponse.builder()
                .planId(holding.getPlanId())
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

    /**
     * Build transaction list from holding ID
     */
    private List<HoldingTransactionResponse> buildTransactionList(Long holdingId) {
        return holdingTransactionRepository
                .findByHoldingId(holdingId)
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

    // CORE CALCULATION METHODS

    /**
     * Calculate all portfolio data from holdings
     * This is the main generic method that eliminates duplicate calculation logic
     */
    private PortfolioData calculatePortfolioData(List<UserHoldingPortfolioProjection> holdings) {
        BigDecimal totalNetInvestedAmount = BigDecimal.ZERO;
        BigDecimal totalMarketValue = BigDecimal.ZERO;

        Set<Integer> planIds = holdings.stream()
                .map(UserHoldingPortfolioProjection::getPlanId)
                .collect(Collectors.toSet());

        Map<Integer, BigDecimal> navMap = navHistoryService.getLatestNavForPlans(planIds);
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

            assetMarketValueMap.merge(h.getAssetName(), marketValue, BigDecimal::add);

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

        BigDecimal totalNetGain = calculateGain(totalNetInvestedAmount, totalMarketValue);
        String totalNetGainPercentage = calculateNetGainPercentage(totalNetGain, totalNetInvestedAmount);
        List<AssetAllocationResponse> assetAllocations = buildAssetAllocations(assetMarketValueMap, totalMarketValue);

        return new PortfolioData(
                totalNetInvestedAmount,
                totalMarketValue,
                totalNetGain,
                totalNetGainPercentage,
                assetAllocations,
                holdingResponses
        );
    }

    /**
     * Calculate combined totals from personal and family member portfolios
     */
    private CombinedTotals calculateCombinedTotals(
            IndividualPortfolioResponse personalPortfolio,
            List<IndividualPortfolioResponse> familyMemberPortfolios
    ) {
        BigDecimal combinedInvested = personalPortfolio.getTotalNetInvestedAmount();
        BigDecimal combinedMarketValue = personalPortfolio.getTotalMarketValue();
        Map<String, BigDecimal> combinedAssetMap = new HashMap<>();

        // Add personal assets
        personalPortfolio.getAssetAllocation().forEach(asset ->
                combinedAssetMap.put(asset.getAssetName(), asset.getMarketValue())
        );

        // Add family member assets
        for (IndividualPortfolioResponse familyPortfolio : familyMemberPortfolios) {
            combinedInvested = combinedInvested.add(familyPortfolio.getTotalNetInvestedAmount());
            combinedMarketValue = combinedMarketValue.add(familyPortfolio.getTotalMarketValue());

            familyPortfolio.getAssetAllocation().forEach(asset ->
                    combinedAssetMap.merge(asset.getAssetName(), asset.getMarketValue(), BigDecimal::add)
            );
        }

        BigDecimal combinedGain = calculateGain(combinedInvested, combinedMarketValue);
        String combinedGainPercentage = calculateNetGainPercentage(combinedGain, combinedInvested);
        List<AssetAllocationResponse> combinedAssetAllocation =
                buildAssetAllocations(combinedAssetMap, combinedMarketValue);

        return new CombinedTotals(
                combinedInvested,
                combinedMarketValue,
                combinedGain,
                combinedGainPercentage,
                combinedAssetAllocation
        );
    }

    /**
     * Build asset allocations from asset market value map
     */
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

    // UTILITY METHODS

    private Long getUserId(String customerId) {
        return userService.getUserIdByCustomerId(customerId);
    }

    private List<UserHoldingPortfolioProjection> getHoldingsForUser(Long userId) {
        return userHoldingRepository.findPortfolioByUserId(userId)
                .orElseThrow(() -> new HoldingNotFoundException("No holdings found for user"));
    }

    private UserHoldingPortfolioProjection getHoldingByUserAndPlan(Long userId, Integer planId) {
        return userHoldingRepository.findByUserAndPlan(userId, planId)
                .orElseThrow(() -> new HoldingNotFoundException("No holding found"));
    }

    private BigDecimal calculateMarketValue(BigDecimal totalUnits, BigDecimal navValue) {
        return totalUnits.multiply(navValue).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateGain(BigDecimal netInvested, BigDecimal marketValue) {
        return marketValue.subtract(netInvested).setScale(2, RoundingMode.HALF_UP);
    }

    private String calculateNetGainPercentage(BigDecimal totalNetGain, BigDecimal totalNetInvestedAmount) {
        if (totalNetInvestedAmount == null || totalNetInvestedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }

        return totalNetGain
                .multiply(BigDecimal.valueOf(100))
                .divide(totalNetInvestedAmount, 2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }
}

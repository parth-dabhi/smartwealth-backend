package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.common.*;
import com.smartwealth.smartwealth_backend.dto.response.goal.RecommendationResponse;
import com.smartwealth.smartwealth_backend.dto.response.goal.SipRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ExpectedReturnCalculator expectedReturnCalculator;
    private final SchemeSelectionService   schemeSelectionService;

    // STEP 1 — How much should the user invest per month?

    /**
     * Calculates the recommended monthly SIP amount to reach targetAmount
     * in durationYears, given the user's risk profile.
     *
     * Formula: PMT (ordinary annuity / end-of-period SIP)
     *   SIP = target × r / ((1 + r)^n - 1)
     *   where r = monthly rate = annualReturn / 1200
     *         n = total months
     *
     * Expected return is risk + duration aware:
     *   ExpectedReturnCalculator → GlidePathAllocationEngine → AssetMix
     *   blended return = (equity% × 12.0%) + (debt% × 6.5%)
     *
     * Example — moderate risk, 19-year goal, ₹4,53,840 target:
     *   GlidePath → AssetMix(85% equity, 15% debt)
     *   blended   → 11.18%
     *   SIP       → ₹581 / month
     */
    @Override
    public SipRecommendationResponse calculateRecommendedSip(
            Integer durationYears,
            BigDecimal targetAmount,
            Integer riskProfileId
    ) {
        int months = durationYears * 12;

        BigDecimal expectedReturn = expectedReturnCalculator.calculate(riskProfileId, months);

        BigDecimal sip = calculateSip(targetAmount, months, expectedReturn);

        log.info("SIP recommendation: duration={}y target={} riskId={} → return={}% sip={}",
                durationYears, targetAmount, riskProfileId, expectedReturn, sip);

        return new SipRecommendationResponse(sip);
    }

    // STEP 2 — Which schemes should the user invest in?

    /**
     * Generates the actual scheme recommendation after the user has confirmed
     * their SIP amount (and optionally a lumpsum amount).
     *
     * Step A: Get asset mix (equity/debt split) for this risk + duration.
     * Step B: Decide how many schemes to recommend (based on SIP amount).
     * Step C: Pick top-scored SIP schemes from each asset class.
     * Step D: Pick top-scored lumpsum schemes (if lumpsum provided).
     * Step E: Build and return the full recommendation response.
     */
    @Override
    public RecommendationResponse generateRecommendation(
            Integer durationYears,
            BigDecimal sipAmount,
            BigDecimal lumpsumAmount,
            Integer riskProfileId
    ) {
        int months = durationYears * 12;

        log.info("Generating recommendation: duration={}y sip={} lumpsum={} riskId={}",
                durationYears, sipAmount, lumpsumAmount, riskProfileId);

        // Step A
        AssetMix mix = expectedReturnCalculator.getAssetMix(riskProfileId, months);
        log.info("Asset mix: equity={}% debt={}%", mix.getEquityPercent(), mix.getDebtPercent());

        // Step B
        SchemeCount schemeCount = resolveSchemeCount(mix, sipAmount);
        log.info("Scheme count: equity={} debt={}", schemeCount.equityCount(), schemeCount.debtCount());

        // Step C
        List<RecommendedSipSchemeDto> sipSchemes = schemeSelectionService.pickSipSchemes(
                mix, sipAmount, months, schemeCount, riskProfileId
        );

        // Step D
        boolean hasLumpsum = lumpsumAmount != null
                && lumpsumAmount.compareTo(BigDecimal.ZERO) > 0;

        List<RecommendedLumpsumSchemeDto> lumpsumSchemes = hasLumpsum
                ? schemeSelectionService.pickLumpsumSchemes(mix, lumpsumAmount, months, schemeCount, riskProfileId)
                : null;

        // Step E
        List<AssetAllocation> allocation = List.of(
                new AssetAllocation("Equity", mix.getEquityPercent()),
                new AssetAllocation("Debt",   mix.getDebtPercent())
        );

        return new RecommendationResponse(sipAmount, lumpsumAmount, allocation, sipSchemes, lumpsumSchemes);
    }

    // PRIVATE HELPERS
    /**
     * PMT formula — ordinary annuity (SIP paid at end of each month).
     *
     *   SIP = target × r / ((1 + r)^n - 1)
     *
     * @param target         goal target amount in ₹
     * @param months         total duration in months
     * @param annualReturn   expected annual return % (e.g. 11.18 for 11.18%)
     * @return monthly SIP amount, rounded to nearest rupee
     */
    private BigDecimal calculateSip(BigDecimal target, int months, BigDecimal annualReturn) {
        // monthly rate = annual% / 1200   (÷100 to convert %, ÷12 for monthly)
        BigDecimal monthlyRate = annualReturn.divide(
                BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP
        );

        BigDecimal onePlusR    = BigDecimal.ONE.add(monthlyRate);
        BigDecimal compounded  = onePlusR.pow(months);                    // (1+r)^n
        BigDecimal denominator = compounded.subtract(BigDecimal.ONE);     // (1+r)^n - 1

        // sipAmount
        return target.multiply(monthlyRate)
                .divide(denominator, 0, RoundingMode.HALF_UP);
    }

    /**
     * Decides how many equity and debt schemes to recommend.
     *
     * Total slots scale with SIP amount:
     *   < ₹8,000  → 3 total slots  (smaller amount, fewer funds to keep SIP viable)
     *   ≥ ₹8,000  → 4 total slots  (more diversification makes sense)
     *
     * Slots are split proportionally by equity/debt percentage.
     * Rules:
     *   - If equity% = 0 → all slots go to debt (e.g. risk-averse short-term goal)
     *   - If debt% = 0   → all slots go to equity
     *   - Otherwise      → proportional split, min 1 each, total capped at max slots
     *
     * Example — mix(85% equity, 15% debt), SIP=₹6,000 → total=3:
     *   equityRatio = 85/100 = 0.85 → floor(0.85 × 3) = 2 (rounds to 3 → clamp)
     *   debtRatio   = 15/100 = 0.15 → floor(0.15 × 3) = 1
     *   result: SchemeCount(equity=2, debt=1) ✓
     *
     * Example — mix(85% equity, 15% debt), SIP=₹6,000 edge case overflow:
     *   If HALF_UP rounding gives equity=3, debt=1 → total=4 > 3
     *   → reduce equity first (it has more) → equity=2, debt=1 ✓
     */
    private SchemeCount resolveSchemeCount(AssetMix mix, BigDecimal sipAmount) {

        int totalSlots = sipAmount.compareTo(BigDecimal.valueOf(8_000)) < 0 ? 3 : 4;

        BigDecimal equityPct = mix.getEquityPercent();
        BigDecimal debtPct   = mix.getDebtPercent();

        // Pure single-asset cases
        if (equityPct.compareTo(BigDecimal.ZERO) == 0) return new SchemeCount(0, totalSlots);
        if (debtPct.compareTo(BigDecimal.ZERO)   == 0) return new SchemeCount(totalSlots, 0);

        // Proportional split
        BigDecimal totalPct = equityPct.add(debtPct);

        int equityCount = equityPct
                .divide(totalPct, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(totalSlots))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        int debtCount = debtPct
                .divide(totalPct, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(totalSlots))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        // Guarantee at least 1 of each since both asset classes are non-zero
        if (equityCount == 0) equityCount = 1;
        if (debtCount   == 0) debtCount   = 1;

        // Rounding can push total above max — trim from the larger side
        while (equityCount + debtCount > totalSlots) {
            if (equityCount >= debtCount)
                equityCount--;
            else
                debtCount--;
        }

        // Rounding can push total below max — add to the larger side
        while (equityCount + debtCount < totalSlots) {
            if (equityPct.compareTo(debtPct) >= 0)
                equityCount++;
            else
                debtCount++;
        }

        return new SchemeCount(equityCount, debtCount);
    }
}

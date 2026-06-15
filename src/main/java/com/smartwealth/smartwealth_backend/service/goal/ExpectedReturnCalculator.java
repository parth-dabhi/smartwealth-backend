package com.smartwealth.smartwealth_backend.service.goal;

import com.smartwealth.smartwealth_backend.dto.common.AssetMix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates the blended expected annual return for a goal based on
 * risk profile + goal duration.
 *
 * How it works:
 *   1. GlidePathAllocationEngine determines equity/debt split
 *      based on BOTH riskId and duration (duration bucket: SHORT/MID/LONG).
 *   2. We multiply each asset class's expected return by its allocation weight.
 *   3. The sum is the blended expected return used for SIP calculation.
 *
 * Example — moderate risk (riskId=3), 19-year goal (LONG bucket):
 *   GlidePath → AssetMix(85% equity, 15% debt)
 *   Expected  = (0.85 × 12.0) + (0.15 × 6.5)
 *             = 10.20 + 0.975
 *             = 11.18%
 *
 * Example — moderate risk (riskId=3), 2-year goal (SHORT bucket):
 *   GlidePath → AssetMix(35% equity, 65% debt)
 *   Expected  = (0.35 × 12.0) + (0.65 × 6.5)
 *             = 4.20 + 4.225
 *             = 8.43%
 *
 * Asset return assumptions (Indian market long-term averages):
 *   Equity: 12.0%  — Nifty 50 long-term CAGR is ~11–13%
 *   Debt:    6.5%  — Debt fund average returns (short to medium duration)
 *
 * These are conservative-to-moderate estimates. They deliberately avoid
 * peak-cycle numbers to give users realistic SIP targets.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpectedReturnCalculator {

    private final GlidePathAllocationEngine allocationEngine;

    private static final BigDecimal EQUITY_RETURN = new BigDecimal("12.0");
    private static final BigDecimal DEBT_RETURN   = new BigDecimal("6.5");

    /**
     * Returns the blended expected annual return (%) for a given risk profile
     * and goal duration.
     *
     * Both riskId AND months affect the result — duration changes the
     * equity/debt split via GlidePathAllocationEngine's duration buckets.
     *
     * @param riskId  1=RiskAverse, 2=Conservative, 3=Moderate,
     *                4=Aggressive, 5=VeryAggressive
     * @param months  goal duration in months
     * @return blended annual return percentage, e.g. 11.18
     */
    public BigDecimal calculate(int riskId, int months) {

        AssetMix mix = allocationEngine.getAllocation(riskId, months);

        log.info("ExpectedReturn: riskId={} months={} → mix={}",
                riskId, months, mix);

        BigDecimal blended =
                pct(mix.getEquityPercent()).multiply(EQUITY_RETURN)
                .add(pct(mix.getDebtPercent()).multiply(DEBT_RETURN));

        BigDecimal result = blended.setScale(2, RoundingMode.HALF_UP);

        log.info("ExpectedReturn: riskId={} months={} → {}%", riskId, months, result);

        return result;
    }

    /**
     * Returns the AssetMix for a given risk + duration — used by
     * RecommendationServiceImpl to split SIP/Lumpsum amounts.
     */
    public AssetMix getAssetMix(int riskId, int months) {
        return allocationEngine.getAllocation(riskId, months);
    }

    private BigDecimal pct(BigDecimal p) {
        return p.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }
}

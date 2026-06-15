package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.entity.enums.HoldingTxnType;
import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.holding.UserHolding;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.HoldingNotFoundException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.HoldingUpdateFailedException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.InvalidSellRequestException;
import com.smartwealth.smartwealth_backend.repository.holding.UserHoldingRepository;
import com.smartwealth.smartwealth_backend.repository.holding.projection.HoldingIdProjection;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingProjection;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingSellProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserHoldingServiceImpl implements UserHoldingService {

    private final UserHoldingRepository userHoldingRepository;
    private final HoldingTransactionService holdingTransactionService;
    private final FolioNumberGeneratorService folioNumberGeneratorService;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Override
    public Long updateUserHoldingsForBuy(
            InvestmentOrder order,
            BigDecimal unitsToBuy,
            PlanNavProjection nav,
            InvestmentMode investmentMode
    ) {

        Long userId = order.getUserId();
        Integer planId = order.getPlanId();
        BigDecimal investedAmount = order.getAmount();
        Long holdingId = order.getHoldingId();

        if (holdingId != null) {

            // EXISTING FOLIO FLOW

            int rowsUpdated = userHoldingRepository.updateUserHoldingUnitsForBuy(
                    holdingId,
                    userId,
                    planId,
                    unitsToBuy,
                    investedAmount,
                    OffsetDateTime.now(IST)
            );

            if (rowsUpdated == 0) {
                throw new HoldingUpdateFailedException("Holding not found for id=" + holdingId);
            }

            log.info("Updated existing holding. holdingId={}, units={}", holdingId, unitsToBuy);

        } else {

            // NEW FOLIO FLOW

            String folioNumber = folioNumberGeneratorService.generateFolioNumber();

            UserHolding holding = UserHolding.builder()
                    .userId(order.getUserId())
                    .planId(order.getPlanId())
                    .folioNumber(folioNumber)
                    .totalUnits(unitsToBuy)
                    .totalInvestedAmount(investedAmount)
                    .totalRedeemedAmount(BigDecimal.ZERO)
                    .isActive(true)
                    .createdAt(OffsetDateTime.now(IST))
                    .updatedAt(OffsetDateTime.now(IST))
                    .build();

            UserHolding savedHolding = userHoldingRepository.save(holding);

            holdingId = savedHolding.getHoldingId(); // Get the generated holding ID

            order.setHoldingId(holdingId); // Update the order with the new holding ID for transaction record creation
        }

        // Create holding transaction record
        holdingTransactionService.createHoldingTransactionRecord(
                holdingId,
                order.getInvestmentOrderId(),
                unitsToBuy,
                investedAmount,
                HoldingTxnType.BUY,
                investmentMode,
                nav
        );

        return holdingId; // Return the holding ID for both existing and new folio flows
    }

    @Override
    public void updateUserHoldingsForSell(
            InvestmentOrder order,
            BigDecimal unitsToSell,
            BigDecimal redeemedAmount,
            PlanNavProjection nav,
            InvestmentMode investmentMode
    ) {
        Long userId = order.getUserId();
        Integer planId = order.getPlanId();
        Long holdingId = order.getHoldingId();

        if (holdingId == null) {
            throw new InvalidSellRequestException("Holding ID is required for SELL");
        }

        // Update existing holding
        int rowsUpdated = userHoldingRepository.updateUserHoldingUnitsForSell(
                holdingId,
                unitsToSell,
                redeemedAmount,
                OffsetDateTime.now(IST)
        );

        if (rowsUpdated == 0) {
            log.error("Failed to update user holding for sell. userId={}, planId={}", userId, planId);
            throw new HoldingUpdateFailedException("Failed to update user holding for sell");
        }

        log.info("Holding updated for SELL. holdingId={}, unitsSold={}", holdingId, unitsToSell);

        // Create holding transaction record
        holdingTransactionService.createHoldingTransactionRecord (
                holdingId,
                order.getInvestmentOrderId(),
                unitsToSell,
                redeemedAmount,
                HoldingTxnType.SELL,
                investmentMode,
                nav
        );
    }

    private UserHoldingSellProjection getHoldingForSell(
            Long holdingId
    ) {
        return userHoldingRepository.findByHoldingId(holdingId, UserHoldingSellProjection.class)
                .orElseThrow(() -> {
                    log.error("No holding found for holdingId: {}", holdingId);
                    return new HoldingNotFoundException("No holding found for the given user and plan");
                });
    }

    @Override
    public BigDecimal getRedeemAmount(
            BigDecimal unitsToSell,
            BigDecimal navLatestValue
    ) {
        if (unitsToSell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSellRequestException("Invalid SELL units specified");
        }
        return unitsToSell
                .multiply(navLatestValue)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getUnitsToSell(
            BigDecimal redeemAmount,
            BigDecimal navLatestValue
    ) {
        if (redeemAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSellRequestException("Invalid SELL amount specified");
        }
        return redeemAmount
                .divide(navLatestValue, 8, RoundingMode.DOWN); // VERY IMPORTANT
    }

    @Override
    public void validateSufficientHoldings(
            Long holdingId,
            BigDecimal unitsToSell
    ) {
        UserHoldingSellProjection holding = getHoldingForSell(holdingId);

        if (holding == null || !holding.getIsActive()) {
            log.warn("No active holding found for SELL.");
            throw new InvalidSellRequestException("No active holding found for SELL");
        }

        if (unitsToSell.compareTo(holding.getTotalUnits()) > 0) {
            log.warn(
                    "SELL exceeds holding units, requested={}, available={}",
                    unitsToSell,
                    holding.getTotalUnits()
            );
            throw new InvalidSellRequestException("SELL exceeds holding units");
        }
    }

    @Override
    public UserHoldingProjection getHoldingFromFolioNumber(
            String folioNumber,
            Long userId
    ) {
        return userHoldingRepository.findHoldingByFolioNumber(folioNumber, userId)
                .orElseThrow(() -> {
                    log.error("No holding found for folio number: {}", folioNumber);
                    return new HoldingNotFoundException("No holding found for the given folio number");
                });
    }
}

package com.smartwealth.smartwealth_backend.service.holding;

import com.smartwealth.smartwealth_backend.entity.enums.InvestmentMode;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;
import com.smartwealth.smartwealth_backend.repository.holding.projection.UserHoldingSellProjection;

import java.math.BigDecimal;

public interface UserHoldingService {

    void updateUserHoldingsForBuy(
            InvestmentOrder order,
            BigDecimal unitsToBuy,
            PlanNavProjection navLatest,
            InvestmentMode investmentMode
    );

    void updateUserHoldingsForSell(
            InvestmentOrder order,
            BigDecimal unitsToSell,
            BigDecimal redeemedAmount,
            PlanNavProjection nav,
            InvestmentMode investmentMode
    );

    UserHoldingSellProjection getHoldingForSell(
            Long userId,
            Integer planId
    );

    BigDecimal getRedeemAmount(
            BigDecimal unitsToSell,
            BigDecimal navLatestValue
    );

    BigDecimal getUnitsToSell(
            BigDecimal redeemAmount,
            BigDecimal navLatestValue
    );

    void validateSufficientHoldings(
            Long userId,
            Integer planId,
            BigDecimal unitsToSell
    );
}

package com.smartwealth.smartwealth_backend.service.investment;

import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavProjection;

public interface InvestmentAllotmentService {
    int processSingleOrder(InvestmentOrder order, PlanNavProjection navMap);
}

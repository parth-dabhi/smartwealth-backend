package com.smartwealth.smartwealth_backend.repository.investment;

import com.smartwealth.smartwealth_backend.entity.investment.InvestmentAllotment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAllotmentRepository extends JpaRepository<InvestmentAllotment, Long> {

    boolean existsByInvestmentOrderId(Long investmentOrderId);
}


package com.smartwealth.smartwealth_backend.dto.response.investment;

import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
import com.smartwealth.smartwealth_backend.entity.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentBuyResponse {

    private Long investmentOrderId;
    private Integer planId;

    private BigDecimal amount;

    private LocalDate applicableNavDate;
    private OrderStatus status; // PENDING

    private String orderTime;

    private String message;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter IST_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public static InvestmentBuyResponse fromOrder(InvestmentOrder order) {
        return InvestmentBuyResponse.builder()
                .investmentOrderId(order.getInvestmentOrderId())
                .planId(order.getPlanId())
                .amount(order.getAmount())
                .applicableNavDate(order.getApplicableNavDate())
                .status(order.getStatus())
                .orderTime(order.getOrderTime().atZoneSameInstant(IST_ZONE).format(IST_FORMATTER))
                .message("Investment buy order placed successfully.")
                .build();
    }
}


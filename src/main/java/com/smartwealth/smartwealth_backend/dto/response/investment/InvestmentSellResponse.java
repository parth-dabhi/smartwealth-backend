package com.smartwealth.smartwealth_backend.dto.response.investment;

import com.smartwealth.smartwealth_backend.entity.enums.OrderStatus;
import com.smartwealth.smartwealth_backend.entity.investment.InvestmentOrder;
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
public class InvestmentSellResponse {

    private Long investmentOrderId;
    private Integer planId;

    private BigDecimal amount;
    private BigDecimal unitsSold;

    private LocalDate applicableNavDate;
    private OrderStatus status; // PENDING

    private String orderTime;
    private String message;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter IST_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public static InvestmentSellResponse fromOrder(InvestmentOrder order) {

        return InvestmentSellResponse.builder()
                .investmentOrderId(order.getInvestmentOrderId())
                .planId(order.getPlanId())
                .amount(order.getAmount())
                .unitsSold(order.getUnits())
                .applicableNavDate(order.getApplicableNavDate())
                .status(order.getStatus())
                .orderTime(
                        order.getOrderTime()
                                .atZoneSameInstant(IST_ZONE)
                                .format(IST_FORMATTER)
                )
                .message("Investment sell order placed successfully.")
                .build();
    }
}

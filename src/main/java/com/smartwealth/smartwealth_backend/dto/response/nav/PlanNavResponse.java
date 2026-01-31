package com.smartwealth.smartwealth_backend.dto.response.nav;

import com.smartwealth.smartwealth_backend.repository.nav.projection.PlanNavViewProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanNavResponse {
    private String planName;
    private BigDecimal navValue;

    public static PlanNavResponse fromProjection(
            PlanNavViewProjection projection
    ) {
        return PlanNavResponse.builder()
                .planName(projection.getPlanName())
                .navValue(projection.getNavValue())
                .build();
    }
}

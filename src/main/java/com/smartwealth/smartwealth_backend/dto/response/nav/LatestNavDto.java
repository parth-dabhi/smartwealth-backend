package com.smartwealth.smartwealth_backend.dto.response.nav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LatestNavDto {

    private LocalDate navDate;
    private BigDecimal navValue;
}


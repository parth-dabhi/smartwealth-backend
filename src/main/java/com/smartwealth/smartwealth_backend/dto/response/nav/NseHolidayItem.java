package com.smartwealth.smartwealth_backend.dto.response.nav;

import lombok.Data;

@Data
public class NseHolidayItem {
    private String tradingDate;
    private String weekDay;
    private String description;
}

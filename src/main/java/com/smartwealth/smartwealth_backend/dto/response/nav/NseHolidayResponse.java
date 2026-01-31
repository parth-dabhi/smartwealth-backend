package com.smartwealth.smartwealth_backend.dto.response.nav;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NseHolidayResponse {

    private List<NseHolidayItem> MF;
}

package com.smartwealth.smartwealth_backend.dto.response.investment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListAllSipMandateResponse {

    Integer totalSips;
    Integer activeSips;
    Integer pausedSips;
    Integer cancelledSips;
    Integer completedSips;
    Integer suspendedSips;

    List<SipMandateResponse> sipMandates;
}

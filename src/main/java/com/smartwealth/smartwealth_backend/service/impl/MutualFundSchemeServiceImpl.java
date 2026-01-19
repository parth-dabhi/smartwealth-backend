package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.response.common.PageMetaResponse;
import com.smartwealth.smartwealth_backend.dto.response.pagination.PaginationResponse;
import com.smartwealth.smartwealth_backend.dto.response.scheme.SchemeWithPlansResponse;
import com.smartwealth.smartwealth_backend.repository.MutualFundSchemeRepository;
import com.smartwealth.smartwealth_backend.repository.SchemePlanRepository;
import com.smartwealth.smartwealth_backend.repository.projection.PlanProjection;
import com.smartwealth.smartwealth_backend.repository.projection.SchemeProjection;
import com.smartwealth.smartwealth_backend.service.MutualFundSchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MutualFundSchemeServiceImpl implements MutualFundSchemeService {

    private final MutualFundSchemeRepository schemeRepository;
    private final SchemePlanRepository planRepository;

    @Override
    public PaginationResponse<SchemeWithPlansResponse> getSchemes(
            Integer amcId,
            Integer assetId,
            Integer categoryId,
            Integer optionTypeId,
            String search,
            int page,
            int size
    ) {
        // Create pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);

        // Fetch schemes based on the provided filters
        Page<SchemeProjection> schemesPage = schemeRepository
                .findSchemes(amcId, assetId, categoryId, optionTypeId, search, pageable);

        // Extract scheme IDs from the retrieved schemes
        List<Integer> schemeIds = schemesPage.getContent()
                .stream()
                .map(SchemeProjection::getSchemeId)
                .toList();

        // Fetch plans for the retrieved schemes
        List<PlanProjection> plans = planRepository.findPlansBySchemeIdsAndOptionType(schemeIds, optionTypeId);

        // Group plans by scheme ID for easy association
        Map<Integer, List<PlanProjection>> plansByScheme = plans.stream()
                        .collect(Collectors.groupingBy(PlanProjection::getSchemeId));

        // Map schemes to SchemeWithPlansResponse including their associated plans
        List<SchemeWithPlansResponse> data =
                schemesPage.getContent().stream()
                        .map(scheme -> SchemeWithPlansResponse.from(
                                scheme,
                                plansByScheme.getOrDefault(scheme.getSchemeId(), List.of())
                        ))
                        .toList();

        log.info("Fetched {} schemes for page {} with size {}", data.size(), page, size);

        // Construct and return the paginated response
        return PaginationResponse.<SchemeWithPlansResponse>builder()
                .meta(PageMetaResponse.from(schemesPage))
                .data(data)
                .build();
    }
}

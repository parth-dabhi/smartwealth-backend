package com.smartwealth.smartwealth_backend.service.impl;

import com.smartwealth.smartwealth_backend.dto.common.CategoryFilterOption;
import com.smartwealth.smartwealth_backend.dto.common.FilterOption;
import com.smartwealth.smartwealth_backend.dto.response.filter.FilterChoicesResponse;
import com.smartwealth.smartwealth_backend.repository.FilterLookupDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterChoiceService {

    private final FilterLookupDao filterLookupDao;

    public FilterChoicesResponse getFilterChoices() {

        return FilterChoicesResponse.builder()
                .amcs(filterLookupDao.findAllAmcs())
                .assets(filterLookupDao.findAllAssets())
                .categories(getCategoryFilters())
                .optionTypes(filterLookupDao.findOptionTypes())
                .build();
    }

    private Map<String, List<FilterOption>> getCategoryFilters() {
        List<CategoryFilterOption> rows = filterLookupDao.findAllCategories();
        return rows.stream()
                .collect(Collectors.groupingBy(
                        CategoryFilterOption::getGroup,
                        LinkedHashMap::new,   // preserve order
                        Collectors.mapping(
                                r -> new FilterOption(r.getLabel(), r.getValue()),
                                Collectors.toList()
                        )
                ));
    }
}



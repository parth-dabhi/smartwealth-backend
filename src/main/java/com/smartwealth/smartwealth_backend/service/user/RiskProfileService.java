package com.smartwealth.smartwealth_backend.service.user;

import com.smartwealth.smartwealth_backend.dto.common.RiskProfileDto;
import com.smartwealth.smartwealth_backend.repository.user.RiskProfileDao;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RiskProfileService {

    private final RiskProfileCacheLoader cacheLoader;

    public Map<Integer, RiskProfileDto> loadAll() {
        return cacheLoader.loadAllList().stream()
                .collect(Collectors.toMap(
                        RiskProfileDto::getId,
                        Function.identity()
                ));
    }

    public RiskProfileDto getById(Integer id) {
        if (id == null) return null;
        return loadAll().get(id);
    }
}

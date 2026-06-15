package com.smartwealth.smartwealth_backend.service.user;

import com.smartwealth.smartwealth_backend.dto.common.RiskProfileDto;
import com.smartwealth.smartwealth_backend.repository.user.RiskProfileDao;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskProfileCacheLoader {

    private final RiskProfileDao dao;

    @Cacheable("risk_profiles_all")
    public List<RiskProfileDto> loadAllList() {
        return dao.findAll();
    }
}

package com.smartwealth.smartwealth_backend.repository.family_member.projection;

import java.time.OffsetDateTime;

public interface PendingFamilyRequestProjection {
    Long getId();
    Long getRequesterId();
    String getRequesterName();
    OffsetDateTime getCreatedAt();
}

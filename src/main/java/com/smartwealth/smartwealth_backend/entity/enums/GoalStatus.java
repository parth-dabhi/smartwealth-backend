package com.smartwealth.smartwealth_backend.entity.enums;

public enum GoalStatus {
    CREATED,        // goal created but no investment yet
    ACTIVE,         // investments started (SIP or lumpsum exists)
    PAUSED,         // SIP paused
    COMPLETED,      // target reached
    CANCELLED,      // user cancelled goal
    FAILED          // duration ended but target not met
}

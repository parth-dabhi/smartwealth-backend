package com.smartwealth.smartwealth_backend.dto.request.transaction;

import com.smartwealth.smartwealth_backend.entity.enums.TransactionCategory;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionStatus;
import com.smartwealth.smartwealth_backend.entity.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TransactionFilterRequest {
    private TransactionType type;
    private TransactionCategory category;
    private TransactionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}

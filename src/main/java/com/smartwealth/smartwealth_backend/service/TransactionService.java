package com.smartwealth.smartwealth_backend.service;


import com.smartwealth.smartwealth_backend.dto.common.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.TransactionCreateCommand;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionCreateCommand command);
}

package com.smartwealth.smartwealth_backend.service.wallet;


import com.smartwealth.smartwealth_backend.dto.response.transaction.TransactionResponse;
import com.smartwealth.smartwealth_backend.entity.transaction.TransactionCreateCommand;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionCreateCommand command);
}

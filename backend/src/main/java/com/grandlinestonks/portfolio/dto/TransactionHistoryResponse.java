package com.grandlinestonks.portfolio.dto;

import com.grandlinestonks.ledger.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionHistoryResponse(
        Long transactionId, TransactionType type, BigDecimal amount, Instant createdAt) {
}

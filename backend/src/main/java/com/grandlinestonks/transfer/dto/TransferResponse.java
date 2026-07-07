package com.grandlinestonks.transfer.dto;

import java.math.BigDecimal;

public record TransferResponse(Long transactionId, String toUsername, BigDecimal amount) {
}

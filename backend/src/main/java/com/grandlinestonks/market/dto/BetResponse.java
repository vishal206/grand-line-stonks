package com.grandlinestonks.market.dto;

import java.math.BigDecimal;
import java.util.List;

public record BetResponse(
        Long betId,
        Long transactionId,
        Long marketId,
        Long outcomeId,
        BigDecimal amount,
        BigDecimal shares,
        List<BigDecimal> pricesAfter) {
}

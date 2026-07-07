package com.grandlinestonks.market.dto;

import com.grandlinestonks.market.MarketStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketResponse(
        Long id,
        String question,
        MarketStatus status,
        BigDecimal liquidity,
        Long createdBy,
        Instant createdAt,
        Long winningOutcomeId,
        List<OutcomeResponse> outcomes) {
}

package com.grandlinestonks.portfolio.dto;

import com.grandlinestonks.market.MarketStatus;
import java.math.BigDecimal;

public record PositionResponse(
        Long positionId,
        Long marketId,
        String question,
        MarketStatus marketStatus,
        Long outcomeId,
        String outcomeLabel,
        BigDecimal shares,
        BigDecimal spent,
        boolean won) {
}

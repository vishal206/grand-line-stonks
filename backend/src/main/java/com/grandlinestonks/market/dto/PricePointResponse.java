package com.grandlinestonks.market.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PricePointResponse(Instant timestamp, List<BigDecimal> prices) {
}

package com.grandlinestonks.market.dto;

import java.math.BigDecimal;

public record OutcomeResponse(Long id, int idx, String label, BigDecimal price, BigDecimal shares) {
}

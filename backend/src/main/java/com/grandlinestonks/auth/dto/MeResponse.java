package com.grandlinestonks.auth.dto;

import java.math.BigDecimal;

public record MeResponse(Long userId, String username, BigDecimal balance) {
}

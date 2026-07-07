package com.grandlinestonks.market.dto;

import jakarta.validation.constraints.NotNull;

public record ResolveRequest(@NotNull Long winningOutcomeId) {
}

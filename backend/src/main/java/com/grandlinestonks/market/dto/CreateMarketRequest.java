package com.grandlinestonks.market.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateMarketRequest(
        @NotBlank @Size(max = 300) String question,
        @NotNull @Size(min = 2, max = 10) List<@NotBlank @Size(max = 100) String> outcomes,
        @NotNull @DecimalMin("10") @Digits(integer = 15, fraction = 4) BigDecimal liquidity) {
}

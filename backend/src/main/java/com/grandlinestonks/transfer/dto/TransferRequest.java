package com.grandlinestonks.transfer.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank String toUsername,
        @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank String idempotencyKey) {
}

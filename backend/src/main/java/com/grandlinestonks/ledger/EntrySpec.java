package com.grandlinestonks.ledger;

import java.math.BigDecimal;

public record EntrySpec(Long accountId, BigDecimal amount) {
}

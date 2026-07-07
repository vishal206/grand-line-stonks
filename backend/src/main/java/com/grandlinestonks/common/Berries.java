package com.grandlinestonks.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Berries {

    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final RoundingMode CHARGE_ROUNDING = RoundingMode.CEILING;
    public static final int PRICE_SCALE = 6;
    public static final BigDecimal STARTING_BERRIES = new BigDecimal("10000.0000");

    private Berries() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(SCALE, ROUNDING);
    }
}

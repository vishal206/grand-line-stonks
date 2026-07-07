package com.grandlinestonks.market;

import com.grandlinestonks.common.Berries;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class LmsrMarketMaker {

    private final double b;

    public LmsrMarketMaker(BigDecimal liquidity) {
        if (liquidity == null || liquidity.signum() <= 0) {
            throw new IllegalArgumentException("liquidity parameter b must be positive");
        }
        this.b = liquidity.doubleValue();
    }

    public double cost(List<BigDecimal> quantities) {
        requireOutcomes(quantities);
        return b * logSumExp(scaledQuantities(quantities));
    }

    public List<BigDecimal> prices(List<BigDecimal> quantities) {
        double[] raw = rawPrices(quantities);
        List<BigDecimal> prices = new ArrayList<>(raw.length);
        for (double price : raw) {
            prices.add(BigDecimal.valueOf(price).setScale(Berries.PRICE_SCALE, Berries.ROUNDING));
        }
        return prices;
    }

    public double[] rawPrices(List<BigDecimal> quantities) {
        requireOutcomes(quantities);
        double[] scaled = scaledQuantities(quantities);
        double max = max(scaled);
        double sum = 0;
        double[] exps = new double[scaled.length];
        for (int i = 0; i < scaled.length; i++) {
            exps[i] = Math.exp(scaled[i] - max);
            sum += exps[i];
        }
        double[] prices = new double[scaled.length];
        for (int i = 0; i < scaled.length; i++) {
            prices[i] = exps[i] / sum;
        }
        return prices;
    }

    public double exactCostToBuy(List<BigDecimal> quantities, int outcomeIndex, BigDecimal shares) {
        requireOutcomes(quantities);
        if (outcomeIndex < 0 || outcomeIndex >= quantities.size()) {
            throw new IllegalArgumentException("outcome index out of range: " + outcomeIndex);
        }
        if (shares == null || shares.signum() <= 0) {
            throw new IllegalArgumentException("shares to buy must be positive");
        }
        List<BigDecimal> after = new ArrayList<>(quantities);
        after.set(outcomeIndex, after.get(outcomeIndex).add(shares));
        return cost(after) - cost(quantities);
    }

    public BigDecimal costToBuy(List<BigDecimal> quantities, int outcomeIndex, BigDecimal shares) {
        return roundCharge(exactCostToBuy(quantities, outcomeIndex, shares));
    }

    public static BigDecimal roundCharge(double exactCost) {
        return BigDecimal.valueOf(exactCost).setScale(Berries.SCALE, Berries.CHARGE_ROUNDING);
    }

    private double[] scaledQuantities(List<BigDecimal> quantities) {
        double[] scaled = new double[quantities.size()];
        for (int i = 0; i < quantities.size(); i++) {
            scaled[i] = quantities.get(i).doubleValue() / b;
        }
        return scaled;
    }

    // Subtracting the max exponent before exponentiating keeps exp() from overflowing for large q/b.
    private static double logSumExp(double[] values) {
        double max = max(values);
        double sum = 0;
        for (double value : values) {
            sum += Math.exp(value - max);
        }
        return max + Math.log(sum);
    }

    private static double max(double[] values) {
        double max = Double.NEGATIVE_INFINITY;
        for (double value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    private static void requireOutcomes(List<BigDecimal> quantities) {
        if (quantities == null || quantities.size() < 2) {
            throw new IllegalArgumentException("a market needs at least two outcomes");
        }
    }
}

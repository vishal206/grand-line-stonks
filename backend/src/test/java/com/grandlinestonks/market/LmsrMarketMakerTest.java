package com.grandlinestonks.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.grandlinestonks.common.Berries;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LmsrMarketMakerTest {

    private static final double EPSILON = 1e-9;

    private final LmsrMarketMaker maker = new LmsrMarketMaker(new BigDecimal("100"));

    private static List<BigDecimal> quantities(String... values) {
        List<BigDecimal> result = new ArrayList<>();
        for (String value : values) {
            result.add(new BigDecimal(value));
        }
        return result;
    }

    @Test
    void pricesSumToOne() {
        assertPricesSumToOne(quantities("0", "0"));
        assertPricesSumToOne(quantities("120.5", "3.25", "88.1"));
        assertPricesSumToOne(quantities("1000000", "0"));
        assertPricesSumToOne(quantities("5", "5", "5", "5", "5"));
    }

    private void assertPricesSumToOne(List<BigDecimal> quantities) {
        double sum = 0;
        for (double price : maker.rawPrices(quantities)) {
            assertThat(price).isBetween(0.0, 1.0);
            sum += price;
        }
        assertThat(sum).isCloseTo(1.0, within(EPSILON));
    }

    @Test
    void equalQuantitiesGiveEqualPrices() {
        double[] prices = maker.rawPrices(quantities("50", "50"));
        assertThat(prices[0]).isCloseTo(0.5, within(EPSILON));
        assertThat(prices[1]).isCloseTo(0.5, within(EPSILON));
    }

    @Test
    void knownWorkedExampleMatches() {
        // b=10, q=[0,0], buy 10 of outcome 0: 10 * ln((e + 1) / 2) = 6.201145069582...
        LmsrMarketMaker smallMaker = new LmsrMarketMaker(BigDecimal.TEN);
        double cost = smallMaker.exactCostToBuy(quantities("0", "0"), 0, BigDecimal.TEN);
        assertThat(cost).isCloseTo(10 * Math.log((Math.E + 1) / 2), within(EPSILON));
        assertThat(cost).isCloseTo(6.2011450695, within(1e-9));
    }

    @Test
    void priceMatchesClosedFormAfterSkewedQuantities() {
        // q0 = b*ln(3) makes exp(q0/b) = 3, so price0 = 3/4.
        BigDecimal q0 = BigDecimal.valueOf(100 * Math.log(3));
        double[] prices = maker.rawPrices(List.of(q0, BigDecimal.ZERO));
        assertThat(prices[0]).isCloseTo(0.75, within(1e-9));
        assertThat(prices[1]).isCloseTo(0.25, within(1e-9));
    }

    @Test
    void costToBuyIsAlwaysPositive() {
        Random random = new Random(7);
        for (int i = 0; i < 100; i++) {
            List<BigDecimal> quantities = quantities(
                    String.valueOf(random.nextInt(1000)), String.valueOf(random.nextInt(1000)));
            BigDecimal shares = BigDecimal.valueOf(1 + random.nextInt(100));
            int outcome = random.nextInt(2);
            assertThat(maker.exactCostToBuy(quantities, outcome, shares)).isGreaterThan(0.0);
            assertThat(maker.costToBuy(quantities, outcome, shares))
                    .isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    void costFunctionIsMonotonicallyIncreasingInShares() {
        List<BigDecimal> base = quantities("30", "70");
        double previous = maker.cost(base);
        for (int shares = 1; shares <= 50; shares++) {
            List<BigDecimal> next = quantities(String.valueOf(30 + shares), "70");
            double current = maker.cost(next);
            assertThat(current).isGreaterThan(previous);
            previous = current;
        }
    }

    @Test
    void largeQuantitiesStayNumericallyStable() {
        List<BigDecimal> extreme = quantities("10000000", "0");
        double[] prices = maker.rawPrices(extreme);
        assertThat(prices[0]).isCloseTo(1.0, within(EPSILON));
        assertThat(prices[1]).isCloseTo(0.0, within(EPSILON));
        assertThat(maker.cost(extreme)).isFinite();
        assertThat(maker.exactCostToBuy(extreme, 1, BigDecimal.ONE)).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void chargeRoundingNeverUnderchargesAndResidualIsBounded() {
        BigDecimal roundingUnit = BigDecimal.ONE.movePointLeft(Berries.SCALE);
        Random random = new Random(99);
        List<BigDecimal> quantities = quantities("0", "0");
        BigDecimal totalCharged = BigDecimal.ZERO;
        double totalExact = 0;
        int trades = 1000;
        for (int i = 0; i < trades; i++) {
            int outcome = random.nextInt(2);
            BigDecimal shares = BigDecimal.valueOf(1 + random.nextInt(99), 2);
            double exact = maker.exactCostToBuy(quantities, outcome, shares);
            BigDecimal charged = maker.costToBuy(quantities, outcome, shares);
            BigDecimal residual = charged.subtract(BigDecimal.valueOf(exact));
            assertThat(residual).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(residual).isLessThan(roundingUnit);
            quantities.set(outcome, quantities.get(outcome).add(shares));
            totalCharged = totalCharged.add(charged);
            totalExact += exact;
        }
        BigDecimal drift = totalCharged.subtract(BigDecimal.valueOf(totalExact));
        assertThat(drift).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(drift).isLessThan(roundingUnit.multiply(BigDecimal.valueOf(trades)));
    }

    @Test
    void invalidInputsAreRejected() {
        assertThatThrownBy(() -> new LmsrMarketMaker(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> maker.cost(quantities("5")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> maker.exactCostToBuy(quantities("0", "0"), 2, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> maker.exactCostToBuy(quantities("0", "0"), 0, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

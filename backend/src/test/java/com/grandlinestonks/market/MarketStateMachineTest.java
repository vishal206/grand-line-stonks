package com.grandlinestonks.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.grandlinestonks.error.IllegalMarketTransitionException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MarketStateMachineTest {

    private final MarketStateMachine stateMachine = new MarketStateMachine();

    private Market marketIn(MarketStatus status) {
        Market market = new Market("q?", new BigDecimal("100"), 1L);
        if (status != MarketStatus.DRAFT) {
            market.moveTo(status);
        }
        return market;
    }

    @Test
    void fullLifecycleIsLegal() {
        Market market = marketIn(MarketStatus.DRAFT);
        stateMachine.transition(market, MarketStatus.OPEN);
        stateMachine.transition(market, MarketStatus.CLOSED);
        stateMachine.transition(market, MarketStatus.RESOLVED);
        stateMachine.transition(market, MarketStatus.SETTLED);
        assertThat(market.getStatus()).isEqualTo(MarketStatus.SETTLED);
    }

    @Test
    void skippingStatesIsRejected() {
        assertThatThrownBy(() -> stateMachine.transition(marketIn(MarketStatus.DRAFT), MarketStatus.CLOSED))
                .isInstanceOf(IllegalMarketTransitionException.class);
        assertThatThrownBy(() -> stateMachine.transition(marketIn(MarketStatus.OPEN), MarketStatus.RESOLVED))
                .isInstanceOf(IllegalMarketTransitionException.class);
        assertThatThrownBy(() -> stateMachine.transition(marketIn(MarketStatus.DRAFT), MarketStatus.SETTLED))
                .isInstanceOf(IllegalMarketTransitionException.class);
    }

    @Test
    void movingBackwardsIsRejected() {
        assertThatThrownBy(() -> stateMachine.transition(marketIn(MarketStatus.CLOSED), MarketStatus.OPEN))
                .isInstanceOf(IllegalMarketTransitionException.class);
        assertThatThrownBy(() -> stateMachine.transition(marketIn(MarketStatus.SETTLED), MarketStatus.RESOLVED))
                .isInstanceOf(IllegalMarketTransitionException.class);
    }

    @Test
    void selfTransitionIsRejected() {
        assertThatThrownBy(() -> stateMachine.transition(marketIn(MarketStatus.OPEN), MarketStatus.OPEN))
                .isInstanceOf(IllegalMarketTransitionException.class);
    }

    @Test
    void requireStatusRejectsWrongState() {
        assertThatThrownBy(() -> stateMachine.requireStatus(
                marketIn(MarketStatus.DRAFT), MarketStatus.OPEN, "accept bets"))
                .isInstanceOf(IllegalMarketTransitionException.class);
        stateMachine.requireStatus(marketIn(MarketStatus.OPEN), MarketStatus.OPEN, "accept bets");
    }
}

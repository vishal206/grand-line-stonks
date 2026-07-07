package com.grandlinestonks.market;

import com.grandlinestonks.error.IllegalMarketTransitionException;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MarketStateMachine {

    private static final Map<MarketStatus, Set<MarketStatus>> LEGAL_TRANSITIONS = Map.of(
            MarketStatus.DRAFT, Set.of(MarketStatus.OPEN, MarketStatus.CANCELLED),
            MarketStatus.OPEN, Set.of(MarketStatus.CLOSED, MarketStatus.CANCELLED),
            MarketStatus.CLOSED, Set.of(MarketStatus.RESOLVED, MarketStatus.CANCELLED),
            MarketStatus.RESOLVED, Set.of(MarketStatus.SETTLED),
            MarketStatus.SETTLED, Set.of(),
            MarketStatus.CANCELLED, Set.of());

    public void transition(Market market, MarketStatus target) {
        Set<MarketStatus> allowed = LEGAL_TRANSITIONS.get(market.getStatus());
        if (allowed == null || !allowed.contains(target)) {
            throw new IllegalMarketTransitionException(
                    "market " + market.getId() + " cannot go from " + market.getStatus() + " to " + target);
        }
        market.moveTo(target);
    }

    public void requireStatus(Market market, MarketStatus expected, String action) {
        if (market.getStatus() != expected) {
            throw new IllegalMarketTransitionException(
                    "market " + market.getId() + " must be " + expected + " to " + action
                            + ", but is " + market.getStatus());
        }
    }
}

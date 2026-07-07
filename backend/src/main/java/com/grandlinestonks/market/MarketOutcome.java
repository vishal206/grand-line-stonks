package com.grandlinestonks.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "market_outcomes")
public class MarketOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_id", nullable = false, updatable = false)
    private Long marketId;

    @Column(nullable = false, updatable = false)
    private int idx;

    @Column(nullable = false, updatable = false)
    private String label;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal shares;

    protected MarketOutcome() {
    }

    public MarketOutcome(Long marketId, int idx, String label) {
        this.marketId = marketId;
        this.idx = idx;
        this.label = label;
        this.shares = BigDecimal.ZERO;
    }

    public void addShares(BigDecimal delta) {
        this.shares = this.shares.add(delta);
    }

    public Long getId() {
        return id;
    }

    public Long getMarketId() {
        return marketId;
    }

    public int getIdx() {
        return idx;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getShares() {
        return shares;
    }
}

package com.grandlinestonks.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "market_id", nullable = false, updatable = false)
    private Long marketId;

    @Column(name = "outcome_id", nullable = false, updatable = false)
    private Long outcomeId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal shares;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal spent;

    protected Position() {
    }

    public Position(Long userId, Long marketId, Long outcomeId) {
        this.userId = userId;
        this.marketId = marketId;
        this.outcomeId = outcomeId;
        this.shares = BigDecimal.ZERO;
        this.spent = BigDecimal.ZERO;
    }

    public void addPurchase(BigDecimal shares, BigDecimal amount) {
        this.shares = this.shares.add(shares);
        this.spent = this.spent.add(amount);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getMarketId() {
        return marketId;
    }

    public Long getOutcomeId() {
        return outcomeId;
    }

    public BigDecimal getShares() {
        return shares;
    }

    public BigDecimal getSpent() {
        return spent;
    }
}

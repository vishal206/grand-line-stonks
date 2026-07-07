package com.grandlinestonks.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market_id", nullable = false, updatable = false)
    private Long marketId;

    @Column(name = "outcome_id", nullable = false, updatable = false)
    private Long outcomeId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "transaction_id", nullable = false, updatable = false, unique = true)
    private Long transactionId;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal shares;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prices_after", nullable = false, updatable = false)
    private List<BigDecimal> pricesAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Bet() {
    }

    public Bet(Long marketId, Long outcomeId, Long userId, Long transactionId,
            BigDecimal amount, BigDecimal shares, List<BigDecimal> pricesAfter) {
        this.marketId = marketId;
        this.outcomeId = outcomeId;
        this.userId = userId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.shares = shares;
        this.pricesAfter = pricesAfter;
    }

    public Long getId() {
        return id;
    }

    public Long getMarketId() {
        return marketId;
    }

    public Long getOutcomeId() {
        return outcomeId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getShares() {
        return shares;
    }

    public List<BigDecimal> getPricesAfter() {
        return pricesAfter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

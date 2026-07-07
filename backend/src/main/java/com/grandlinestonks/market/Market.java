package com.grandlinestonks.market;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "markets")
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarketStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal liquidity;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "market_maker_account_id", unique = true)
    private Long marketMakerAccountId;

    @Column(name = "winning_outcome_id")
    private Long winningOutcomeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Market() {
    }

    public Market(String question, BigDecimal liquidity, Long createdBy) {
        this.question = question;
        this.liquidity = liquidity;
        this.createdBy = createdBy;
        this.status = MarketStatus.DRAFT;
    }

    public void moveTo(MarketStatus next) {
        this.status = next;
    }

    public void attachMarketMakerAccount(Long accountId) {
        this.marketMakerAccountId = accountId;
    }

    public void setWinningOutcome(Long outcomeId) {
        this.winningOutcomeId = outcomeId;
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public MarketStatus getStatus() {
        return status;
    }

    public BigDecimal getLiquidity() {
        return liquidity;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Long getMarketMakerAccountId() {
        return marketMakerAccountId;
    }

    public Long getWinningOutcomeId() {
        return winningOutcomeId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

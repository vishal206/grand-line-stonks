package com.grandlinestonks.market;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.account.AccountType;
import com.grandlinestonks.error.AccountNotFoundException;
import com.grandlinestonks.error.InvalidBetException;
import com.grandlinestonks.ledger.EntrySpec;
import com.grandlinestonks.ledger.LedgerService;
import com.grandlinestonks.ledger.TransactionType;
import com.grandlinestonks.market.dto.MarketResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {

    private final MarketService marketService;
    private final MarketOutcomeRepository outcomeRepository;
    private final PositionRepository positionRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final MarketStateMachine stateMachine;
    private final Timer settlementTimer;

    public SettlementService(
            MarketService marketService,
            MarketOutcomeRepository outcomeRepository,
            PositionRepository positionRepository,
            AccountRepository accountRepository,
            LedgerService ledgerService,
            MarketStateMachine stateMachine,
            MeterRegistry meterRegistry) {
        this.marketService = marketService;
        this.outcomeRepository = outcomeRepository;
        this.positionRepository = positionRepository;
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.stateMachine = stateMachine;
        this.settlementTimer = Timer.builder("settlement.duration").register(meterRegistry);
    }

    @Transactional
    public MarketResponse resolve(Long userId, Long marketId, Long winningOutcomeId) {
        Market market = marketService.lockMarket(marketId);
        marketService.requireOwner(market, userId);
        outcomeRepository.findByIdAndMarketId(winningOutcomeId, marketId)
                .orElseThrow(() -> new InvalidBetException(
                        "outcome " + winningOutcomeId + " does not belong to this market"));
        stateMachine.transition(market, MarketStatus.RESOLVED);
        market.setWinningOutcome(winningOutcomeId);
        return marketService.toDto(market);
    }

    @Transactional
    public MarketResponse settle(Long userId, Long marketId) {
        Timer.Sample sample = Timer.start();
        try {
            Market market = marketService.lockMarket(marketId);
            marketService.requireOwner(market, userId);
            stateMachine.transition(market, MarketStatus.SETTLED);
            Account marketMaker = marketMakerOf(market);
            List<EntrySpec> entries = payoutEntries(market, marketMaker);
            if (!entries.isEmpty()) {
                ledgerService.record(TransactionType.SETTLEMENT, "settle:market:" + marketId, entries);
            }
            return marketService.toDto(market);
        } finally {
            sample.stop(settlementTimer);
        }
    }

    @Transactional
    public MarketResponse cancel(Long userId, Long marketId) {
        Market market = marketService.lockMarket(marketId);
        marketService.requireOwner(market, userId);
        boolean funded = market.getMarketMakerAccountId() != null;
        stateMachine.transition(market, MarketStatus.CANCELLED);
        if (funded) {
            List<EntrySpec> entries = refundEntries(market, marketMakerOf(market));
            if (!entries.isEmpty()) {
                ledgerService.record(TransactionType.REFUND, "cancel:market:" + marketId, entries);
            }
        }
        return marketService.toDto(market);
    }

    private List<EntrySpec> payoutEntries(Market market, Account marketMaker) {
        Map<Long, BigDecimal> payoutByAccount = new HashMap<>();
        for (Position position : positionRepository.findByMarketId(market.getId())) {
            if (position.getOutcomeId().equals(market.getWinningOutcomeId())
                    && position.getShares().signum() > 0) {
                Long accountId = userAccountId(position.getUserId());
                payoutByAccount.merge(accountId, position.getShares(), BigDecimal::add);
            }
        }
        return drainMarketMaker(marketMaker, payoutByAccount);
    }

    private List<EntrySpec> refundEntries(Market market, Account marketMaker) {
        Map<Long, BigDecimal> refundByAccount = new HashMap<>();
        for (Position position : positionRepository.findByMarketId(market.getId())) {
            if (position.getSpent().signum() > 0) {
                Long accountId = userAccountId(position.getUserId());
                refundByAccount.merge(accountId, position.getSpent(), BigDecimal::add);
            }
        }
        return drainMarketMaker(marketMaker, refundByAccount);
    }

    private List<EntrySpec> drainMarketMaker(Account marketMaker, Map<Long, BigDecimal> credits) {
        BigDecimal total = credits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal residual = marketMaker.getBalance().subtract(total);
        List<EntrySpec> entries = new ArrayList<>();
        credits.forEach((accountId, amount) -> entries.add(new EntrySpec(accountId, amount)));
        if (residual.signum() != 0) {
            entries.add(new EntrySpec(treasury().getId(), residual));
        }
        if (marketMaker.getBalance().signum() != 0) {
            entries.add(new EntrySpec(marketMaker.getId(), marketMaker.getBalance().negate()));
        }
        return entries.size() >= 2 ? entries : List.of();
    }

    private Account marketMakerOf(Market market) {
        return accountRepository.findById(market.getMarketMakerAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "market maker for market " + market.getId()));
    }

    private Long userAccountId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("user id " + userId))
                .getId();
    }

    private Account treasury() {
        return accountRepository.findByType(AccountType.TREASURY)
                .orElseThrow(() -> new AccountNotFoundException("treasury"));
    }
}

package com.grandlinestonks.portfolio;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.error.AccountNotFoundException;
import com.grandlinestonks.ledger.LedgerEntry;
import com.grandlinestonks.ledger.LedgerEntryRepository;
import com.grandlinestonks.ledger.LedgerTransaction;
import com.grandlinestonks.ledger.LedgerTransactionRepository;
import com.grandlinestonks.market.Market;
import com.grandlinestonks.market.MarketOutcome;
import com.grandlinestonks.market.MarketOutcomeRepository;
import com.grandlinestonks.market.MarketRepository;
import com.grandlinestonks.market.Position;
import com.grandlinestonks.market.PositionRepository;
import com.grandlinestonks.portfolio.dto.PositionResponse;
import com.grandlinestonks.portfolio.dto.TransactionHistoryResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PositionRepository positionRepository;
    private final MarketRepository marketRepository;
    private final MarketOutcomeRepository outcomeRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository entryRepository;
    private final LedgerTransactionRepository transactionRepository;

    public PortfolioService(
            PositionRepository positionRepository,
            MarketRepository marketRepository,
            MarketOutcomeRepository outcomeRepository,
            AccountRepository accountRepository,
            LedgerEntryRepository entryRepository,
            LedgerTransactionRepository transactionRepository) {
        this.positionRepository = positionRepository;
        this.marketRepository = marketRepository;
        this.outcomeRepository = outcomeRepository;
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> positions(Long userId, Pageable pageable) {
        Page<Position> positions = positionRepository.findByUserId(userId, pageable);
        Map<Long, Market> markets = marketRepository
                .findAllById(positions.map(Position::getMarketId).toList()).stream()
                .collect(Collectors.toMap(Market::getId, Function.identity()));
        Map<Long, MarketOutcome> outcomes = outcomeRepository
                .findAllById(positions.map(Position::getOutcomeId).toList()).stream()
                .collect(Collectors.toMap(MarketOutcome::getId, Function.identity()));
        return positions.map(position -> {
            Market market = markets.get(position.getMarketId());
            MarketOutcome outcome = outcomes.get(position.getOutcomeId());
            return new PositionResponse(
                    position.getId(), market.getId(), market.getQuestion(), market.getStatus(),
                    outcome.getId(), outcome.getLabel(), position.getShares(), position.getSpent(),
                    position.getOutcomeId().equals(market.getWinningOutcomeId()));
        });
    }

    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> transactions(Long userId, Pageable pageable) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("user id " + userId));
        Page<LedgerEntry> entries = entryRepository.findByAccountId(account.getId(), pageable);
        Map<Long, LedgerTransaction> transactions = transactionRepository
                .findAllById(entries.map(LedgerEntry::getTransactionId).toList()).stream()
                .collect(Collectors.toMap(LedgerTransaction::getId, Function.identity()));
        return entries.map(entry -> new TransactionHistoryResponse(
                entry.getTransactionId(),
                transactions.get(entry.getTransactionId()).getType(),
                entry.getAmount(),
                entry.getCreatedAt()));
    }
}

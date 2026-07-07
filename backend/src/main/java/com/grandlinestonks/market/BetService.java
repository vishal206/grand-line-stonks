package com.grandlinestonks.market;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.error.AccountNotFoundException;
import com.grandlinestonks.error.InvalidBetException;
import com.grandlinestonks.ledger.EntrySpec;
import com.grandlinestonks.ledger.LedgerService;
import com.grandlinestonks.ledger.LedgerTransaction;
import com.grandlinestonks.ledger.TransactionType;
import com.grandlinestonks.market.dto.BetRequest;
import com.grandlinestonks.market.dto.BetResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BetService {

    private final MarketService marketService;
    private final MarketOutcomeRepository outcomeRepository;
    private final PositionRepository positionRepository;
    private final BetRepository betRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final MarketStateMachine stateMachine;

    public BetService(
            MarketService marketService,
            MarketOutcomeRepository outcomeRepository,
            PositionRepository positionRepository,
            BetRepository betRepository,
            AccountRepository accountRepository,
            LedgerService ledgerService,
            MarketStateMachine stateMachine) {
        this.marketService = marketService;
        this.outcomeRepository = outcomeRepository;
        this.positionRepository = positionRepository;
        this.betRepository = betRepository;
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public BetResponse placeBet(Long userId, Long marketId, BetRequest request) {
        Market market = marketService.lockMarket(marketId);
        Optional<BetResponse> replay = findReplay(marketId, request.idempotencyKey());
        if (replay.isPresent()) {
            return replay.get();
        }
        stateMachine.requireStatus(market, MarketStatus.OPEN, "accept bets");

        List<MarketOutcome> outcomes = outcomeRepository.findByMarketIdOrderByIdx(marketId);
        int outcomeIndex = indexOf(outcomes, request.outcomeId());
        LmsrMarketMaker maker = new LmsrMarketMaker(market.getLiquidity());
        List<BigDecimal> quantities = outcomes.stream().map(MarketOutcome::getShares).toList();
        BigDecimal shares = buyableShares(maker, quantities, outcomeIndex, request.amount());

        LedgerTransaction transaction = chargeBettor(userId, market, request);
        MarketOutcome outcome = outcomes.get(outcomeIndex);
        outcome.addShares(shares);
        updatePosition(userId, marketId, outcome.getId(), shares, request.amount());
        List<BigDecimal> pricesAfter = maker.prices(
                outcomes.stream().map(MarketOutcome::getShares).toList());
        Bet bet = betRepository.save(new Bet(
                marketId, outcome.getId(), userId, transaction.getId(),
                request.amount(), shares, pricesAfter));
        return toResponse(bet);
    }

    private Optional<BetResponse> findReplay(Long marketId, String idempotencyKey) {
        return ledgerService.findByIdempotencyKey(idempotencyKey)
                .map(transaction -> {
                    Bet bet = betRepository.findByTransactionId(transaction.getId())
                            .filter(existing -> existing.getMarketId().equals(marketId))
                            .orElseThrow(() -> new InvalidBetException(
                                    "idempotency key already used for another operation"));
                    return toResponse(bet);
                });
    }

    private int indexOf(List<MarketOutcome> outcomes, Long outcomeId) {
        for (int i = 0; i < outcomes.size(); i++) {
            if (outcomes.get(i).getId().equals(outcomeId)) {
                return i;
            }
        }
        throw new InvalidBetException("outcome " + outcomeId + " does not belong to this market");
    }

    private BigDecimal buyableShares(
            LmsrMarketMaker maker, List<BigDecimal> quantities, int outcomeIndex, BigDecimal amount) {
        BigDecimal shares;
        try {
            shares = maker.sharesForSpend(quantities, outcomeIndex, amount);
        } catch (IllegalArgumentException e) {
            throw new InvalidBetException(e.getMessage());
        }
        if (shares.signum() <= 0) {
            throw new InvalidBetException("amount too small to buy any shares");
        }
        return shares;
    }

    private LedgerTransaction chargeBettor(Long userId, Market market, BetRequest request) {
        Account bettor = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException("user id " + userId));
        return ledgerService.record(TransactionType.BET, request.idempotencyKey(), List.of(
                new EntrySpec(bettor.getId(), request.amount().negate()),
                new EntrySpec(market.getMarketMakerAccountId(), request.amount())));
    }

    private void updatePosition(
            Long userId, Long marketId, Long outcomeId, BigDecimal shares, BigDecimal amount) {
        Position position = positionRepository.findByUserIdAndOutcomeId(userId, outcomeId)
                .orElseGet(() -> positionRepository.save(new Position(userId, marketId, outcomeId)));
        position.addPurchase(shares, amount);
    }

    private BetResponse toResponse(Bet bet) {
        return new BetResponse(
                bet.getId(), bet.getTransactionId(), bet.getMarketId(), bet.getOutcomeId(),
                bet.getAmount(), bet.getShares(), bet.getPricesAfter());
    }
}

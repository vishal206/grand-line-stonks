package com.grandlinestonks.market;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.account.AccountType;
import com.grandlinestonks.error.AccountNotFoundException;
import com.grandlinestonks.error.MarketNotFoundException;
import com.grandlinestonks.error.NotMarketOwnerException;
import com.grandlinestonks.ledger.EntrySpec;
import com.grandlinestonks.ledger.LedgerService;
import com.grandlinestonks.ledger.TransactionType;
import com.grandlinestonks.market.dto.CreateMarketRequest;
import com.grandlinestonks.market.dto.MarketResponse;
import com.grandlinestonks.market.dto.OutcomeResponse;
import com.grandlinestonks.market.dto.PricePointResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketService {

    private final MarketRepository marketRepository;
    private final MarketOutcomeRepository outcomeRepository;
    private final BetRepository betRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final MarketStateMachine stateMachine;

    public MarketService(
            MarketRepository marketRepository,
            MarketOutcomeRepository outcomeRepository,
            BetRepository betRepository,
            AccountRepository accountRepository,
            LedgerService ledgerService,
            MarketStateMachine stateMachine) {
        this.marketRepository = marketRepository;
        this.outcomeRepository = outcomeRepository;
        this.betRepository = betRepository;
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.stateMachine = stateMachine;
    }

    @Transactional
    public MarketResponse create(Long userId, CreateMarketRequest request) {
        Market market = marketRepository.save(
                new Market(request.question(), request.liquidity(), userId));
        List<MarketOutcome> outcomes = new ArrayList<>();
        for (int i = 0; i < request.outcomes().size(); i++) {
            outcomes.add(new MarketOutcome(market.getId(), i, request.outcomes().get(i)));
        }
        outcomeRepository.saveAll(outcomes);
        return toResponse(market, outcomes);
    }

    @Transactional
    public MarketResponse open(Long userId, Long marketId) {
        Market market = lockMarket(marketId);
        if (!market.getCreatedBy().equals(userId)) {
            throw new NotMarketOwnerException(marketId);
        }
        stateMachine.transition(market, MarketStatus.OPEN);
        List<MarketOutcome> outcomes = outcomeRepository.findByMarketIdOrderByIdx(marketId);
        Account marketMaker = accountRepository.save(
                new Account(null, AccountType.MARKET_MAKER, BigDecimal.ZERO));
        market.attachMarketMakerAccount(marketMaker.getId());
        fundSubsidy(market, marketMaker, outcomes.size());
        return toResponse(market, outcomes);
    }

    @Transactional(readOnly = true)
    public MarketResponse get(Long marketId) {
        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketNotFoundException("id " + marketId));
        return toResponse(market, outcomeRepository.findByMarketIdOrderByIdx(marketId));
    }

    @Transactional(readOnly = true)
    public List<MarketResponse> list(MarketStatus status) {
        List<Market> markets = status == null
                ? marketRepository.findAll()
                : marketRepository.findByStatusOrderByCreatedAtDesc(status);
        return markets.stream()
                .map(market -> toResponse(
                        market, outcomeRepository.findByMarketIdOrderByIdx(market.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PricePointResponse> priceHistory(Long marketId) {
        if (!marketRepository.existsById(marketId)) {
            throw new MarketNotFoundException("id " + marketId);
        }
        return betRepository.findByMarketIdOrderByCreatedAt(marketId).stream()
                .map(bet -> new PricePointResponse(bet.getCreatedAt(), bet.getPricesAfter()))
                .toList();
    }

    Market lockMarket(Long marketId) {
        return marketRepository.lockById(marketId)
                .orElseThrow(() -> new MarketNotFoundException("id " + marketId));
    }

    private void fundSubsidy(Market market, Account marketMaker, int outcomeCount) {
        Account treasury = accountRepository.findByType(AccountType.TREASURY)
                .orElseThrow(() -> new AccountNotFoundException("treasury"));
        BigDecimal subsidy = new LmsrMarketMaker(market.getLiquidity()).maxLoss(outcomeCount);
        ledgerService.record(TransactionType.SUBSIDY, "subsidy:market:" + market.getId(), List.of(
                new EntrySpec(treasury.getId(), subsidy.negate()),
                new EntrySpec(marketMaker.getId(), subsidy)));
    }

    private MarketResponse toResponse(Market market, List<MarketOutcome> outcomes) {
        LmsrMarketMaker maker = new LmsrMarketMaker(market.getLiquidity());
        List<BigDecimal> prices = maker.prices(
                outcomes.stream().map(MarketOutcome::getShares).toList());
        List<OutcomeResponse> outcomeResponses = new ArrayList<>();
        for (int i = 0; i < outcomes.size(); i++) {
            MarketOutcome outcome = outcomes.get(i);
            outcomeResponses.add(new OutcomeResponse(
                    outcome.getId(), outcome.getIdx(), outcome.getLabel(),
                    prices.get(i), outcome.getShares()));
        }
        return new MarketResponse(
                market.getId(), market.getQuestion(), market.getStatus(), market.getLiquidity(),
                market.getCreatedBy(), market.getCreatedAt(), outcomeResponses);
    }
}

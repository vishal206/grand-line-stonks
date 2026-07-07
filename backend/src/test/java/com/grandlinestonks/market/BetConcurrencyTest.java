package com.grandlinestonks.market;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.account.AccountType;
import com.grandlinestonks.error.InsufficientBalanceException;
import com.grandlinestonks.ledger.EntrySpec;
import com.grandlinestonks.ledger.LedgerService;
import com.grandlinestonks.ledger.TransactionType;
import com.grandlinestonks.market.dto.BetRequest;
import com.grandlinestonks.market.dto.BetResponse;
import com.grandlinestonks.market.dto.CreateMarketRequest;
import com.grandlinestonks.market.dto.MarketResponse;
import com.grandlinestonks.user.User;
import com.grandlinestonks.user.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BetConcurrencyTest {

    private static final int BETTORS = 8;
    private static final int BETS_PER_BETTOR = 15;
    private static final int THREADS = 16;
    private static final BigDecimal SEED_BALANCE = new BigDecimal("1000.0000");

    @Autowired
    private MarketService marketService;

    @Autowired
    private BetService betService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketOutcomeRepository outcomeRepository;

    @Test
    void parallelBetsPreserveAllInvariants() throws Exception {
        List<Long> bettors = seedBettors();
        Long creator = bettors.get(0);
        MarketResponse market = marketService.create(creator, new CreateMarketRequest(
                "Who wins the next big clash?", List.of("Straw Hats", "Marines", "Draw"),
                new BigDecimal("100")));
        marketService.open(creator, market.id());
        List<MarketOutcome> outcomes = outcomeRepository.findByMarketIdOrderByIdx(market.id());
        BigDecimal totalBefore = totalBalance();

        Random random = new Random(4242);
        List<Future<?>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        for (Long bettor : bettors) {
            for (int i = 0; i < BETS_PER_BETTOR; i++) {
                Long outcomeId = outcomes.get(random.nextInt(outcomes.size())).getId();
                BigDecimal amount = BigDecimal.valueOf(1 + random.nextInt(4000), 2);
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        betService.placeBet(bettor, market.id(), new BetRequest(
                                outcomeId, amount, UUID.randomUUID().toString()));
                    } catch (InsufficientBalanceException ignored) {
                    }
                    return null;
                }));
            }
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(180, TimeUnit.SECONDS)).isTrue();
        for (Future<?> future : futures) {
            future.get();
        }

        assertThat(totalBalance()).isEqualByComparingTo(totalBefore);
        for (Account account : accountRepository.findAll()) {
            if (account.getType() != AccountType.TREASURY) {
                assertThat(account.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            }
            assertThat(ledgerService.recomputeBalance(account.getId()))
                    .isEqualByComparingTo(account.getBalance());
        }
        double priceSum = 0;
        for (var outcome : marketService.get(market.id()).outcomes()) {
            priceSum += outcome.price().doubleValue();
        }
        assertThat(priceSum).isCloseTo(1.0, within(1e-4));
    }

    @Test
    void concurrentBetsWithSameIdempotencyKeyChargeOnce() throws Exception {
        List<Long> bettors = seedBettors();
        Long creator = bettors.get(0);
        MarketResponse market = marketService.create(creator, new CreateMarketRequest(
                "Race to the treasure?", List.of("Yes", "No"), new BigDecimal("100")));
        marketService.open(creator, market.id());
        Long outcomeId = outcomeRepository.findByMarketIdOrderByIdx(market.id()).get(0).getId();
        Long bettor = bettors.get(1);
        String key = "bet-race-" + UUID.randomUUID();
        BigDecimal balanceBefore = accountRepository.findByUserId(bettor).orElseThrow().getBalance();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<BetResponse>> futures = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                return betService.placeBet(bettor, market.id(), new BetRequest(
                        outcomeId, new BigDecimal("20.0000"), key));
            }));
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        Long betId = futures.get(0).get().betId();
        for (Future<BetResponse> future : futures) {
            assertThat(future.get().betId()).isEqualTo(betId);
        }
        assertThat(accountRepository.findByUserId(bettor).orElseThrow().getBalance())
                .isEqualByComparingTo(balanceBefore.subtract(new BigDecimal("20.0000")));
    }

    private List<Long> seedBettors() {
        Account treasury = accountRepository.findByType(AccountType.TREASURY).orElseThrow();
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < BETTORS; i++) {
            User user = userRepository.save(
                    new User("bettor_" + UUID.randomUUID().toString().replace("-", ""), "hash"));
            Account account = accountRepository.save(
                    new Account(user.getId(), AccountType.USER, BigDecimal.ZERO));
            ledgerService.record(TransactionType.MINT, null, List.of(
                    new EntrySpec(treasury.getId(), SEED_BALANCE.negate()),
                    new EntrySpec(account.getId(), SEED_BALANCE)));
            userIds.add(user.getId());
        }
        return userIds;
    }

    private BigDecimal totalBalance() {
        return accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

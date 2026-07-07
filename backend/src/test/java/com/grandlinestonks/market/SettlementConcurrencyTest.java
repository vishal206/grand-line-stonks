package com.grandlinestonks.market;

import static org.assertj.core.api.Assertions.assertThat;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.account.AccountType;
import com.grandlinestonks.error.IllegalMarketTransitionException;
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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class SettlementConcurrencyTest {

    private static final int RACERS = 8;

    @Autowired
    private MarketService marketService;

    @Autowired
    private BetService betService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketOutcomeRepository outcomeRepository;

    @Test
    void racingSettlementsPayOutExactlyOnce() throws Exception {
        Long owner = seedUser();
        Long bettor = seedUser();
        MarketResponse market = marketService.create(owner, new CreateMarketRequest(
                "Does the log pose point north?", List.of("Yes", "No"), new BigDecimal("100")));
        marketService.open(owner, market.id());
        Long winningOutcome = outcomeRepository.findByMarketIdOrderByIdx(market.id()).get(0).getId();
        BetResponse bet = betService.placeBet(bettor, market.id(), new BetRequest(
                winningOutcome, new BigDecimal("80.0000"), UUID.randomUUID().toString()));
        marketService.close(owner, market.id());
        settlementService.resolve(owner, market.id(), winningOutcome);
        BigDecimal balanceBeforeSettle =
                accountRepository.findByUserId(bettor).orElseThrow().getBalance();

        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(RACERS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < RACERS; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    settlementService.settle(owner, market.id());
                    succeeded.incrementAndGet();
                } catch (IllegalMarketTransitionException e) {
                    rejected.incrementAndGet();
                }
                return null;
            }));
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(60, TimeUnit.SECONDS)).isTrue();
        for (Future<?> future : futures) {
            future.get();
        }

        assertThat(succeeded.get()).isEqualTo(1);
        assertThat(rejected.get()).isEqualTo(RACERS - 1);
        assertThat(accountRepository.findByUserId(bettor).orElseThrow().getBalance())
                .isEqualByComparingTo(balanceBeforeSettle.add(bet.shares()));
        for (Account account : accountRepository.findAll()) {
            assertThat(ledgerService.recomputeBalance(account.getId()))
                    .isEqualByComparingTo(account.getBalance());
        }
    }

    private Long seedUser() {
        Account treasury = accountRepository.findByType(AccountType.TREASURY).orElseThrow();
        User user = userRepository.save(
                new User("racer_" + UUID.randomUUID().toString().replace("-", ""), "hash"));
        Account account = accountRepository.save(
                new Account(user.getId(), AccountType.USER, BigDecimal.ZERO));
        ledgerService.record(TransactionType.MINT, null, List.of(
                new EntrySpec(treasury.getId(), new BigDecimal("-1000.0000")),
                new EntrySpec(account.getId(), new BigDecimal("1000.0000"))));
        return user.getId();
    }
}

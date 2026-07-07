package com.grandlinestonks.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.account.AccountType;
import com.grandlinestonks.error.InsufficientBalanceException;
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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransferConcurrencyTest {

    private static final int ACCOUNTS = 5;
    private static final int TRANSFERS = 200;
    private static final int THREADS = 16;
    private static final BigDecimal SEED_BALANCE = new BigDecimal("1000.0000");

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LedgerTransactionRepository transactionRepository;

    @Autowired
    private LedgerEntryRepository entryRepository;

    @Test
    void concurrentTransfersConserveTotalBerries() throws Exception {
        List<Account> accounts = seedAccounts();
        BigDecimal totalBefore = totalBalance();
        AtomicInteger rejected = new AtomicInteger();
        Random random = new Random(42);
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < TRANSFERS; i++) {
            int from = random.nextInt(ACCOUNTS);
            int to = (from + 1 + random.nextInt(ACCOUNTS - 1)) % ACCOUNTS;
            pairs.add(new int[] {from, to, 1 + random.nextInt(500)});
        }

        runConcurrently(pairs, accounts, rejected);

        assertThat(totalBalance()).isEqualByComparingTo(totalBefore);
        assertThat(entryRepository.sumAll()).isEqualByComparingTo(BigDecimal.ZERO);
        for (Account account : accounts) {
            Account fresh = accountRepository.findById(account.getId()).orElseThrow();
            assertThat(fresh.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(ledgerService.recomputeBalance(fresh.getId()))
                    .isEqualByComparingTo(fresh.getBalance());
        }
    }

    @Test
    void concurrentSameIdempotencyKeyCreatesOneTransaction() throws Exception {
        List<Account> accounts = seedAccounts();
        Account from = accounts.get(0);
        Account to = accounts.get(1);
        String key = "race-" + UUID.randomUUID();
        List<EntrySpec> entries = List.of(
                new EntrySpec(from.getId(), new BigDecimal("-10.0000")),
                new EntrySpec(to.getId(), new BigDecimal("10.0000")));

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Long>> results = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            results.add(executor.submit(() -> {
                start.await();
                try {
                    return ledgerService.record(TransactionType.TRANSFER, key, entries).getId();
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    return ledgerService.findByIdempotencyKey(key).orElseThrow().getId();
                }
            }));
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        Long firstId = results.get(0).get();
        for (Future<Long> result : results) {
            assertThat(result.get()).isEqualTo(firstId);
        }
        assertThat(accountRepository.findById(from.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo(SEED_BALANCE.subtract(new BigDecimal("10.0000")));
    }

    private List<Account> seedAccounts() {
        Account treasury = accountRepository.findByType(AccountType.TREASURY).orElseThrow();
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < ACCOUNTS; i++) {
            User user = userRepository.save(
                    new User("conc_" + UUID.randomUUID().toString().replace("-", ""), "hash"));
            Account account = accountRepository.save(
                    new Account(user.getId(), AccountType.USER, BigDecimal.ZERO));
            ledgerService.record(TransactionType.MINT, null, List.of(
                    new EntrySpec(treasury.getId(), SEED_BALANCE.negate()),
                    new EntrySpec(account.getId(), SEED_BALANCE)));
            accounts.add(account);
        }
        return accounts;
    }

    private void runConcurrently(
            List<int[]> pairs, List<Account> accounts, AtomicInteger rejected) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int[] pair : pairs) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    ledgerService.record(TransactionType.TRANSFER, null, List.of(
                            new EntrySpec(accounts.get(pair[0]).getId(),
                                    BigDecimal.valueOf(-pair[2], 2)),
                            new EntrySpec(accounts.get(pair[1]).getId(),
                                    BigDecimal.valueOf(pair[2], 2))));
                } catch (InsufficientBalanceException e) {
                    rejected.incrementAndGet();
                }
                return null;
            }));
        }
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(120, TimeUnit.SECONDS)).isTrue();
        for (Future<?> future : futures) {
            future.get();
        }
    }

    private BigDecimal totalBalance() {
        return accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

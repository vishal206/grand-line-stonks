package com.grandlinestonks.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.grandlinestonks.TestcontainersConfiguration;
import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.account.AccountType;
import com.grandlinestonks.error.InsufficientBalanceException;
import com.grandlinestonks.error.UnbalancedTransactionException;
import com.grandlinestonks.user.User;
import com.grandlinestonks.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LedgerServiceIntegrationTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LedgerTransactionRepository transactionRepository;

    private Account newFundedAccount(BigDecimal balance) {
        User user = userRepository.save(
                new User("ledger_" + UUID.randomUUID().toString().replace("-", ""), "hash"));
        Account account = accountRepository.save(
                new Account(user.getId(), AccountType.USER, BigDecimal.ZERO));
        if (balance.signum() > 0) {
            Account treasury = accountRepository.findByType(AccountType.TREASURY).orElseThrow();
            ledgerService.record(TransactionType.MINT, null, List.of(
                    new EntrySpec(treasury.getId(), balance.negate()),
                    new EntrySpec(account.getId(), balance)));
        }
        return account;
    }

    @Test
    void transferMovesBalanceAndMatchesLedgerRecomputation() {
        Account from = newFundedAccount(new BigDecimal("100.0000"));
        Account to = newFundedAccount(BigDecimal.ZERO);

        ledgerService.record(TransactionType.TRANSFER, null, List.of(
                new EntrySpec(from.getId(), new BigDecimal("-40.0000")),
                new EntrySpec(to.getId(), new BigDecimal("40.0000"))));

        Account fromAfter = accountRepository.findById(from.getId()).orElseThrow();
        Account toAfter = accountRepository.findById(to.getId()).orElseThrow();
        assertThat(fromAfter.getBalance()).isEqualByComparingTo("60.0000");
        assertThat(toAfter.getBalance()).isEqualByComparingTo("40.0000");
        assertThat(ledgerService.recomputeBalance(from.getId()))
                .isEqualByComparingTo(fromAfter.getBalance());
        assertThat(ledgerService.recomputeBalance(to.getId()))
                .isEqualByComparingTo(toAfter.getBalance());
    }

    @Test
    void unbalancedEntriesAreRejected() {
        Account a = newFundedAccount(new BigDecimal("10.0000"));
        Account b = newFundedAccount(BigDecimal.ZERO);

        assertThatThrownBy(() -> ledgerService.record(TransactionType.TRANSFER, null, List.of(
                new EntrySpec(a.getId(), new BigDecimal("-5.0000")),
                new EntrySpec(b.getId(), new BigDecimal("4.9999")))))
                .isInstanceOf(UnbalancedTransactionException.class);
    }

    @Test
    void singleEntryTransactionIsRejected() {
        Account a = newFundedAccount(new BigDecimal("10.0000"));

        assertThatThrownBy(() -> ledgerService.record(TransactionType.TRANSFER, null,
                List.of(new EntrySpec(a.getId(), new BigDecimal("5.0000")))))
                .isInstanceOf(UnbalancedTransactionException.class);
    }

    @Test
    void overdraftIsRejectedAndNothingPersists(){
        Account from = newFundedAccount(new BigDecimal("10.0000"));
        Account to = newFundedAccount(BigDecimal.ZERO);
        long transactionsBefore = transactionRepository.count();

        assertThatThrownBy(() -> ledgerService.record(TransactionType.TRANSFER, null, List.of(
                new EntrySpec(from.getId(), new BigDecimal("-10.0001")),
                new EntrySpec(to.getId(), new BigDecimal("10.0001")))))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(transactionRepository.count()).isEqualTo(transactionsBefore);
        assertThat(accountRepository.findById(from.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("10.0000");
    }

    @Test
    void repeatedIdempotencyKeyReturnsOriginalTransaction() {
        Account from = newFundedAccount(new BigDecimal("50.0000"));
        Account to = newFundedAccount(BigDecimal.ZERO);
        String key = "idem-" + UUID.randomUUID();
        List<EntrySpec> entries = List.of(
                new EntrySpec(from.getId(), new BigDecimal("-5.0000")),
                new EntrySpec(to.getId(), new BigDecimal("5.0000")));

        LedgerTransaction first = ledgerService.record(TransactionType.TRANSFER, key, entries);
        LedgerTransaction second = ledgerService.record(TransactionType.TRANSFER, key, entries);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(accountRepository.findById(from.getId()).orElseThrow().getBalance())
                .isEqualByComparingTo("45.0000");
    }
}

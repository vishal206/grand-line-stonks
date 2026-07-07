package com.grandlinestonks.ledger;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.common.Berries;
import com.grandlinestonks.error.AccountNotFoundException;
import com.grandlinestonks.error.InsufficientBalanceException;
import com.grandlinestonks.error.UnbalancedTransactionException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerEntryRepository entryRepository;
    private final AccountRepository accountRepository;

    public LedgerService(
            LedgerTransactionRepository transactionRepository,
            LedgerEntryRepository entryRepository,
            AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public LedgerTransaction record(TransactionType type, String idempotencyKey, List<EntrySpec> entries) {
        if (idempotencyKey != null) {
            Optional<LedgerTransaction> existing =
                    transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        validate(entries);
        Map<Long, Account> accounts = lockAccounts(entries);
        applyToBalances(entries, accounts);
        return persist(type, idempotencyKey, entries);
    }

    @Transactional(readOnly = true)
    public Optional<LedgerTransaction> findByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Transactional(readOnly = true)
    public BigDecimal recomputeBalance(Long accountId) {
        return Berries.normalize(entryRepository.sumByAccountId(accountId));
    }

    private void validate(List<EntrySpec> entries) {
        if (entries.size() < 2) {
            throw new UnbalancedTransactionException("a transaction needs at least two entries");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (EntrySpec entry : entries) {
            if (entry.amount().signum() == 0) {
                throw new UnbalancedTransactionException("zero-amount entry for account " + entry.accountId());
            }
            if (entry.amount().scale() > Berries.SCALE) {
                throw new UnbalancedTransactionException(
                        "amount scale exceeds " + Berries.SCALE + " for account " + entry.accountId());
            }
            sum = sum.add(entry.amount());
        }
        if (sum.signum() != 0) {
            throw new UnbalancedTransactionException("entries sum to " + sum + ", expected 0");
        }
    }

    private Map<Long, Account> lockAccounts(List<EntrySpec> entries) {
        List<Long> ids = entries.stream().map(EntrySpec::accountId).distinct().sorted().toList();
        Map<Long, Account> accounts = accountRepository.lockAllByIdIn(ids).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        for (Long id : ids) {
            if (!accounts.containsKey(id)) {
                throw new AccountNotFoundException("id " + id);
            }
        }
        return accounts;
    }

    private void applyToBalances(List<EntrySpec> entries, Map<Long, Account> accounts) {
        for (EntrySpec entry : entries) {
            Account account = accounts.get(entry.accountId());
            account.credit(entry.amount());
            if (account.getBalance().signum() < 0 && !account.mayGoNegative()) {
                throw new InsufficientBalanceException(account.getId());
            }
        }
    }

    private LedgerTransaction persist(TransactionType type, String idempotencyKey, List<EntrySpec> entries) {
        LedgerTransaction transaction =
                transactionRepository.save(new LedgerTransaction(type, idempotencyKey));
        entryRepository.saveAll(entries.stream()
                .map(entry -> new LedgerEntry(transaction.getId(), entry.accountId(), entry.amount()))
                .toList());
        return transaction;
    }
}

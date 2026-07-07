package com.grandlinestonks.transfer;

import com.grandlinestonks.account.Account;
import com.grandlinestonks.account.AccountRepository;
import com.grandlinestonks.error.AccountNotFoundException;
import com.grandlinestonks.error.InvalidTransferException;
import com.grandlinestonks.ledger.EntrySpec;
import com.grandlinestonks.ledger.LedgerService;
import com.grandlinestonks.ledger.LedgerTransaction;
import com.grandlinestonks.ledger.TransactionType;
import com.grandlinestonks.transfer.dto.TransferResponse;
import com.grandlinestonks.user.User;
import com.grandlinestonks.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final LedgerService ledgerService;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransferService(
            LedgerService ledgerService,
            AccountRepository accountRepository,
            UserRepository userRepository) {
        this.ledgerService = ledgerService;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public TransferResponse transfer(
            Long fromUserId, String toUsername, BigDecimal amount, String idempotencyKey) {
        Account from = accountRepository.findByUserId(fromUserId)
                .orElseThrow(() -> new AccountNotFoundException("user id " + fromUserId));
        User toUser = userRepository.findByUsername(toUsername)
                .orElseThrow(() -> new AccountNotFoundException("username " + toUsername));
        Account to = accountRepository.findByUserId(toUser.getId())
                .orElseThrow(() -> new AccountNotFoundException("username " + toUsername));
        if (from.getId().equals(to.getId())) {
            throw new InvalidTransferException("cannot transfer to yourself");
        }
        LedgerTransaction transaction = recordIdempotent(from, to, amount, idempotencyKey);
        return new TransferResponse(transaction.getId(), toUsername, amount);
    }

    private LedgerTransaction recordIdempotent(
            Account from, Account to, BigDecimal amount, String idempotencyKey) {
        List<EntrySpec> entries = List.of(
                new EntrySpec(from.getId(), amount.negate()),
                new EntrySpec(to.getId(), amount));
        try {
            return ledgerService.record(TransactionType.TRANSFER, idempotencyKey, entries);
        } catch (DataIntegrityViolationException e) {
            return ledgerService.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> e);
        }
    }
}

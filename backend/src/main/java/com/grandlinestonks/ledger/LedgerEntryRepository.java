package com.grandlinestonks.ledger;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransactionId(Long transactionId);

    @Query("select coalesce(sum(e.amount), 0) from LedgerEntry e where e.accountId = :accountId")
    BigDecimal sumByAccountId(@Param("accountId") Long accountId);

    @Query("select coalesce(sum(e.amount), 0) from LedgerEntry e")
    BigDecimal sumAll();
}

package com.grandlinestonks.market;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {

    Optional<Bet> findByTransactionId(Long transactionId);

    List<Bet> findByMarketIdOrderByCreatedAt(Long marketId);
}

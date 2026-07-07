package com.grandlinestonks.market;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketRepository extends JpaRepository<Market, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Market m where m.id = :id")
    Optional<Market> lockById(@Param("id") Long id);

    List<Market> findByStatusOrderByCreatedAtDesc(MarketStatus status);
}

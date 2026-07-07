package com.grandlinestonks.market;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketRepository extends JpaRepository<Market, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Market m where m.id = :id")
    Optional<Market> lockById(@Param("id") Long id);

    Page<Market> findByStatus(MarketStatus status, Pageable pageable);
}

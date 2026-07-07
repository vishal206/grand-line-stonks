package com.grandlinestonks.market;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByUserIdAndOutcomeId(Long userId, Long outcomeId);

    List<Position> findByMarketId(Long marketId);

    Page<Position> findByUserId(Long userId, Pageable pageable);
}

package com.arena.ticketing.repository;
import com.arena.ticketing.model.MatchSectorPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface MatchSectorPriceRepository extends JpaRepository<MatchSectorPrice, Long> {
    Optional<MatchSectorPrice> findByMatchIdAndSectorId(Long matchId, Long sectorId);
}
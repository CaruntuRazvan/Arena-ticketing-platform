package com.arena.ticketing.repository;
import com.arena.ticketing.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import com.arena.ticketing.model.MatchStatus;
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    // Gaseste toate meciurile unde matchDate este mai mare (dupa) data trimisa ca parametru
    List<Match> findByMatchDateAfterAndStatus(LocalDateTime date, MatchStatus status);

    List<Match> findByMatchDateBeforeAndStatus(LocalDateTime now, MatchStatus matchStatus);
}
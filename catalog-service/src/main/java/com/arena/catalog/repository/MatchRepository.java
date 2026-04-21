package com.arena.catalog.repository;

import com.arena.catalog.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import com.arena.catalog.model.MatchStatus;
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByMatchDateAfterAndStatus(LocalDateTime date, MatchStatus status);

    List<Match> findByMatchDateBeforeAndStatus(LocalDateTime now, MatchStatus matchStatus);
}
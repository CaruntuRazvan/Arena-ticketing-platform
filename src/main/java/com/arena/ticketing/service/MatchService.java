package com.arena.ticketing.service;

import com.arena.ticketing.dto.MatchStatsDTO;
import com.arena.ticketing.dto.SectorAvailabilityDTO;
import com.arena.ticketing.model.Match;
import com.arena.ticketing.dto.MatchRequestDTO;
import com.arena.ticketing.dto.PriceRequestDTO;
import com.arena.ticketing.model.MatchStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MatchService {
    Match createMatch(MatchRequestDTO dto);
    void setMatchPrices(List<PriceRequestDTO> prices);
    Match saveMatch(Match match);
    List<Match> getAllMatches();
    Double getPriceForSector(Long matchId, Long sectorId);
    List<Match> getUpcomingMatches();
    MatchStatsDTO getMatchStatistics(Long id);

    List<SectorAvailabilityDTO> getSectorsAvailabilityForMatch(Long matchId);
    void updateMatchStatus(Long matchId, MatchStatus newStatus);
}
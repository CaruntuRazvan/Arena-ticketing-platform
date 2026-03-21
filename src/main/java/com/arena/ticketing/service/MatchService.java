package com.arena.ticketing.service;

import com.arena.ticketing.dto.*;
import com.arena.ticketing.model.Match;
import com.arena.ticketing.model.MatchStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MatchService {
    MatchDTO createMatch(MatchRequestDTO dto);
    void setMatchPrices(List<PriceRequestDTO> prices);
    Match saveMatch(Match match);
    List<MatchDTO> getAllMatchesDTO(); // Metodă nouă pentru Controller
    List<MatchDTO> getUpcomingMatchesDTO();
    Double getPriceForSector(Long matchId, Long sectorId);
    MatchStatsDTO getMatchStatistics(Long id);

    List<SectorAvailabilityDTO> getSectorsAvailabilityForMatch(Long matchId);
    void updateMatchStatus(Long matchId, MatchStatus newStatus);
}
package com.arena.catalog.service;

import com.arena.catalog.dto.*;
import com.arena.catalog.model.Match;
import com.arena.catalog.model.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MatchService {
    MatchDTO createMatch(MatchRequestDTO dto);
    void setMatchPrices(List<PriceRequestDTO> prices);
    Match saveMatch(Match match);
    /*
    List<MatchDTO> getAllMatchesDTO(); // Metodă nouă pentru Controller
    List<MatchDTO> getUpcomingMatchesDTO();
    */
    Page<MatchDTO> getAllMatchesDTO(Pageable pageable);
    Page<MatchDTO> getUpcomingMatchesDTO(Pageable pageable);

    Double getPriceForSector(Long matchId, Long sectorId);
    void updateMatchStatus(Long matchId, MatchStatus newStatus);
    void deleteMatch(Long id);
    MatchDTO getMatchById(Long id);
    Double getSectorPrice(Long matchId, Long sectorId);
    void publishMatch(Long matchId);
}
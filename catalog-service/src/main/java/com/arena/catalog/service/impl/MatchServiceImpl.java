package com.arena.catalog.service.impl;

import com.arena.catalog.model.*;
import com.arena.catalog.dto.*;
import com.arena.catalog.repository.*;
import com.arena.catalog.service.MatchService;
import com.arena.catalog.exception.CatalogException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchSectorPriceRepository matchSectorPriceRepository;
    private final StadiumRepository stadiumRepository;
    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;

    private Double getRequiredPrice(Long matchId, Long sectorId) {
        return matchSectorPriceRepository.findByMatchIdAndSectorId(matchId, sectorId)
                .map(MatchSectorPrice::getPrice)
                .orElseThrow(() -> new CatalogException("Prețul pentru sectorul " + sectorId + " nu a fost configurat!"));
    }

    @Override
    @CacheEvict(value = {"allMatchesCache", "upcomingMatchesCache"}, allEntries = true)
    public MatchDTO createMatch(MatchRequestDTO dto) {
        log.info(">>> Meci creat. Curăț cache-ul pentru liste.");
        Stadium stadium = stadiumRepository.findById(dto.getStadiumId())
                .orElseThrow(() -> new CatalogException("Stadionul nu a fost găsit!"));

        Match match = new Match();
        match.setOpponentName(dto.getOpponentName());
        match.setMatchDate(dto.getMatchDate());
        match.setStadium(stadium);
        match.setStatus(MatchStatus.SCHEDULED);

        Match savedMatch = matchRepository.save(match);
        return mapToDTO(savedMatch);
    }
    @Override
    @Cacheable(value = "allMatchesCache")
    public List<MatchDTO> getAllMatchesDTO() {
        log.info(">>> Cache MISS pentru ALL matches");
        return matchRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "upcomingMatchesCache")
    public List<MatchDTO> getUpcomingMatchesDTO() {
        log.info(">>> Cache MISS pentru UPCOMING matches");
        return matchRepository.findByMatchDateAfterAndStatus(LocalDateTime.now(), MatchStatus.SCHEDULED)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "sectorPriceCache", allEntries = true)
    public void setMatchPrices(List<PriceRequestDTO> prices) {
        log.info(">>> Prețuri actualizate. Curăț cache prețuri.");
        for (PriceRequestDTO dto : prices) {
            Match match = matchRepository.findById(dto.getMatchId())
                    .orElseThrow(() -> new CatalogException("Meci negăsit"));
            Sector sector = sectorRepository.findById(dto.getSectorId())
                    .orElseThrow(() -> new CatalogException("Sector negăsit"));

            // Folosim numele corect: matchSectorPriceRepository
            MatchSectorPrice msp = matchSectorPriceRepository
                    .findByMatchIdAndSectorId(dto.getMatchId(), dto.getSectorId())
                    .orElse(new MatchSectorPrice());

            msp.setMatch(match);
            msp.setSector(sector);
            msp.setPrice(dto.getPrice());

            matchSectorPriceRepository.save(msp);
        }
    }

    @Override
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }


    @Override
    public Double getPriceForSector(Long matchId, Long sectorId) {

        return getRequiredPrice(matchId, sectorId);
    }



    @Override
    @Transactional
    @CacheEvict(value = {"allMatchesCache", "upcomingMatchesCache", "matchDetailsCache"}, allEntries = true)
    public void updateMatchStatus(Long matchId, MatchStatus newStatus) {
        log.info(">>> Status meci {} modificat în {}. Reset cache.", matchId, newStatus);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new CatalogException("Meciul nu a fost găsit!"));

        // Verificăm dacă nu cumva meciul este deja anulat
        if (match.getStatus() == MatchStatus.CANCELLED) {
            throw new CatalogException("Nu poți schimba statusul unui meci anulat!");
        }

        match.setStatus(newStatus);
        matchRepository.save(match);
    }

    // rulare zilnic la ora 00:00
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoUpdateMatchStatus() {
        // Luăm toate meciurile programate care au data înainte de "acum"
        List<Match> pastMatches = matchRepository.findByMatchDateBeforeAndStatus(
                LocalDateTime.now(),
                MatchStatus.SCHEDULED
        );

        for (Match match : pastMatches) {
            match.setStatus(MatchStatus.FINISHED);
        }

        if (!pastMatches.isEmpty()) {
            matchRepository.saveAll(pastMatches);
            System.out.println("Automatizare: " + pastMatches.size() + " meciuri au fost trecute în status FINISHED.");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allMatchesCache", "upcomingMatchesCache"}, allEntries = true)
    public void deleteMatch(Long id) {
        log.info(">>> Meci șters {}. Reset cache.", id);
        if (!matchRepository.existsById(id)) {
            throw new CatalogException("Meciul cu ID-ul " + id + " nu există!");
        }
        matchRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "matchDetailsCache", key = "#id")
    public MatchDTO getMatchById(Long id) {
        log.info(">>> Cache MISS pentru detalii meci ID: {}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new CatalogException("Meciul cu ID-ul " + id + " nu a fost găsit"));

        // Folosim metoda mapToDTO pe care o ai deja definită mai jos pentru consistență
        return mapToDTO(match);
    }

    @Override
    @Cacheable(value = "sectorPriceCache", key = "{#matchId, #sectorId}")
    public Double getSectorPrice(Long matchId, Long sectorId) {
        log.info(">>> Cache MISS pentru preț sector: {} la meciul: {}", sectorId, matchId);
        // Folosim repository-ul tău: matchSectorPriceRepository
        return matchSectorPriceRepository.findByMatchIdAndSectorId(matchId, sectorId)
                .map(MatchSectorPrice::getPrice)
                .orElseThrow(() -> new CatalogException("Prețul nu este configurat pentru acest sector!"));
    }

    private MatchDTO mapToDTO(Match match) {
        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setOpponentName(match.getOpponentName());
        dto.setMatchDate(match.getMatchDate());
        dto.setStatus(match.getStatus());
        dto.setStadiumName(match.getStadium().getName());
        return dto;
    }
}
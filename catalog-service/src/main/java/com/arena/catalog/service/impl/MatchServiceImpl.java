package com.arena.catalog.service.impl;

import com.arena.catalog.client.NotificationClient;
import com.arena.catalog.model.*;
import com.arena.catalog.dto.*;
import com.arena.catalog.repository.*;
import com.arena.catalog.service.MatchService;
import com.arena.catalog.exception.CatalogException;
import com.arena.catalog.util.SerializablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final NotificationClient notificationClient;

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
        match.setMatchImageUrl(dto.getMatchImageUrl());

        Match savedMatch = matchRepository.save(match);
        return mapToDTO(savedMatch);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allMatchesCache", "upcomingMatchesCache", "matchDetailsCache"}, allEntries = true)
    public MatchDTO updateMatch(Long id, MatchRequestDTO dto) {
        log.info(">>> Se actualizează detaliile meciului cu ID {}. Reset cache.", id);

        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new CatalogException("Meciul cu ID-ul " + id + " nu a fost găsit."));

        Stadium stadium = stadiumRepository.findById(dto.getStadiumId())
                .orElseThrow(() -> new CatalogException("Stadionul nu a fost găsit!"));

        match.setOpponentName(dto.getOpponentName());
        match.setMatchDate(dto.getMatchDate());
        match.setStadium(stadium);
        match.setMatchImageUrl(dto.getMatchImageUrl());

        Match updatedMatch = matchRepository.save(match);
        return mapToDTO(updatedMatch);
    }
    /*
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
    */

    @Override
    //@Cacheable(value = "allMatchesCache", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<MatchDTO> getAllMatchesDTO(Pageable pageable) {
        log.info(">>> Cache MISS pentru ALL matches (Page: {})", pageable.getPageNumber());
        return matchRepository.findAll(pageable)
                .map(this::mapToDTO); // .map pe Page păstrează metadatele de paginare
    }
    /*
    @Override
    //@Cacheable(value = "upcomingMatchesCache", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<MatchDTO> getUpcomingMatchesDTO(Pageable pageable) {
        log.info(">>> Cache MISS pentru UPCOMING matches (Page: {})", pageable.getPageNumber());
        return matchRepository.findByMatchDateAfterAndStatusAndIsPublishedTrue(
                LocalDateTime.now(),
                MatchStatus.SCHEDULED,
                pageable
        ).map(this::mapToDTO);
    }
    */
    @Cacheable(value = "upcomingMatchesCache",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public SerializablePage<MatchDTO> getUpcomingMatchesDTO(Pageable pageable) {
        log.info(">>> Cache MISS pentru UPCOMING matches (Page: {})", pageable.getPageNumber());

        Page<MatchDTO> page = matchRepository
                .findByMatchDateAfterAndStatusAndIsPublishedTrue(
                        LocalDateTime.now(),
                        MatchStatus.SCHEDULED,
                        pageable
                ).map(this::mapToDTO);

        return new SerializablePage<>(page);
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
    @Transactional
    @CacheEvict(value = {"upcomingMatchesCache", "allMatchesCache"}, allEntries = true)
    public void publishMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new CatalogException("Meciul nu a fost găsit!"));

        // are preturi configurate?
        boolean hasPrices = matchSectorPriceRepository.existsByMatchId(matchId);
        if (!hasPrices) {
            throw new CatalogException("Nu poți publica meciul fără a configura prețurile sectoarelor!");
        }

        // schimbam in publish
        match.setPublished(true);
        matchRepository.save(match);

        log.info(">>> Meciul {} a fost publicat!", matchId);

        MatchNotificationRequestDTO notification = new MatchNotificationRequestDTO(
                match.getOpponentName(),
                match.getMatchDate().toString(),
                match.getStadium().getName(),
                null
        );

        // Apelul propriu-zis către celălalt microserviciu
        try {
            notificationClient.broadcastMatch(notification);
            log.info(">>> Notificarea de broadcast a fost trimisă către Notification Service.");
        } catch (Exception e) {
            // Logăm eroarea dar NU dăm rollback la tranzacție (meciul rămâne publicat chiar dacă notificarea a eșuat momentan)
            log.error(">>> Eroare la trimiterea notificării prin Feign: {}", e.getMessage());
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
        log.info(">>> Începe procesul de ștergere în cascadă pentru meciul {}. Reset cache.", id);

        if (!matchRepository.existsById(id)) {
            throw new CatalogException("Meciul cu ID-ul " + id + " nu există!");
        }

        matchSectorPriceRepository.deleteByMatchId(id);
        log.info(">>> Toate prețurile sectoarelor atașate meciului {} au fost șterse.", id);

        matchRepository.deleteById(id);
        log.info(">>> Meciul {} a fost eliminat definitiv din catalog.", id);
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

    @Override
    public SectorDTO getSectorDetailsByName(Long matchId, String sectorName) {
        // 1. Identificăm meciul
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new CatalogException("Meciul cu ID-ul " + matchId + " nu a fost găsit."));

        // 2. Găsim sectorul - Dacă aici crapă, înseamnă că numele de pe imagine nu e în DB
        Sector sector = sectorRepository.findByStadiumIdAndName(match.getStadium().getId(), sectorName)
                .orElseThrow(() -> new CatalogException("Configurația pentru '" + sectorName + "' nu a fost găsită în baza de date a stadionului."));

        // 3. Verificăm prețul - Logica complexă cerută
        Double price = matchSectorPriceRepository.findByMatchIdAndSectorId(matchId, sector.getId())
                .map(MatchSectorPrice::getPrice)
                .orElseThrow(() -> new CatalogException("Sectorul " + sectorName + " este definit, dar nu are prețuri setate pentru acest meci specific. Contactați administratorul."));

        // 4. Verificăm dacă există locuri generate (pentru a evita un grid gol)
        List<Seat> seats = sector.getSeats();
        if (seats == null || seats.isEmpty()) {
            throw new CatalogException("Sectorul " + sectorName + " este disponibil, dar locurile nu au fost încă generate.");
        }

        // 5. Calculăm dimensiunile (Logica ta existentă)
        int maxRows = seats.stream().mapToInt(Seat::getRowNumber).max().orElse(0);
        int maxSeatsPerRow = seats.stream().mapToInt(Seat::getSeatNumber).max().orElse(0);

        // 6. Mapăm în SectorDTO
        SectorDTO dto = new SectorDTO();
        dto.setId(sector.getId());
        dto.setName(sector.getName());
        dto.setBasePrice(price);
        dto.setStadiumId(match.getStadium().getId());
        dto.setRows(maxRows);
        dto.setSeatsPerRow(maxSeatsPerRow);
        dto.setTotalSeats(seats.size());

        return dto;
    }

    private MatchDTO mapToDTO(Match match) {
        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setOpponentName(match.getOpponentName());
        dto.setMatchImageUrl(match.getMatchImageUrl());
        dto.setMatchDate(match.getMatchDate());
        dto.setStatus(match.getStatus());
        dto.setStadiumName(match.getStadium().getName());
        dto.setPublished(match.isPublished());
        return dto;
    }
}
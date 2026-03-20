package com.arena.ticketing.service.impl;

import com.arena.ticketing.model.*;
import com.arena.ticketing.dto.*;
import com.arena.ticketing.repository.*;
import com.arena.ticketing.service.MatchService;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.util.MathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Adaugă importul acesta!
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchSectorPriceRepository matchSectorPriceRepository;
    private final StadiumRepository stadiumRepository;
    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    private Double getRequiredPrice(Long matchId, Long sectorId) {
        return matchSectorPriceRepository.findByMatchIdAndSectorId(matchId, sectorId)
                .map(MatchSectorPrice::getPrice)
                .orElseThrow(() -> new TicketException("Prețul pentru sectorul " + sectorId + " nu a fost configurat!"));
    }

    @Override
    public Match createMatch(MatchRequestDTO dto) {
        Stadium stadium = stadiumRepository.findById(dto.getStadiumId())
                .orElseThrow(() -> new RuntimeException("Stadionul nu a fost găsit!"));

        Match match = new Match();
        match.setOpponentName(dto.getOpponentName());
        match.setMatchDate(dto.getMatchDate());
        match.setStadium(stadium);

        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public void setMatchPrices(List<PriceRequestDTO> prices) {
        for (PriceRequestDTO dto : prices) {
            Match match = matchRepository.findById(dto.getMatchId())
                    .orElseThrow(() -> new RuntimeException("Meci negăsit"));
            Sector sector = sectorRepository.findById(dto.getSectorId())
                    .orElseThrow(() -> new RuntimeException("Sector negăsit"));

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
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @Override
    public Double getPriceForSector(Long matchId, Long sectorId) {

        return getRequiredPrice(matchId, sectorId);
    }

    @Override
    public List<Match> getUpcomingMatches() {
        return matchRepository.findByMatchDateAfterAndStatus(LocalDateTime.now(), MatchStatus.SCHEDULED);
    }

    @Override
    public MatchStatsDTO getMatchStatistics(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TicketException("Meciul nu a fost găsit"));

        // 1. Capacitate totală stadion
        long totalCapacity = seatRepository.countBySectorStadiumId(match.getStadium().getId());

        // 2. Bilete vândute pentru acest meci
        long ticketsSold = ticketRepository.countByMatchId(matchId);

        // 3. Calcul procent total
        double overallPercentage = MathUtils.calculatePercentage(ticketsSold, totalCapacity);

        // 4. Calcul per sector (opțional, dar ai menționat că vrei și per sector)
        Map<String, Double> sectorStats = new HashMap<>();
        match.getStadium().getSectors().forEach(sector -> {
            long sectorCapacity = seatRepository.countBySectorId(sector.getId());
            long sectorSold = ticketRepository.countByMatchIdAndSeatSectorId(matchId, sector.getId());

            double rawSectorPercentage = (sectorCapacity == 0) ? 0 : (double) sectorSold / sectorCapacity * 100;
            sectorStats.put(sector.getName(), MathUtils.round(rawSectorPercentage,4));
        });

        return new MatchStatsDTO(matchId, match.getOpponentName(), totalCapacity, ticketsSold, overallPercentage, sectorStats);
    }

    @Override
    public List<SectorAvailabilityDTO> getSectorsAvailabilityForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TicketException("Meciul nu a fost găsit"));

        return match.getStadium().getSectors().stream().map(sector -> {
            // 1. locuri totale sector
            long totalCapacity = seatRepository.countBySectorId(sector.getId());

            // 2. bilete vandute in sector
            long soldTickets = ticketRepository.countByMatchIdAndSeatSectorId(matchId, sector.getId());

            long remaining = totalCapacity - soldTickets;

            Double price = getRequiredPrice(matchId, sector.getId());

            return new SectorAvailabilityDTO(
                    sector.getId(),
                    sector.getName(),
                    remaining,
                    remaining <= 0,
                    price
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMatchStatus(Long matchId, MatchStatus newStatus) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TicketException("Meciul nu a fost găsit!"));

        // Verificăm dacă nu cumva meciul este deja anulat
        if (match.getStatus() == MatchStatus.CANCELLED) {
            throw new TicketException("Nu poți schimba statusul unui meci anulat!");
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
}
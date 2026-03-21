package com.arena.ticketing.service.impl;

import com.arena.ticketing.dto.SeatDTO;
import com.arena.ticketing.model.Seat;
import com.arena.ticketing.model.MatchSectorPrice;
import com.arena.ticketing.repository.SeatRepository;
import com.arena.ticketing.repository.TicketRepository;
import com.arena.ticketing.repository.MatchRepository;
import com.arena.ticketing.repository.MatchSectorPriceRepository;
import com.arena.ticketing.service.SeatService;
import com.arena.ticketing.dto.SeatStatusDTO;
import com.arena.ticketing.exception.TicketException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository; // Adăugăm repository-ul pentru bilete
    private final MatchRepository matchRepository;
    private final MatchSectorPriceRepository matchSectorPriceRepository;


    @Override
    public List<SeatDTO> getSeatsBySector(Long sectorId) {
        return seatRepository.findBySectorId(sectorId)
                .stream()
                .map(seat -> new SeatDTO(
                        seat.getId(),
                        seat.getRowNumber(),
                        seat.getSeatNumber(),
                        seat.getSector().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatStatusDTO> getSeatsStatusByMatch(Long matchId, Long sectorId) {
        // 1. Validări de bază
        if (!matchRepository.existsById(matchId)) {
            throw new TicketException("Meciul dorit nu a fost găsit!");
        }

        // 2. Luăm prețul (o singură interogare)
        Double price = matchSectorPriceRepository.findByMatchIdAndSectorId(matchId, sectorId)
                .map(MatchSectorPrice::getPrice)
                .orElseThrow(() -> new TicketException("Prețul pentru acest sector nu a fost configurat!"));

        // 3. Luăm toate locurile din sector
        List<Seat> allSeats = seatRepository.findBySectorId(sectorId);
        if (allSeats.isEmpty()) {
            throw new TicketException("Sectorul nu are locuri configurate.");
        }

        // 4. OPTIMIZARE: Luăm toate ID-urile locurilor deja ocupate pentru acest meci și sector
        // Avem nevoie de o metodă în TicketRepository: findOccupiedSeatIds(matchId, sectorId)
        List<Long> occupiedSeatIds = ticketRepository.findOccupiedSeatIdsByMatchAndSector(matchId, sectorId);

        // 5. Mapăm rapid în memorie
        return allSeats.stream().map(seat -> {
            boolean isAvailable = !occupiedSeatIds.contains(seat.getId());

            return new SeatStatusDTO(
                    seat.getId(),
                    seat.getRowNumber(),
                    seat.getSeatNumber(),
                    isAvailable,
                    price
            );
        }).collect(Collectors.toList());
    }
}
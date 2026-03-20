package com.arena.ticketing.service.impl;

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
    public List<Seat> getSeatsBySector(Long sectorId) {
        return seatRepository.findBySectorId(sectorId);
    }

    @Override
    public List<SeatStatusDTO> getSeatsStatusByMatch(Long matchId, Long sectorId) {

        if (!matchRepository.existsById(matchId)) {
            throw new TicketException("Meciul dorit nu a fost găsit!");
        }

        // 2. Validăm existența sectorului (opțional, dar recomandat)
        if (!seatRepository.existsBySectorId(sectorId)) {
            // Dacă nu există niciun scaun în acest sector, probabil sectorul nu există
            throw new TicketException("Sectorul dorit nu există sau nu are locuri.");
        }

        Double price = matchSectorPriceRepository.findByMatchIdAndSectorId(matchId, sectorId)
                .map(MatchSectorPrice::getPrice)
                .orElseThrow(() -> new TicketException("Prețul pentru acest sector nu a fost configurat!"));

        List<Seat> allSeats = seatRepository.findBySectorId(sectorId);

        return allSeats.stream().map(seat -> {
            // Verificăm dacă există bilet pentru acest meci și acest loc
            boolean isTaken = ticketRepository.existsByMatchIdAndSeatId(matchId, seat.getId());


            return new SeatStatusDTO(
                    seat.getId(),
                    seat.getRowNumber(),
                    seat.getSeatNumber(),
                    !isTaken, // Este disponibil dacă NU este luat,
                    price
            );
        }).collect(Collectors.toList());
    }
}
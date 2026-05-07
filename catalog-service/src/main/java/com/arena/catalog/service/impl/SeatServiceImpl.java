package com.arena.catalog.service.impl;

import com.arena.catalog.client.TicketingClient;
import com.arena.catalog.dto.SeatDTO;
import com.arena.catalog.exception.CatalogException;
import com.arena.catalog.model.Seat;
import com.arena.catalog.repository.SeatRepository;
import com.arena.catalog.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final TicketingClient ticketingClient;

    @Override
    @Cacheable(value = "seatsBySectorCache", key = "#sectorId")
    public List<SeatDTO> getSeatsBySector(Long sectorId) {
        log.info(">>> Cache MISS pentru locurile din sectorul: {}", sectorId);
        return seatRepository.findBySectorId(sectorId)
                .stream()
                .map(seat -> new SeatDTO(
                        seat.getId(),
                        seat.getRowNumber(),
                        seat.getSeatNumber(),
                        seat.getSector().getId(),
                        false))
                .collect(Collectors.toList());
    }
    @Override
    @Cacheable(value = "seatCache", key = "#id")
    public SeatDTO getSeatById(Long id) {
        log.info(">>> Cache MISS pentru locul cu ID: {}", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new CatalogException("Locul nu a fost găsit!"));

        return new SeatDTO(
                seat.getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                seat.getSector().getId(),
                false
        );
    }

    @Override
    public List<SeatDTO> getSeatsBySector(Long matchId, Long sectorId) {
        List<Seat> allSeats = seatRepository.findBySectorId(sectorId);

        List<Long> allSeatIds = allSeats.stream()
                .map(Seat::getId)
                .collect(Collectors.toList());

        List<Long> occupiedIds = ticketingClient.getOccupiedSeats(matchId, allSeatIds);

        Set<Long> occupiedSet = new HashSet<>(occupiedIds);

        return allSeats.stream()
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    dto.setId(seat.getId());
                    dto.setRowNumber(seat.getRowNumber());
                    dto.setSeatNumber(seat.getSeatNumber());
                    dto.setSectorId(sectorId);
                    // Aici se face magia: dacă ID-ul este în lista de la Ticketing, e roșu (occupied)
                    dto.setOccupied(occupiedSet.contains(seat.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
package com.arena.catalog.service.impl;

import com.arena.catalog.dto.SeatDTO;
import com.arena.catalog.exception.CatalogException;
import com.arena.catalog.model.Seat;
import com.arena.catalog.repository.SeatRepository;
import com.arena.catalog.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;


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
    public SeatDTO getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new CatalogException("Locul nu a fost găsit!"));

        return new SeatDTO(
                seat.getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                seat.getSector().getId()
        );
    }
}
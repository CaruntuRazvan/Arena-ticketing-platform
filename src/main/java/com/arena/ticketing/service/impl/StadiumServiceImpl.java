package com.arena.ticketing.service.impl;

import com.arena.ticketing.dto.StadiumDTO;
import com.arena.ticketing.model.Stadium;
import com.arena.ticketing.repository.StadiumRepository;
import com.arena.ticketing.repository.SectorRepository;
import com.arena.ticketing.repository.SeatRepository;
import com.arena.ticketing.service.StadiumService;
import com.arena.ticketing.dto.SectorRequestDTO;
import com.arena.ticketing.model.Sector;
import com.arena.ticketing.model.Seat;
import org.springframework.stereotype.Service;
import com.arena.ticketing.exception.TicketException;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class StadiumServiceImpl implements StadiumService {

    private final StadiumRepository stadiumRepository;
    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;

    @Override
    public StadiumDTO createStadium(StadiumDTO dto) {
        Stadium stadium = new Stadium();
        stadium.setName(dto.getName());
        stadium.setLocation(dto.getLocation());

        Stadium saved = stadiumRepository.save(stadium);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    public List<StadiumDTO> getAllStadiums() {
        return stadiumRepository.findAll().stream().map(s -> {
            StadiumDTO dto = new StadiumDTO();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setLocation(s.getLocation());
            dto.setNumberOfSectors(s.getSectors() != null ? s.getSectors().size() : 0);
            return dto;
        }).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public Sector addSector(SectorRequestDTO dto) {
        Stadium stadium = stadiumRepository.findById(dto.getStadiumId())
                .orElseThrow(() -> new TicketException("Stadionul nu a fost găsit!"));

        Sector sector = new Sector();
        sector.setName(dto.getName());
        sector.setStadium(stadium);
        Sector savedSector = sectorRepository.save(sector);

        // Generăm automat locurile pentru acest sector
        for (int i = 1; i <= dto.getRows(); i++) {
            for (int j = 1; j <= dto.getSeatsPerRow(); j++) {
                Seat seat = new Seat();
                seat.setRowNumber(i);
                seat.setSeatNumber(j);
                seat.setSector(savedSector);
                seatRepository.save(seat);
            }
        }

        return savedSector;
    }
}
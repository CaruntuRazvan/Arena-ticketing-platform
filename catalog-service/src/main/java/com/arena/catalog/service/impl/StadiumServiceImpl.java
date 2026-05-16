package com.arena.catalog.service.impl;


import com.arena.catalog.dto.SectorDTO;
import com.arena.catalog.dto.StadiumDTO;
import com.arena.catalog.model.Stadium;
import com.arena.catalog.repository.StadiumRepository;
import com.arena.catalog.repository.SectorRepository;
import com.arena.catalog.repository.SeatRepository;
import com.arena.catalog.service.StadiumService;
import com.arena.catalog.dto.SectorRequestDTO;
import com.arena.catalog.model.Sector;
import com.arena.catalog.model.Seat;
import org.springframework.stereotype.Service;
import com.arena.catalog.exception.CatalogException;

import java.util.ArrayList;
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
    public List<SectorDTO> getSectorsByStadiumId(Long stadiumId) {
        // Verificăm dacă stadionul există (opțional, dar recomandat)
        if (!stadiumRepository.existsById(stadiumId)) {
            throw new CatalogException("Stadionul cu ID-ul " + stadiumId + " nu a fost găsit!");
        }

        // Luăm sectoarele și le mapăm la SectorDTO
        return sectorRepository.findByStadiumId(stadiumId).stream()
                .map(sector -> {
                    SectorDTO dto = new SectorDTO();
                    dto.setId(sector.getId());
                    dto.setName(sector.getName());
                    dto.setStadiumId(stadiumId);
                    // Calculăm numărul de locuri dacă ai nevoie de el în UI
                    dto.setTotalSeats(sector.getSeats() != null ? sector.getSeats().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SectorDTO addSector(SectorRequestDTO dto) {
        Stadium stadium = stadiumRepository.findById(dto.getStadiumId())
                .orElseThrow(() -> new CatalogException("Stadionul nu a fost găsit!"));

        Sector sector = new Sector();
        sector.setName(dto.getName());
        sector.setStadium(stadium);
        // Presupunând că ai adăugat câmpul price în Sector model și DTO
        // sector.setBasePrice(dto.getBasePrice());

        Sector savedSector = sectorRepository.save(sector);

        List<Seat> seatsToSave = new ArrayList<>();

        for (int i = 1; i <= dto.getRows(); i++) {
            for (int j = 1; j <= dto.getSeatsPerRow(); j++) {
                Seat seat = new Seat();
                seat.setRowNumber(i);
                seat.setSeatNumber(j);
                seat.setSector(savedSector);
                seatsToSave.add(seat);
            }
        }

        // Salvăm toate locurile dintr-o singură mișcare (mult mai rapid!)
        seatRepository.saveAll(seatsToSave);

        // Returnăm un DTO curat
        SectorDTO responseDTO = new SectorDTO();
        responseDTO.setId(savedSector.getId());
        responseDTO.setName(savedSector.getName());
        responseDTO.setStadiumId(stadium.getId());
        responseDTO.setTotalSeats(seatsToSave.size());

        responseDTO.setRows(dto.getRows());
        responseDTO.setSeatsPerRow(dto.getSeatsPerRow());
        return responseDTO;
    }
    @Override
    @Transactional
    public void deleteSector(Long sectorId) {
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new CatalogException("Sectorul cu ID-ul " + sectorId + " nu există!"));

        // 1. Ștergem toate locurile din acest sector mai întâi
        // (Dacă nu ai configurat CascadeType.REMOVE în entitatea Sector)
        seatRepository.deleteBySectorId(sectorId);

        // 2. Ștergem sectorul
        sectorRepository.delete(sector);
    }
}
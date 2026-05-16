package com.arena.catalog.service;

import com.arena.catalog.dto.SectorDTO;
import com.arena.catalog.dto.StadiumDTO;
import com.arena.catalog.dto.SectorRequestDTO;

import java.util.List;


public interface StadiumService {
    StadiumDTO createStadium(StadiumDTO stadiumDTO);
    List<StadiumDTO> getAllStadiums();
    List<SectorDTO> getSectorsByStadiumId(Long stadiumId);
    SectorDTO addSector(SectorRequestDTO dto);
    void deleteSector(Long sectorId);
}
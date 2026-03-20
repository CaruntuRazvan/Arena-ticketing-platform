package com.arena.ticketing.service;

import com.arena.ticketing.dto.StadiumDTO;
import com.arena.ticketing.dto.SectorRequestDTO;
import com.arena.ticketing.model.Sector;
import java.util.List;


public interface StadiumService {
    StadiumDTO createStadium(StadiumDTO stadiumDTO);
    List<StadiumDTO> getAllStadiums();
    Sector addSector(SectorRequestDTO dto);
}
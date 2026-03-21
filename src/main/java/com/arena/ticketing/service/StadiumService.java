package com.arena.ticketing.service;

import com.arena.ticketing.dto.SectorDTO;
import com.arena.ticketing.dto.StadiumDTO;
import com.arena.ticketing.dto.SectorRequestDTO;

import java.util.List;


public interface StadiumService {
    StadiumDTO createStadium(StadiumDTO stadiumDTO);
    List<StadiumDTO> getAllStadiums();
    SectorDTO addSector(SectorRequestDTO dto);
}
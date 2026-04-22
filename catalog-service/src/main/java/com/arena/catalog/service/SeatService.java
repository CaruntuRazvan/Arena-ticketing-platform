package com.arena.catalog.service;


import com.arena.catalog.dto.SeatDTO;

import java.util.List;

public interface SeatService {
    List<SeatDTO> getSeatsBySector(Long sectorId);
    SeatDTO getSeatById(Long id);
}
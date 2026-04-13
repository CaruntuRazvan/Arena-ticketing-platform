package com.arena.ticketing.service;

import com.arena.ticketing.dto.SeatDTO;
import com.arena.ticketing.dto.SeatStatusDTO;
import java.util.List;

public interface SeatService {
    List<SeatDTO> getSeatsBySector(Long sectorId);
    List<SeatStatusDTO> getSeatsStatusByMatch(Long matchId, Long sectorId);
}
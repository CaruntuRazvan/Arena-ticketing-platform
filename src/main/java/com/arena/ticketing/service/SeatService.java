package com.arena.ticketing.service;

import com.arena.ticketing.model.Seat;
import com.arena.ticketing.dto.SeatStatusDTO;
import java.util.List;

public interface SeatService {
    List<Seat> getSeatsBySector(Long sectorId);
    List<SeatStatusDTO> getSeatsStatusByMatch(Long matchId, Long sectorId);
}
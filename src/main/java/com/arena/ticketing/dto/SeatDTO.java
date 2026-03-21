package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatDTO {
    private Long id;
    private int rowNumber;
    private int seatNumber;
    private Long sectorId;
}
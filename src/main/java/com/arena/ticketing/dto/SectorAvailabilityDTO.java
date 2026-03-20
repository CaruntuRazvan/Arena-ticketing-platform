package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectorAvailabilityDTO {
    private Long sectorId;
    private String sectorName;
    private long availableSeats;
    private boolean isSoldOut;
    private double price;
}
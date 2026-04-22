package com.arena.ticketing.dto.external;

import lombok.Data;

@Data
public class SeatDTO {
    private Long id;
    private Integer rowNumber;
    private Integer seatNumber;
    private Long sectorId;
}
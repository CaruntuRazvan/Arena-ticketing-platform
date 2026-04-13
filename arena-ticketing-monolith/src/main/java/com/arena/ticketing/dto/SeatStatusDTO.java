package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatStatusDTO {
    private Long seatId;
    private int rowNumber;
    private int seatNumber;
    private boolean isAvailable; // Aici e cheia!
    private double price;

}
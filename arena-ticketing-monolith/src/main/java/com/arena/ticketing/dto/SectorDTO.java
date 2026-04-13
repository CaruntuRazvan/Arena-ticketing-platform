package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectorDTO {
    private Long id;
    private String name;
    private int rows;
    private int seatsPerRow;
    private int totalSeats; // Calculat ca rows * seatsPerRow
    private Double basePrice; // Prețul per bilet în acest sector
    private Long stadiumId;
}
package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    private Long id;
    private String ticketCode;
    private String opponentName;
    private String sectorName;
    private int rowNumber;
    private int seatNumber;
    private Double finalPrice;
    private String status;
    private java.time.LocalDateTime createdAt;
}
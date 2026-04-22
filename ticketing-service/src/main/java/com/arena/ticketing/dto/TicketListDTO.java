package com.arena.ticketing.dto;

public record TicketListDTO(
        Long id,
        String ticketCode,
        String opponentName,
        String sectorName,
        int row,
        int seat,
        Double price,
        boolean used
) {}

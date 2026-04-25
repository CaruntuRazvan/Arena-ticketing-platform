package com.arena.ticketing.service;

import com.arena.ticketing.dto.*;
import java.util.List;

public interface TicketService {
    List<TicketResponseDTO> buyTickets(TicketRequestDTO request);
    List<TicketResponseDTO> confirmPayment(List<Long> ticketIds);

    // Adaugă restul dacă vrei să scapi de erorile de compilare din Impl
    List<TicketResponseDTO> getAllTickets();
    List<TicketResponseDTO> getTicketsByMatch(Long matchId);
    List<TicketListDTO> getTicketsByUserId(Long userId);
    void validateTicket(String ticketCode);
}
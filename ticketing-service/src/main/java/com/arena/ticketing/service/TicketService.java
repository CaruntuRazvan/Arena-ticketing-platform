package com.arena.ticketing.service;

import com.arena.ticketing.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {
    List<TicketResponseDTO> buyTickets(TicketRequestDTO request);
    List<TicketResponseDTO> confirmPayment(List<Long> ticketIds);
    /*
    List<TicketResponseDTO> getAllTickets();
    List<TicketResponseDTO> getTicketsByMatch(Long matchId);
    List<TicketListDTO> getTicketsByUserId(Long userId);

     */
    Page<TicketResponseDTO> getAllTickets(Pageable pageable);
    Page<TicketResponseDTO> getTicketsByMatch(Long matchId, Pageable pageable);
    Page<TicketListDTO> getTicketsByUserId(Long userId, Pageable pageable);
    void validateTicket(String ticketCode);
    List<Long> getOccupiedSeatsInList(Long matchId, List<Long> seatIds);
}
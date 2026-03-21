package com.arena.ticketing.service;

import com.arena.ticketing.dto.TicketListDTO;
import com.arena.ticketing.dto.TicketRequestDTO;
import com.arena.ticketing.dto.MatchRevenueReportDTO;
import com.arena.ticketing.dto.TicketResponseDTO;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TicketService {

    @Transactional(isolation = Isolation.SERIALIZABLE)
    List<TicketResponseDTO> buyTickets(TicketRequestDTO request);

    List<TicketResponseDTO> getAllTickets();
    List<TicketResponseDTO> getTicketsByMatch(Long matchId);
    List<TicketListDTO> getTicketsByUserId(Long userId);
    Double getTotalRevenueByMatch(Long matchId);
    TicketResponseDTO getTicketResponseById(Long id);
    void validateTicket(String ticketCode);

    MatchRevenueReportDTO getDetailedRevenueReport(Long matchId);
}
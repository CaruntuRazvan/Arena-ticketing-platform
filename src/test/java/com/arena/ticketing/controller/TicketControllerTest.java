package com.arena.ticketing.controller;

import com.arena.ticketing.dto.MatchRevenueReportDTO;
import com.arena.ticketing.dto.TicketListDTO;
import com.arena.ticketing.dto.TicketRequestDTO;
import com.arena.ticketing.model.Ticket;
import com.arena.ticketing.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/tickets/buy - Achizitie reusita")
    void shouldBuyTickets() throws Exception {
        TicketRequestDTO request = new TicketRequestDTO();
        request.setMatchId(1L);
        request.setUserId(1L);
        request.setSeatIds(List.of(10L, 11L));

        Ticket t1 = new Ticket();
        t1.setId(100L);

        when(ticketService.buyTickets(any(TicketRequestDTO.class))).thenReturn(List.of(t1));

        mockMvc.perform(post("/api/tickets/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    @DisplayName("GET /api/tickets/user/{userId} - Istoric bilete")
    void shouldReturnUserTickets() throws Exception {
        TicketListDTO dto = new TicketListDTO(1L, "UUID-CODE", "Echipa A", "Sector 1", 5, 12, 50.0, false);

        when(ticketService.getTicketsByUserId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tickets/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketCode").value("UUID-CODE"))
                .andExpect(jsonPath("$[0].opponentName").value("Echipa A"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/validate/{code} - Validare bilet")
    void shouldValidateTicket() throws Exception {
        String code = "valid-uuid";
        doNothing().when(ticketService).validateTicket(anyString());

        mockMvc.perform(patch("/api/tickets/validate/" + code))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("a fost validat")));
    }

    @Test
    @DisplayName("GET /api/tickets/revenue/match/{id} - Raport financiar")
    void shouldReturnRevenueReport() throws Exception {
        MatchRevenueReportDTO report = new MatchRevenueReportDTO(1L, "Opponent", 500.0, List.of());

        when(ticketService.getDetailedRevenueReport(1L)).thenReturn(report);

        mockMvc.perform(get("/api/tickets/revenue/match/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(500.0));
    }
}
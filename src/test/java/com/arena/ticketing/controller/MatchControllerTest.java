package com.arena.ticketing.controller;

import com.arena.ticketing.dto.MatchRequestDTO;
import com.arena.ticketing.model.Match;
import com.arena.ticketing.model.MatchStatus;
import com.arena.ticketing.service.MatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @Autowired
    private ObjectMapper objectMapper; // Pentru a transforma obiectele în JSON

    private Match match;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setId(1L);
        match.setOpponentName("CFR Cluj");
        match.setStatus(MatchStatus.SCHEDULED);
        match.setMatchDate(LocalDateTime.now().plusDays(2));
    }

    @Test
    @DisplayName("GET /api/matches - Ar trebui să returneze lista de meciuri")
    void shouldReturnAllMatches() throws Exception {
        when(matchService.getAllMatches()).thenReturn(List.of(match));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].opponentName").value("CFR Cluj"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/matches - Ar trebui să creeze un meci")
    void shouldCreateMatch() throws Exception {
        MatchRequestDTO dto = new MatchRequestDTO();
        dto.setOpponentName("CFR Cluj");
        dto.setStadiumId(1L);
        dto.setMatchDate(LocalDateTime.now().plusDays(5));

        when(matchService.createMatch(any(MatchRequestDTO.class))).thenReturn(match);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentName").value("CFR Cluj"));
    }

    @Test
    @DisplayName("PATCH /api/matches/{id}/status - Ar trebui să actualizeze statusul")
    void shouldUpdateStatus() throws Exception {
        mockMvc.perform(patch("/api/matches/1/status")
                        .param("status", "FINISHED"))
                .andExpect(status().isOk())
                .andExpect(content().string("Statusul meciului a fost actualizat în FINISHED"));
    }

    @Test
    @DisplayName("GET /api/matches/{id}/statistics - Ar trebui să returneze 200 OK")
    void shouldReturnMatchStats() throws Exception {

        mockMvc.perform(get("/api/matches/1/statistics"))
                .andExpect(status().isOk());
    }
}
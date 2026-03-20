package com.arena.ticketing.controller;

import com.arena.ticketing.dto.SeatStatusDTO;
import com.arena.ticketing.model.Seat;
import com.arena.ticketing.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
public class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @Test
    @DisplayName("GET /api/seats/sector/{id} - Ar trebui să returneze locurile din sector")
    void shouldReturnSeatsBySector() throws Exception {
        Seat seat = new Seat();
        seat.setId(50L);
        seat.setRowNumber(1);
        seat.setSeatNumber(10);

        when(seatService.getSeatsBySector(1L)).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/seats/sector/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(50))
                .andExpect(jsonPath("$[0].seatNumber").value(10));
    }

    @Test
    @DisplayName("GET /api/seats/availability - Ar trebui să returneze starea locurilor (Liber/Ocupat)")
    void shouldReturnAvailability() throws Exception {
        SeatStatusDTO status = new SeatStatusDTO(50L, 1, 10, true, 45.0);

        when(seatService.getSeatsStatusByMatch(1L, 1L)).thenReturn(List.of(status));

        mockMvc.perform(get("/api/seats/availability")
                        .param("matchId", "1")
                        .param("sectorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatId").value(50L))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].price").value(45.0));
    }
}
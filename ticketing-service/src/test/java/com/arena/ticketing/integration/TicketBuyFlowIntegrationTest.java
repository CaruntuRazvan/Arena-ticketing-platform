package com.arena.ticketing.integration;

import com.arena.ticketing.dto.TicketRequestDTO;
import com.arena.ticketing.model.Ticket;
import com.arena.ticketing.repository.TicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketBuyFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Mock simplu și curat pe repository-ul tău real
    @MockitoBean private TicketRepository ticketRepository;

    @RegisterExtension
    static WireMockExtension authServiceMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    static WireMockExtension catalogServiceMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.auth-service.url", authServiceMock::baseUrl);
        registry.add("spring.cloud.openfeign.client.config.catalog-service.url", catalogServiceMock::baseUrl);
    }

    @BeforeEach
    void setUpMocks() {
        // =========================================================================
        // CONFIGURĂM WIREMOCK STUBS (HTTP LAYER)
        // =========================================================================
        authServiceMock.stubFor(get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"username\":\"marius_popescu\",\"email\":\"marius@arena.ro\",\"loyaltyPoints\":15}")));

        catalogServiceMock.stubFor(get(urlEqualTo("/api/catalog/matches/10"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":10,\"opponentName\":\"Dinamo\",\"matchDate\":\"2026-05-20T21:00:00\",\"stadiumName\":\"Arena Nationala\"}")));

        catalogServiceMock.stubFor(get(urlEqualTo("/api/catalog/seats/100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":100,\"rowNumber\":5,\"seatNumber\":12,\"sectorId\":3,\"sectorName\":\"Tribuna 2\"}")));

        catalogServiceMock.stubFor(get(urlEqualTo("/api/catalog/matches/10/prices/3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("50.0")));

        // =========================================================================
        // CONFIGURĂM METODELE REALE DIN TICKETREPOSITORY (MOCKITO)
        // =========================================================================

        // 1. Îi spunem că locul NU este ocupat (returnează listă goală de ID-uri ocupate)
        // Folosim any() pentru parametri (matchId, seatIds, timeout) conform semnăturii din repo-ul tău
        when(ticketRepository.findOccupiedSeatIdsInList(any(), any(), any())).thenReturn(Collections.emptyList());

        // 2. În caz că serviciul tău folosește și cealaltă metodă booleană (isSeatOccupied), îi spunem direct că e false (locul e liber)
        when(ticketRepository.isSeatOccupied(any(), any(), any())).thenReturn(false);

        // 3. Simulăm salvarea biletului atașându-i un ID valid
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(12345L);
            return ticket;
        });
    }

    @Test
    @DisplayName("E2E Backend Contract Test: Cumpărare bilet cu verificare apeluri HTTP și comportament baze de date")
    void fullTicketPurchaseFlow() throws Exception {

        TicketRequestDTO requestDTO = new TicketRequestDTO(10L, List.of(100L), 1L, false);

        // ACT
        mockMvc.perform(post("/api/ticketing/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].opponentName").value("Dinamo"))
                .andExpect(jsonPath("$[0].finalPrice").value(50.0));

        // =========================================================================
        // VERIFICĂRI COMPORTAMENTALE STRICTE
        // =========================================================================

        // Verificăm apelurile HTTP folosind corect metodele din instanțele specifice de extensie
        authServiceMock.verify(getRequestedFor(urlEqualTo("/api/users/1")));
        catalogServiceMock.verify(getRequestedFor(urlEqualTo("/api/catalog/matches/10")));
        catalogServiceMock.verify(getRequestedFor(urlEqualTo("/api/catalog/seats/100")));
        catalogServiceMock.verify(getRequestedFor(urlEqualTo("/api/catalog/matches/10/prices/3")));

        // Verificăm apelul metodei de salvare din JpaRepository
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }
}
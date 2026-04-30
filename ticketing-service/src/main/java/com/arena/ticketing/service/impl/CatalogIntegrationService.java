package com.arena.ticketing.service.impl;

import com.arena.ticketing.client.CatalogClient;
import com.arena.ticketing.dto.external.MatchDTO;
import com.arena.ticketing.dto.external.SeatDTO;
import com.arena.ticketing.exception.TicketException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogIntegrationService {

    private final CatalogClient catalogClient;

    @Cacheable(value = "matchCache", key = "#matchId", unless = "#result == null")
    @CircuitBreaker(name = "catalogCB", fallbackMethod = "fallbackMatch")
    public MatchDTO getMatchSecurely(Long matchId) {
        log.info(">>> Cache Miss pentru Meciul: {}. Apelez Catalog-Service prin Feign...", matchId);
        return catalogClient.getMatchById(matchId);
    }

    @Cacheable(value = "seatCache", key = "#seatId", unless = "#result == null")
    @CircuitBreaker(name = "catalogCB", fallbackMethod = "fallbackSeat")
    public SeatDTO getSeatSecurely(Long seatId) {
        log.info(">>> Cache Miss pentru Locul: {}. Apelez Catalog-Service prin Feign...", seatId);
        return catalogClient.getSeatById(seatId);
    }

    @Cacheable(value = "priceCache", key = "{#matchId, #sectorId}")
    @CircuitBreaker(name = "catalogCB", fallbackMethod = "fallbackPrice")
    public Double getPriceSecurely(Long matchId, Long sectorId) {
        return catalogClient.getPrice(matchId, sectorId);
    }

    // --- FALLBACKS ---

    public MatchDTO fallbackMatch(Long matchId, Throwable t) {
        log.error(">>> FALLBACK MATCH pentru ID {}: {}", matchId, t.getMessage());
        MatchDTO dto = new MatchDTO();
        dto.setId(matchId);
        dto.setOpponentName("Meci Indisponibil (Offline)");
        dto.setMatchDate(LocalDateTime.now().plusDays(1));
        return dto;
    }

    public SeatDTO fallbackSeat(Long seatId, Throwable t) {
        log.error(">>> FALLBACK SEAT pentru ID {}: {}", seatId, t.getMessage());
        SeatDTO dto = new SeatDTO();
        dto.setId(seatId);
        dto.setSectorId(0L);
        return dto;
    }

    public Double fallbackPrice(Long matchId, Long sectorId, Throwable t) {
        log.error(">>> CRITICAL: Catalog offline și preț lipsă în Cache pentru Match {} Sector {}.", matchId, sectorId);
        // În loc de 50.0, aruncăm o excepție de business
        throw new TicketException("Serviciul de prețuri este momentan indisponibil. Vă rugăm reîncercați!");
    }
}
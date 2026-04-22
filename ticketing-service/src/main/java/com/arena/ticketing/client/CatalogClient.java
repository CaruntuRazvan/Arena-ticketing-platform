package com.arena.ticketing.client;

import com.arena.ticketing.dto.external.MatchDTO;
import com.arena.ticketing.dto.external.SeatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service") // Numele serviciului din Eureka
public interface CatalogClient {

    @GetMapping("/api/catalog/matches/{id}")
    MatchDTO getMatchById(@PathVariable("id") Long id);

    @GetMapping("/api/catalog/seats/{id}")
    SeatDTO getSeatById(@PathVariable("id") Long id);

    @GetMapping("/api/catalog/matches/{matchId}/prices/{sectorId}")
    Double getPrice(@PathVariable("matchId") Long matchId, @PathVariable("sectorId") Long sectorId);
}
package com.arena.catalog.controller;


import com.arena.catalog.dto.*;
import com.arena.catalog.model.Match;
import com.arena.catalog.model.MatchStatus;
import com.arena.catalog.service.MatchService;
import com.arena.catalog.model.MatchSectorPrice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/catalog/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatchesDTO());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MatchDTO>> getUpcoming() {
        // folosind un mapper sau manual cu .stream().map(...)
        return ResponseEntity.ok(matchService.getUpcomingMatchesDTO());
    }

    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@Valid @RequestBody MatchRequestDTO dto) {
        return ResponseEntity.ok(matchService.createMatch(dto));
    }

    @PostMapping("/prices")
    public ResponseEntity<String> setPrices(@Valid @RequestBody List<PriceRequestDTO> prices) {
        matchService.setMatchPrices(prices);
        return ResponseEntity.ok("Prețurile au fost setate cu succes!");
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam MatchStatus status) {

        matchService.updateMatchStatus(id, status);
        return ResponseEntity.ok("Statusul meciului a fost actualizat în " + status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build(); // Trimite 204 No Content (succes)
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @GetMapping("/{matchId}/prices/{sectorId}")
    public ResponseEntity<Double> getPrice(@PathVariable Long matchId, @PathVariable Long sectorId) {
        return ResponseEntity.ok(matchService.getSectorPrice(matchId, sectorId));
    }
}
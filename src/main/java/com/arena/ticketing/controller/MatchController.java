package com.arena.ticketing.controller;

import com.arena.ticketing.model.Match;
import com.arena.ticketing.model.MatchStatus;
import com.arena.ticketing.service.MatchService;
import com.arena.ticketing.model.MatchSectorPrice;
import com.arena.ticketing.dto.MatchRequestDTO;
import com.arena.ticketing.dto.PriceRequestDTO;
import com.arena.ticketing.dto.MatchStatsDTO;
import com.arena.ticketing.dto.SectorAvailabilityDTO;

import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(summary = "Listare toate meciurile",
            description = "Returnează lista completă a meciurilor programate în sistem.")
    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @Operation(summary = "Listare meciuri viitoare",
            description = "Returnează lista meciurilor care urmează să aibă loc în viitor si nu au fost anulate.")
    @GetMapping("/upcoming")
    public ResponseEntity<List<Match>> getUpcomingMatches() {
        return ResponseEntity.ok(matchService.getUpcomingMatches());
    }

    @Operation(summary = "Creare meci",
            description = "Permite crearea unui nou meci în sistem.")
    @PostMapping
    public ResponseEntity<Match> createMatch(@Valid @RequestBody MatchRequestDTO dto) {
        return ResponseEntity.ok(matchService.createMatch(dto));
    }

    @Operation(summary = "Setare prețuri meci",
            description = "Permite setarea prețurilor pentru fiecare sector al unui meci.")
    @PostMapping("/prices")
    public ResponseEntity<String> setPrices(@Valid @RequestBody List<PriceRequestDTO> prices) {
        matchService.setMatchPrices(prices);
        return ResponseEntity.ok("Prețurile au fost setate cu succes!");
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Statistici ocupare meci", description = "Returnează procentul de ocupare total și pe fiecare sector în parte.")
    public ResponseEntity<MatchStatsDTO> getMatchStats(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchStatistics(id));
    }

    @Operation(summary = "Verifică disponibilitatea sectoarelor pentru un meci",
            description = "Returnează starea de disponibilitate a fiecărui sector pentru un anumit meci.")
    @GetMapping("/{matchId}/sectors-availability")
    public ResponseEntity<List<SectorAvailabilityDTO>> getSectorsAvailability(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getSectorsAvailabilityForMatch(matchId));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Schimbă statusul unui meci", description = "Admin-ul poate anula sau finaliza un meci.")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam MatchStatus status) {

        matchService.updateMatchStatus(id, status);
        return ResponseEntity.ok("Statusul meciului a fost actualizat în " + status);
    }
}
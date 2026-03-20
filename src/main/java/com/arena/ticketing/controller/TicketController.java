package com.arena.ticketing.controller;

import com.arena.ticketing.dto.MatchRevenueReportDTO;
import com.arena.ticketing.dto.TicketListDTO;
import com.arena.ticketing.dto.TicketRequestDTO;
import com.arena.ticketing.model.Ticket;
import com.arena.ticketing.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    @Operation(summary = "Achizitie bilete multiple", description = "Permite achizitia a maxim 5 bilete intr-o singura tranzactie.")
    public ResponseEntity<List<Ticket>> buyTicket(@Valid @RequestBody TicketRequestDTO request) {

        List<Ticket> bilete = ticketService.buyTickets(request);
        return ResponseEntity.ok(bilete);
    }

    @Operation(summary = "Listare toate biletele",
            description = "Returnează lista completă a biletelor din sistem. Utilizat în scopuri administrative.")
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @Operation(summary = "Istoric bilete utilizator",
            description = "Returnează toate biletele achiziționate de un utilizator specific, incluzând detaliile despre sector, rând și loc.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketListDTO>> getMyTickets(@PathVariable Long userId) {
        // Corect: Controller -> Service -> Repository
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @Operation(summary = "Bilete pentru un meci specific",
            description = "Returnează toate biletele asociate unui meci dat, incluzând detaliile despre utilizator și locurile rezervate.")
    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<Ticket>> getTicketsByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(ticketService.getTicketsByMatch(matchId));
    }

    @Operation(summary = "Raport detaliat venituri meci",
            description = "Returnează un raport detaliat al veniturilor generate de vânzarea biletelor pentru un meci specific.")
    @GetMapping("/revenue/match/{matchId}")
    public ResponseEntity<MatchRevenueReportDTO> getMatchRevenue(@PathVariable Long matchId) {
        return ResponseEntity.ok(ticketService.getDetailedRevenueReport(matchId));
    }

    @PatchMapping("/validate/{ticketCode}")
    @Operation(summary = "Scanare bilet la intrare", description = "Validează biletul folosind codul unic UUID.")
    public ResponseEntity<String> validateTicket(@PathVariable String ticketCode) {
        ticketService.validateTicket(ticketCode);
        return ResponseEntity.ok("Acces permis! Biletul " + ticketCode + " a fost validat.");
    }

}
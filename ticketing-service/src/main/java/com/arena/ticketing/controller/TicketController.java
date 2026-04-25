package com.arena.ticketing.controller;

import com.arena.ticketing.dto.*;
import com.arena.ticketing.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticketing") // Schimbăm prefixul ca să fie clar în Gateway
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    public ResponseEntity<List<TicketResponseDTO>> buyTicket(@Valid @RequestBody TicketRequestDTO request) {
        return ResponseEntity.ok(ticketService.buyTickets(request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<List<TicketResponseDTO>> confirmTickets(@RequestBody List<Long> ticketIds) {
        return ResponseEntity.ok(ticketService.confirmPayment(ticketIds));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketListDTO>> getMyTickets(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<TicketResponseDTO>> getTicketsByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(ticketService.getTicketsByMatch(matchId));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @PatchMapping("/validate/{ticketCode}")
    public ResponseEntity<String> validateTicket(@PathVariable String ticketCode) {
        ticketService.validateTicket(ticketCode);
        return ResponseEntity.ok("Acces permis! Biletul " + ticketCode + " a fost validat.");
    }

    /* NOTĂ: Metoda de download PDF și Revenue Report sunt momentan comentate
       până când implementăm PDF Service și logica de agregare cross-service.
    */
}
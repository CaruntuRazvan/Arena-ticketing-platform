package com.arena.ticketing.controller;

import com.arena.ticketing.dto.SeatDTO;
import com.arena.ticketing.model.Seat;
import com.arena.ticketing.service.SeatService;
import com.arena.ticketing.dto.SeatStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService; // Injectam Service-ul

    @Operation(summary = "Returnează toate locurile dintr-un sector",
            description = "Permite obținerea listei complete a locurilor sub formă de DTO pentru a evita erorile de recursivitate.")
    @GetMapping("/sector/{sectorId}")
    public ResponseEntity<List<SeatDTO>> getSeatsBySector(@PathVariable Long sectorId) {
        // Schimbăm tipul returnat în List<SeatDTO>
        return ResponseEntity.ok(seatService.getSeatsBySector(sectorId));
    }

    @Operation(summary = "Verifică disponibilitatea locurilor pentru un meci și sector specific",
            description = "Returnează starea de disponibilitate a locurilor pentru un anumit meci și sector.")
    @GetMapping("/availability")
    public ResponseEntity<List<SeatStatusDTO>> getAvailability(
            @RequestParam Long matchId,
            @RequestParam Long sectorId) {
        return ResponseEntity.ok(seatService.getSeatsStatusByMatch(matchId, sectorId));
    }
}
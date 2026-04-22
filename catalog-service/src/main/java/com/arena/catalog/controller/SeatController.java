package com.arena.catalog.controller;


import com.arena.catalog.dto.SeatDTO;
import com.arena.catalog.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalog/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService; // Injectam Service-ul

    @GetMapping("/sector/{sectorId}")
    public ResponseEntity<List<SeatDTO>> getSeatsBySector(@PathVariable Long sectorId) {

        return ResponseEntity.ok(seatService.getSeatsBySector(sectorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatDTO> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }
}
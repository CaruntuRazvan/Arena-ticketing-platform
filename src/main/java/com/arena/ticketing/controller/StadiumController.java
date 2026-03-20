package com.arena.ticketing.controller;

import com.arena.ticketing.dto.StadiumDTO;
import com.arena.ticketing.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.arena.ticketing.dto.SectorRequestDTO;
import com.arena.ticketing.model.Sector;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/stadiums")
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumService stadiumService;

    // Endpoint pentru a vedea toate stadioanele
    @Operation(summary = "Listare toate stadioanele",
            description = "Returnează lista completă a stadioanelor din sistem.")
    @GetMapping
    public ResponseEntity<List<StadiumDTO>> getAllStadiums() {
        return ResponseEntity.ok(stadiumService.getAllStadiums());
    }

    @Operation(summary = "Creare stadion",
            description = "Permite crearea unui nou stadion în sistem.")
    @PostMapping
    public ResponseEntity<StadiumDTO> createStadium(@Valid @RequestBody StadiumDTO stadiumDTO) {
        return ResponseEntity.ok(stadiumService.createStadium(stadiumDTO));
    }


    @Operation(summary = "Adăugare sector la stadion",
            description = "Permite adăugarea unui nou sector la un stadion existent.")
    @PostMapping("/sectors")
    public ResponseEntity<Sector> addSector(@Valid @RequestBody SectorRequestDTO dto) {
        return ResponseEntity.ok(stadiumService.addSector(dto));
    }
}
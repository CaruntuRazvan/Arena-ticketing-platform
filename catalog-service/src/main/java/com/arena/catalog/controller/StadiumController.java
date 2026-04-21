package com.arena.catalog.controller;

import com.arena.catalog.dto.SectorDTO;
import com.arena.catalog.dto.StadiumDTO;
import com.arena.catalog.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.arena.catalog.dto.SectorRequestDTO;
import com.arena.catalog.model.Sector;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


import java.util.List;

@RestController
@RequestMapping("/api/catalog/stadiums")
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumService stadiumService;

    // Endpoint pentru a vedea toate stadioanele
    @GetMapping
    public ResponseEntity<List<StadiumDTO>> getAllStadiums() {
        return ResponseEntity.ok(stadiumService.getAllStadiums());
    }

    @PostMapping
    public ResponseEntity<StadiumDTO> createStadium(@Valid @RequestBody StadiumDTO stadiumDTO) {
        return ResponseEntity.ok(stadiumService.createStadium(stadiumDTO));
    }

    @PostMapping("/sectors")
    public ResponseEntity<SectorDTO> addSector(@Valid @RequestBody SectorRequestDTO dto) {
        SectorDTO createdSector = stadiumService.addSector(dto);
        return new ResponseEntity<>(createdSector, HttpStatus.CREATED);
    }

    @DeleteMapping("/sectors/{id}")
    public ResponseEntity<Void> deleteSector(@PathVariable Long id) {
        stadiumService.deleteSector(id);
        return ResponseEntity.noContent().build();
    }
}
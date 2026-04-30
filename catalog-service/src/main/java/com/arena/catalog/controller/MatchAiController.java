package com.arena.catalog.controller;

import com.arena.catalog.dto.MatchDTO;
import com.arena.catalog.service.MatchAiService;
import com.arena.catalog.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/matches")
@RequiredArgsConstructor
public class MatchAiController {

    private final MatchAiService matchAiService;
    private final MatchService matchService;

    @GetMapping("/{id}/ai-trivia")
    public ResponseEntity<String> getMatchTrivia(@PathVariable Long id) {
        // Obținem meciul folosind serviciul tău existent
        // Presupunem că matchService.getMatchById(id) returnează un MatchDTO
        MatchDTO match = matchService.getMatchById(id);

        // Trimitem doar opponentName către serviciul de AI
        String trivia = matchAiService.getFunFacts(match.getOpponentName());

        return ResponseEntity.ok(trivia);
    }
}
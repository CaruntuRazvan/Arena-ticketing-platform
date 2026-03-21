package com.arena.ticketing.dto;

import com.arena.ticketing.model.MatchStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchDTO {
    private Long id;
    private String opponentName;
    private LocalDateTime matchDate;
    private String stadiumName;
    private MatchStatus status;
}
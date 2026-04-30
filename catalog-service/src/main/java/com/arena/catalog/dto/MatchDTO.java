package com.arena.catalog.dto;


import com.arena.catalog.model.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private String opponentName;
    private LocalDateTime matchDate;
    private String stadiumName;
    private MatchStatus status;
}
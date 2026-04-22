package com.arena.ticketing.dto.external;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MatchDTO {
    private Long id;
    private String opponentName;
    private LocalDateTime matchDate;
    private MatchStatus status;
}
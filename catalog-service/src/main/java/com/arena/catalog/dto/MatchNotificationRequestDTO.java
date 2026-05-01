package com.arena.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchNotificationRequestDTO {
    private String opponentName;
    private String matchDate;
    private String stadiumName;
    private String matchUrl; // Link catre pagina meciului
}

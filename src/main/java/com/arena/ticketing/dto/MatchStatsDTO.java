package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class MatchStatsDTO {
    private Long matchId;
    private String opponentName;
    private long totalCapacity;
    private long ticketsSold;
    private double overallOccupancyPercentage;
    private Map<String, Double> occupancyPerSector; // Nume Sector -> Procent
}
package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MatchRevenueReportDTO {
    private Long matchId;
    private String opponentName;
    private Double totalRevenue;
    private List<SectorRevenueDTO> sectorRevenues;

    @Data
    @AllArgsConstructor
    public static class SectorRevenueDTO {
        private String sectorName;
        private Double revenue;
        private Long ticketsSold;
    }
}
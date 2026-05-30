package com.arena.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRevenueReportDTO {
    private Long matchId;
    private String opponentName;
    private Double totalRevenue;
    private Long totalTicketsSold;
    private List<SectorRevenueDTO> sectorsAnalytics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorRevenueDTO {
        private Long sectorId;
        private Long ticketsSold;
        private Double revenue;
    }
}
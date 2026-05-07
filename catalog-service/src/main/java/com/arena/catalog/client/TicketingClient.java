package com.arena.catalog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "ticketing-service")
public interface TicketingClient {

    @PostMapping("/api/ticketing/occupied-seats")
    List<Long> getOccupiedSeats(
            @RequestParam("matchId") Long matchId,
            @RequestBody List<Long> seatIds
    );
}
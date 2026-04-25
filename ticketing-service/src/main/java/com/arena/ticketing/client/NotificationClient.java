package com.arena.ticketing.client;

import com.arena.ticketing.dto.TicketResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/ticket")
    void sendTicketNotification(@RequestBody TicketResponseDTO ticket, @RequestParam("email") String email);
}
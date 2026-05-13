package com.arena.ticketing.client;

import com.arena.ticketing.dto.TicketResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/ticket")
    void sendTicketNotification(@RequestBody List<TicketResponseDTO> dtos, @RequestParam("email") String email);
}
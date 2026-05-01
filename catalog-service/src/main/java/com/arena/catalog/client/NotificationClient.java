package com.arena.catalog.client;
import com.arena.catalog.dto.MatchNotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "notification-service")
public interface NotificationClient {
    // Endpoint nou pentru notificări de meciuri
    @PostMapping("/api/notifications/broadcast-match")
    void broadcastMatch(@RequestBody MatchNotificationRequestDTO request);
}
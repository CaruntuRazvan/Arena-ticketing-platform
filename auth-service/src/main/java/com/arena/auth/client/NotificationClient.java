package com.arena.auth.client;

import com.arena.auth.dto.NotificationRequestDTO; // Va trebui să creezi acest DTO și în Auth
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/api/notifications/send-email")
    void sendEmail(@RequestBody NotificationRequestDTO request);
}
package com.arena.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/users/emails") // Endpoint-ul pe care l-am creat în Auth Service
    List<String> getAllUserEmails();
}

package com.arena.ticketing.client;

import com.arena.ticketing.dto.external.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @PutMapping("/api/users/{id}/loyalty-points")
    void updatePoints(@PathVariable("id") Long id, @RequestParam("points") int points);
}
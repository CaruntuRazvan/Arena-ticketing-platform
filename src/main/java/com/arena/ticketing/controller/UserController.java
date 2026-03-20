package com.arena.ticketing.controller;

import com.arena.ticketing.dto.RegisterRequestDTO;
import com.arena.ticketing.model.User;
import com.arena.ticketing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // Toate rutele de aici vor începe cu /api/users
@RequiredArgsConstructor // Lombok generează constructorul pentru UserService automat
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        User createdUser = userService.registerUser(registerDTO);
        // Returnăm user-ul creat și statusul 201 Created
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
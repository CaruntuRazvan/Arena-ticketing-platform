package com.arena.auth.controller;

import com.arena.auth.dto.LoginRequestDTO;
import com.arena.auth.dto.LoginResponseDTO;
import com.arena.auth.dto.RegisterRequestDTO;
import com.arena.auth.dto.UserResponseDTO;
import com.arena.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        UserResponseDTO createdUser = userService.registerUser(registerDTO);
        // Returnăm user-ul creat și statusul 201 Created
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String email, @RequestParam String code) {
        userService.verifyAccount(email, code);
        return ResponseEntity.ok("Cont activat cu succes! Acum te poti loga.");
    }

    @PostMapping("/resend-code")
    public ResponseEntity<String> resendCode(@RequestParam String email) {
        userService.resendVerificationCode(email);
        return ResponseEntity.ok("Un cod nou a fost trimis pe adresa de email.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/loyalty-points")
    public ResponseEntity<Void> updatePoints(@PathVariable Long id, @RequestParam int points) {
        userService.updateLoyaltyPoints(id, points);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return ResponseEntity.ok("Logout reușit! Token-ul a fost invalidat.");
    }
}

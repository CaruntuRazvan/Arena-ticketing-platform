package com.arena.auth.controller;

import com.arena.auth.dto.LoginRequestDTO;
import com.arena.auth.dto.LoginResponseDTO;
import com.arena.auth.dto.RegisterRequestDTO;
import com.arena.auth.dto.UserResponseDTO;
import com.arena.auth.dto.TokenRefreshRequestDTO;
import com.arena.auth.service.UserService;
import com.arena.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.arena.auth.config.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
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
    /*
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/emails")
    public ResponseEntity<List<String>> getAllUserEmails() {
        List<String> emails = userService.getAllEmails();
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);

        // 2. Folosim utilitarul tău pentru a lua username-ul (subject-ul)
        String username = jwtUtils.getClaimsFromToken(token).getSubject();

        System.out.println("Extras din token: " + username);
        return ResponseEntity.ok(userService.getMyProfile(username));
    }

    @PutMapping("/{id}/loyalty-points")
    public ResponseEntity<Void> updatePoints(@PathVariable Long id, @RequestParam int points) {
        userService.updateLoyaltyPoints(id, points);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Utilizatorul cu ID-ul " + id + " și toate profilurile sau token-urile asociate au fost șterse cu succes.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        return ResponseEntity.ok("Logout reușit! Token-ul a fost invalidat.");
    }

    @PostMapping("/refresh-token")
    public LoginResponseDTO refreshToken(@RequestBody TokenRefreshRequestDTO request) {
        return userService.refreshToken(request);
    }
}

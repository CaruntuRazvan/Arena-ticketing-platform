package com.arena.auth.dto;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String username,
        String role
) {}
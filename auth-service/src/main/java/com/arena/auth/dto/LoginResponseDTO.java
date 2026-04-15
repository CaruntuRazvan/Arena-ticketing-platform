package com.arena.auth.dto;

public record LoginResponseDTO(
        String token,
        String username,
        String role
) {}
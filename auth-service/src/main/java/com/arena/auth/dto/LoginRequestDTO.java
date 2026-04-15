package com.arena.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Username-ul este obligatoriu")
    String username;
    @NotBlank(message = "Parola este obligatorie")
    String password;
}

package com.arena.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor // Generează constructorul cu toate argumentele (cel de care ai nevoie în test)
@NoArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "Username-ul este obligatoriu")
    String username;
    @NotBlank(message = "Parola este obligatorie")
    String password;
    boolean rememberMe;
}

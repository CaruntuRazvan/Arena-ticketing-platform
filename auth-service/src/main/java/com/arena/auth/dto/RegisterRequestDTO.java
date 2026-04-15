package com.arena.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Username-ul este obligatoriu")
    private String username;
    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Formatul email-ului este invalid")
    private String email;
    @NotBlank(message = "Parola este obligatorie")
    private String password;
    @NotBlank(message = "Prenumele este obligatoriu")
    private String firstName;
    @NotBlank(message = "Numele este obligatoriu")
    private String lastName;

    private String phoneNumber;
}

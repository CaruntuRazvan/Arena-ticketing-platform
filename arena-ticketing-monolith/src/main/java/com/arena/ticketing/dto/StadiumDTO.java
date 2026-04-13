package com.arena.ticketing.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter @Setter
public class StadiumDTO {

    private Long id;

    @NotBlank(message = "Numele stadionului este obligatoriu")
    @Size(min = 3, message = "Numele stadionului trebuie să aibă minim 3 caractere")
    private String name;

    @NotBlank(message = "Locația stadionului este obligatorie")
    private String location;

    private int numberOfSectors; // Informatie calculata, nu exista in tabela Stadium
}
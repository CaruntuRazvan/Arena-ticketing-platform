package com.arena.ticketing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SectorRequestDTO {

    @NotBlank(message = "Numele sectorului este obligatoriu (ex: Peluza Nord, Tribuna 1)")
    @Size(min = 2, max = 30, message = "Numele sectorului trebuie să aibă între 2 și 30 de caractere")
    private String name;

    @Min(value = 1, message = "Sectorul trebuie să aibă cel puțin un rând")
    @Max(value = 100, message = "Un sector nu poate avea mai mult de 100 de rânduri")
    private int rows;

    @Min(value = 1, message = "Fiecare rând trebuie să aibă cel puțin un scaun")
    @Max(value = 200, message = "Un rând nu poate avea mai mult de 200 de scaune")
    private int seatsPerRow;

    @NotNull(message = "ID-ul stadionului este obligatoriu pentru a crea un sector")
    private Long stadiumId;
}
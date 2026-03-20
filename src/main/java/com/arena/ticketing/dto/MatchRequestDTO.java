package com.arena.ticketing.dto;

import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

@Data
public class MatchRequestDTO {
    @NotBlank(message = "Numele adversarului este obligatoriu")
    private String opponentName;

    @NotNull(message = "Data meciului trebuie specificată")
    @Future(message = "Nu poți organiza un meci în trecut")
    private LocalDateTime matchDate;

    @NotNull(message = "ID-ul stadionului este obligatoriu")
    private Long stadiumId;
}
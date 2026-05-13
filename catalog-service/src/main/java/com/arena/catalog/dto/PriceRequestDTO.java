package com.arena.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRequestDTO {

    @NotNull(message = "ID-ul meciului este obligatoriu")
    private Long matchId;

    @NotNull(message = "ID-ul meciului este obligatoriu")
    private Long sectorId;

    @NotNull(message = "Pretul trebuie specificat")
    @Positive(message = "Pretul trebuie sa fie o valoare strict pozitiva")
    private Double price;
}

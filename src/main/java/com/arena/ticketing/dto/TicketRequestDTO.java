package com.arena.ticketing.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequestDTO {

    @NotNull(message = "ID-ul meciului este obligatoriu pentru achiziție")
    private Long matchId;

    @NotEmpty(message = "Lista de ID-uri ale locurilor nu poate fi goală")
    private List<Long> seatIds;

    @NotNull(message = "ID-ul utilizatorului este obligatoriu pentru achiziție")
    private Long userId;
}
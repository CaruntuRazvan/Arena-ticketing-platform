package com.arena.catalog.service;

import com.arena.catalog.client.TicketingClient;
import com.arena.catalog.dto.SeatDTO;
import com.arena.catalog.exception.CatalogException;
import com.arena.catalog.model.Seat;
import com.arena.catalog.model.Sector;
import com.arena.catalog.repository.SeatRepository;
import com.arena.catalog.service.impl.SeatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock private SeatRepository seatRepository;
    @Mock private TicketingClient ticketingClient;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Seat testSeat;
    private Sector testSector;

    @BeforeEach
    void setUp() {
        testSector = new Sector();
        testSector.setId(10L);

        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setRowNumber(1);
        testSeat.setSeatNumber(5);
        testSeat.setSector(testSector);
    }

    @Test
    @DisplayName("Get Seat By ID - Succes")
    void getSeatById_Success() {
        when(seatRepository.findById(1L)).thenReturn(Optional.of(testSeat));

        SeatDTO result = seatService.getSeatById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRowNumber()).isEqualTo(1);
        assertThat(result.getSectorId()).isEqualTo(10L);
        verify(seatRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Seat By ID - Aruncă excepție când nu există")
    void getSeatById_NotFound_ThrowsException() {
        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.getSeatById(99L))
                .isInstanceOf(CatalogException.class)
                .hasMessageContaining("Locul nu a fost găsit");
    }

    @Test
    @DisplayName("Get Seats By Sector (Match Context) - Verifică marcarea locurilor ocupate")
    void getSeatsBySector_WithMatch_ChecksOccupiedStatus() {
        Long matchId = 100L;
        Long sectorId = 10L;

        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setRowNumber(1);
        seat2.setSeatNumber(6);
        seat2.setSector(testSector);

        List<Seat> allSeats = List.of(testSeat, seat2);
        List<Long> allSeatIds = List.of(1L, 2L);
        List<Long> occupiedIds = List.of(1L); // Doar primul loc e ocupat

        when(seatRepository.findBySectorId(sectorId)).thenReturn(allSeats);
        when(ticketingClient.getOccupiedSeats(matchId, allSeatIds)).thenReturn(occupiedIds);

        List<SeatDTO> result = seatService.getSeatsBySector(matchId, sectorId);

        assertThat(result).hasSize(2);

        // Locul 1 ar trebui să fie ocupat (true)
        SeatDTO dto1 = result.stream().filter(s -> s.getId().equals(1L)).findFirst().get();
        assertThat(dto1.isOccupied()).isTrue();

        // Locul 2 ar trebui să fie liber (false)
        SeatDTO dto2 = result.stream().filter(s -> s.getId().equals(2L)).findFirst().get();
        assertThat(dto2.isOccupied()).isFalse();

        verify(ticketingClient).getOccupiedSeats(matchId, allSeatIds);
    }

    @Test
    @DisplayName("Get Seats By Sector (Simplu) - Succes")
    void getSeatsBySector_Simple_Success() {
        when(seatRepository.findBySectorId(10L)).thenReturn(List.of(testSeat));

        List<SeatDTO> result = seatService.getSeatsBySector(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSectorId()).isEqualTo(10L);
        verify(seatRepository).findBySectorId(10L);
    }
}
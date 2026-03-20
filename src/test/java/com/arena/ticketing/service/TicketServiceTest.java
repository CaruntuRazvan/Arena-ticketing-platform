package com.arena.ticketing.service;

import com.arena.ticketing.dto.TicketRequestDTO;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.*;
import com.arena.ticketing.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private UserRepository userRepository;
    @Mock private MatchSectorPriceRepository priceRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Match match;
    private User user;

    @BeforeEach
    void setUp() {
        match = new Match();
        match.setId(1L);
        match.setMatchDate(LocalDateTime.now().plusDays(1));
        match.setStatus(MatchStatus.SCHEDULED);

        user = new User();
        user.setId(1L);
    }

    @Test
    @DisplayName("Ar trebui sa arunce eroare dacă se cumpara mai mult de 5 bilete")
    void shouldThrowExceptionWhenMoreThanMaxPerTransaction() {
        TicketRequestDTO request = new TicketRequestDTO();
        request.setMatchId(1L);
        request.setUserId(1L);
        request.setSeatIds(List.of(1L, 2L, 3L, 4L, 5L, 6L)); // 6 locuri

        TicketException exception = assertThrows(TicketException.class, () -> {
            ticketService.buyTickets(request);
        });

        assertEquals("Puteti cumpara maxim 5 bilete per tranzactie!", exception.getMessage());
    }

    @Test
    @DisplayName("Ar trebui sa cumpere bilet cu succes")
    void shouldBuyTicketSuccessfully() {
        // Arrange
        TicketRequestDTO request = new TicketRequestDTO();
        request.setMatchId(1L);
        request.setUserId(1L);
        request.setSeatIds(List.of(10L));

        Sector sector = new Sector();
        sector.setId(1L);
        sector.setName("Tribuna 1");

        Seat seat = new Seat();
        seat.setId(10L);
        seat.setSector(sector);

        MatchSectorPrice price = new MatchSectorPrice();
        price.setPrice(100.0);

        when(ticketRepository.countByMatchIdAndUserId(1L, 1L)).thenReturn(0L);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.existsByMatchIdAndSeatId(1L, 10L)).thenReturn(false);
        when(seatRepository.findById(10L)).thenReturn(Optional.of(seat));
        when(priceRepository.findByMatchIdAndSectorId(1L, 1L)).thenReturn(Optional.of(price));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        List<Ticket> result = ticketService.buyTickets(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).getFinalPrice());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Ar trebui sa valideze biletul și sa-l marcheze ca folosit")
    void shouldValidateTicketSuccessfully() {
        Ticket ticket = new Ticket();
        ticket.setTicketCode("UUID-123");
        ticket.setUsed(false);
        ticket.setMatch(match);

        when(ticketRepository.findByTicketCode("UUID-123")).thenReturn(Optional.of(ticket));

        ticketService.validateTicket("UUID-123");

        assertTrue(ticket.isUsed());
        verify(ticketRepository).save(ticket);
    }

    @Test
    @DisplayName("Ar trebui sa esueze validarea daca biletul e deja folosit")
    void shouldFailValidationIfAlreadyUsed() {
        Ticket ticket = new Ticket();
        ticket.setTicketCode("UUID-123");
        ticket.setUsed(true);

        when(ticketRepository.findByTicketCode("UUID-123")).thenReturn(Optional.of(ticket));

        assertThrows(TicketException.class, () -> ticketService.validateTicket("UUID-123"));
    }
}
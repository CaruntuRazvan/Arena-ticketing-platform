package com.arena.ticketing.service;

import com.arena.ticketing.client.AuthClient;
import com.arena.ticketing.client.NotificationClient;
import com.arena.ticketing.dto.*;
import com.arena.ticketing.dto.external.*;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.TicketRepository;
import com.arena.ticketing.service.impl.CatalogIntegrationService;
import com.arena.ticketing.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private AuthClient authClient;
    @Mock private NotificationClient notificationClient;
    @Mock private CatalogIntegrationService catalogService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private UserDTO testUser;
    private MatchDTO testMatch;
    private SeatDTO testSeat;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setId(1L);
        testUser.setLoyaltyPoints(15);
        testUser.setEmail("user@arena.ro");

        testMatch = new MatchDTO();
        testMatch.setId(10L);
        testMatch.setOpponentName("FCSB");
        testMatch.setMatchDate(LocalDateTime.now().plusDays(2));
        testMatch.setStatus(MatchStatus.SCHEDULED);

        testSeat = new SeatDTO();
        testSeat.setId(100L);
        testSeat.setSectorId(5L);
        testSeat.setRowNumber(3);
        testSeat.setSeatNumber(12);

        testTicket = new Ticket();
        testTicket.setId(1000L);
        testTicket.setTicketCode("TK-123456");
        testTicket.setMatchId(10L);
        testTicket.setSeatId(100L);
        testTicket.setUserId(1L);
        testTicket.setFinalPrice(50.0);
        testTicket.setStatus(TicketStatus.PENDING);
        testTicket.setCreatedAt(LocalDateTime.now());
        testTicket.setUsed(false);
    }

    @Test
    @DisplayName("Buy Tickets - Succes cu aplicare discount de loialitate")
    void buyTickets_Success_WithDiscount() {
        // Arrange
        TicketRequestDTO request = new TicketRequestDTO(10L, List.of(100L), 1L, true);

        when(authClient.getUserById(1L)).thenReturn(testUser);
        when(catalogService.getMatchSecurely(10L)).thenReturn(testMatch);
        when(ticketRepository.isSeatOccupied(eq(10L), eq(100L), any(LocalDateTime.class))).thenReturn(false);
        when(catalogService.getSeatSecurely(100L)).thenReturn(testSeat);
        when(catalogService.getPriceSecurely(10L, 5L)).thenReturn(100.0);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // Act
        List<TicketResponseDTO> responses = ticketService.buyTickets(request);

        // Assert
        assertThat(responses).hasSize(1);
        verify(authClient).updatePoints(1L, -10);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Buy Tickets - Aruncă excepție dacă locul este deja ocupat")
    void buyTickets_SeatOccupied_ThrowsException() {
        // Arrange
        TicketRequestDTO request = new TicketRequestDTO(10L, List.of(100L), 1L, false);

        when(authClient.getUserById(1L)).thenReturn(testUser);
        when(catalogService.getMatchSecurely(10L)).thenReturn(testMatch);
        when(ticketRepository.isSeatOccupied(eq(10L), eq(100L), any(LocalDateTime.class))).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> ticketService.buyTickets(request))
                .isInstanceOf(TicketException.class)
                .hasMessageContaining("este deja rezervat");
    }

    @Test
    @DisplayName("Confirm Payment - Succes și trimitere notificare")
    void confirmPayment_Success() {
        // Arrange
        List<Long> ticketIds = List.of(1000L);
        when(ticketRepository.findAllById(ticketIds)).thenReturn(List.of(testTicket));
        when(authClient.getUserById(1L)).thenReturn(testUser);
        when(catalogService.getMatchSecurely(10L)).thenReturn(testMatch);
        when(catalogService.getSeatSecurely(100L)).thenReturn(testSeat);

        // Act
        List<TicketResponseDTO> result = ticketService.confirmPayment(ticketIds);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CONFIRMED);
        assertThat(testTicket.getMailSent()).isTrue();

        verify(notificationClient).sendTicketNotification(anyList(), eq("user@arena.ro"));
        verify(authClient).updatePoints(1L, 1);
    }

    @Test
    @DisplayName("Validate Ticket - Succes la scanare bilet la stadion")
    void validateTicket_Success() {
        // Arrange
        testTicket.setStatus(TicketStatus.CONFIRMED);
        when(ticketRepository.findByTicketCode("TK-123456")).thenReturn(Optional.of(testTicket));
        when(catalogService.getMatchSecurely(10L)).thenReturn(testMatch);

        // Act
        ticketService.validateTicket("TK-123456");

        // Assert
        assertThat(testTicket.isUsed()).isTrue();
        verify(ticketRepository).save(testTicket);
    }

    @Test
    @DisplayName("Validate Ticket - Eșuează dacă biletul a fost deja folosit")
    void validateTicket_AlreadyUsed_ThrowsException() {
        // Arrange
        testTicket.setUsed(true);
        when(ticketRepository.findByTicketCode("TK-123456")).thenReturn(Optional.of(testTicket));

        // Act & Assert
        assertThatThrownBy(() -> ticketService.validateTicket("TK-123456"))
                .isInstanceOf(TicketException.class)
                .hasMessageContaining("deja scanat");
    }

    @Test
    @DisplayName("Fallback Resilience4j - Se activează când Notification Client pică")
    void fallbackForNotification_Triggered() {
        // Arrange
        List<Long> ticketIds = List.of(1000L);
        when(ticketRepository.findAllById(ticketIds)).thenReturn(List.of(testTicket));
        when(catalogService.getMatchSecurely(10L)).thenReturn(testMatch);
        when(catalogService.getSeatSecurely(100L)).thenReturn(testSeat);

        // Act
        List<TicketResponseDTO> result = ticketService.fallbackForNotification(ticketIds, new RuntimeException("Notification Down"));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(testTicket.getMailSent()).isFalse();
        assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CONFIRMED);
        verify(ticketRepository).save(testTicket);
    }

    @Test
    @DisplayName("Database Housekeeping - Ștergere automată a rezervărilor expirate")
    void performDatabaseHousekeeping_Success() {
        // Act
        ticketService.performDatabaseHousekeeping();

        // Assert
        verify(ticketRepository).deleteExpiredOrCancelledTickets(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Get Tickets By User ID - Paginare cu succes")
    void getTicketsByUserId_Pagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));

        when(ticketRepository.findByUserId(1L, pageable)).thenReturn(ticketPage);
        when(catalogService.getMatchSecurely(10L)).thenReturn(testMatch);
        when(catalogService.getSeatSecurely(100L)).thenReturn(testSeat);

        // Act
        Page<TicketListDTO> result = ticketService.getTicketsByUserId(1L, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        // MODIFICAT: din getOpponentName() în opponentName() pentru că este un Record
        assertThat(result.getContent().get(0).opponentName()).isEqualTo("FCSB");
    }
}
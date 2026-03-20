package com.arena.ticketing.service;

import com.arena.ticketing.dto.*;
import com.arena.ticketing.exception.TicketException;
import com.arena.ticketing.model.*;
import com.arena.ticketing.repository.*;
import com.arena.ticketing.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private StadiumRepository stadiumRepository;
    @Mock private MatchSectorPriceRepository matchSectorPriceRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private TicketRepository ticketRepository;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Match match;
    private Stadium stadium;

    @BeforeEach
    void setUp() {
        stadium = new Stadium();
        stadium.setId(1L);
        stadium.setName("Arena Nationala");

        match = new Match();
        match.setId(100L);
        match.setOpponentName("Farul Constanta");
        match.setStatus(MatchStatus.SCHEDULED);
        match.setStadium(stadium);
    }

    @Test
    @DisplayName("Ar trebui să creeze un meci nou cu succes")
    void shouldCreateMatchSuccessfully() {
        MatchRequestDTO dto = new MatchRequestDTO();
        dto.setOpponentName("Farul Constanta");
        dto.setStadiumId(1L);
        dto.setMatchDate(LocalDateTime.now().plusDays(7));

        when(stadiumRepository.findById(1L)).thenReturn(Optional.of(stadium));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        Match created = matchService.createMatch(dto);

        assertNotNull(created);
        assertEquals("Farul Constanta", created.getOpponentName());
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    @DisplayName("Ar trebui să schimbe statusul meciului")
    void shouldUpdateMatchStatus() {
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));

        matchService.updateMatchStatus(100L, MatchStatus.FINISHED);

        assertEquals(MatchStatus.FINISHED, match.getStatus());
        verify(matchRepository).save(match);
    }

    @Test
    @DisplayName("Ar trebui să returneze prețul corect pentru un sector")
    void shouldReturnCorrectPriceForSector() {
        MatchSectorPrice msp = new MatchSectorPrice();
        msp.setPrice(150.0);

        when(matchSectorPriceRepository.findByMatchIdAndSectorId(100L, 1L))
                .thenReturn(Optional.of(msp));

        Double price = matchService.getPriceForSector(100L, 1L);

        assertEquals(150.0, price);
    }
}
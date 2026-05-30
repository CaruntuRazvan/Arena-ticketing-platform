package com.arena.catalog.service;

import com.arena.catalog.client.NotificationClient;
import com.arena.catalog.dto.*;
import com.arena.catalog.exception.CatalogException;
import com.arena.catalog.model.*;
import com.arena.catalog.repository.*;
import com.arena.catalog.service.impl.MatchServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private MatchSectorPriceRepository matchSectorPriceRepository;
    @Mock private StadiumRepository stadiumRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private NotificationClient notificationClient;

    @InjectMocks
    private MatchServiceImpl matchService;

    private Match testMatch;
    private Stadium testStadium;

    @BeforeEach
    void setUp() {
        testStadium = new Stadium();
        testStadium.setId(1L);
        testStadium.setName("Arena Nationala");

        testMatch = new Match();
        testMatch.setId(10L);
        testMatch.setOpponentName("FCSB");
        testMatch.setMatchDate(LocalDateTime.now().plusDays(5));
        testMatch.setStadium(testStadium);
        testMatch.setStatus(MatchStatus.SCHEDULED);
        testMatch.setPublished(false);
    }

    @Test
    @DisplayName("Create Match - Succes")
    void createMatch_Success() {
        MatchRequestDTO request = new MatchRequestDTO("FCSB", LocalDateTime.now().plusDays(5), 1L, null);
        when(stadiumRepository.findById(1L)).thenReturn(Optional.of(testStadium));
        when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

        MatchDTO result = matchService.createMatch(request);

        assertThat(result.getOpponentName()).isEqualTo("FCSB");
        assertThat(result.getStadiumName()).isEqualTo("Arena Nationala");
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("Publish Match - Aruncă excepție dacă nu are prețuri")
    void publishMatch_NoPrices_ThrowsException() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(testMatch));
        when(matchSectorPriceRepository.existsByMatchId(10L)).thenReturn(false);

        assertThatThrownBy(() -> matchService.publishMatch(10L))
                .isInstanceOf(CatalogException.class)
                .hasMessageContaining("configura prețurile");
    }

    @Test
    @DisplayName("Publish Match - Succes și apel către Notification (chiar dacă Notification crapă)")
    void publishMatch_Success_EvenIfNotificationFails() {
        // Arrange
        when(matchRepository.findById(10L)).thenReturn(Optional.of(testMatch));
        when(matchSectorPriceRepository.existsByMatchId(10L)).thenReturn(true);
        // Simulăm un eșec la Notification Service
        doThrow(new RuntimeException("API Down")).when(notificationClient).broadcastMatch(any());

        matchService.publishMatch(10L);

        // Assert
        assertThat(testMatch.isPublished()).isTrue();
        verify(matchRepository).save(testMatch);
        verify(notificationClient).broadcastMatch(any());
    }

    @Test
    @DisplayName("Set Match Prices - Succes")
    void setMatchPrices_Success() {
        // Arrange
        PriceRequestDTO priceDto = new PriceRequestDTO(10L, 1L, 50.0);
        Sector sector = new Sector();
        sector.setId(1L);

        when(matchRepository.findById(10L)).thenReturn(Optional.of(testMatch));
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(matchSectorPriceRepository.findByMatchIdAndSectorId(10L, 1L)).thenReturn(Optional.empty());

        matchService.setMatchPrices(List.of(priceDto));

        verify(matchSectorPriceRepository).save(any(MatchSectorPrice.class));
    }

    @Test
    @DisplayName("Update Status - Aruncă excepție pentru meci anulat")
    void updateMatchStatus_CancelledMatch_ThrowsException() {
        testMatch.setStatus(MatchStatus.CANCELLED);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(testMatch));

        assertThatThrownBy(() -> matchService.updateMatchStatus(10L, MatchStatus.FINISHED))
                .isInstanceOf(CatalogException.class)
                .hasMessageContaining("anulat");
    }

    @Test
    @DisplayName("Auto Update Status - Verificare logică programată")
    void autoUpdateMatchStatus_Success() {
        // Arrange
        List<Match> pastMatches = List.of(testMatch);
        when(matchRepository.findByMatchDateBeforeAndStatus(any(), eq(MatchStatus.SCHEDULED)))
                .thenReturn(pastMatches);

        matchService.autoUpdateMatchStatus();

        assertThat(testMatch.getStatus()).isEqualTo(MatchStatus.FINISHED);
        verify(matchRepository).saveAll(pastMatches);
    }
}
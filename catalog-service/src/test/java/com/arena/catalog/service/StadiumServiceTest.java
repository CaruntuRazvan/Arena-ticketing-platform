package com.arena.catalog.service;

import com.arena.catalog.dto.SectorDTO;
import com.arena.catalog.dto.SectorRequestDTO;
import com.arena.catalog.dto.StadiumDTO;
import com.arena.catalog.exception.CatalogException;
import com.arena.catalog.model.Seat;
import com.arena.catalog.model.Sector;
import com.arena.catalog.model.Stadium;
import com.arena.catalog.repository.SeatRepository;
import com.arena.catalog.repository.SectorRepository;
import com.arena.catalog.repository.StadiumRepository;
import com.arena.catalog.service.impl.StadiumServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StadiumServiceTest {

    @Mock private StadiumRepository stadiumRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private SeatRepository seatRepository;

    @InjectMocks
    private StadiumServiceImpl stadiumService;

    private Stadium testStadium;

    @BeforeEach
    void setUp() {
        testStadium = new Stadium();
        testStadium.setId(1L);
        testStadium.setName("Arena Nationala");
        testStadium.setLocation("Bucuresti");
    }

    @Test
    @DisplayName("Create Stadium - Succes")
    void createStadium_Success() {
        StadiumDTO inputDto = new StadiumDTO(null, "Arena Nationala", "Bucuresti", 0);
        when(stadiumRepository.save(any(Stadium.class))).thenReturn(testStadium);

        StadiumDTO result = stadiumService.createStadium(inputDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Arena Nationala");
        verify(stadiumRepository).save(any(Stadium.class));
    }

    @Test
    @DisplayName("Add Sector - Succes și verificare generare locuri")
    void addSector_Success() {
        // Arrange
        // Adaugam un sector cu 2 randuri si 5 locuri pe rand = 10 locuri in total
        SectorRequestDTO request = new SectorRequestDTO("Tribuna 1", 2, 5, 1L);

        Sector savedSector = new Sector();
        savedSector.setId(10L);
        savedSector.setName("Tribuna 1");
        savedSector.setStadium(testStadium);

        when(stadiumRepository.findById(1L)).thenReturn(Optional.of(testStadium));
        when(sectorRepository.save(any(Sector.class))).thenReturn(savedSector);

        // Act
        SectorDTO result = stadiumService.addSector(request);

        // Assert
        assertThat(result.getTotalSeats()).isEqualTo(10); // 2 * 5
        assertThat(result.getName()).isEqualTo("Tribuna 1");

        // Verificăm că s-a apelat saveAll pentru cele 10 locuri
        verify(seatRepository).saveAll(anyList());

        // Verificăm argumentul trimis la saveAll pentru a vedea dacă s-au creat corect 10 obiecte Seat
        verify(seatRepository).saveAll(argThat(seats -> ((List<Seat>) seats).size() == 10));
    }

    @Test
    @DisplayName("Get Sectors by Stadium ID - Aruncă excepție dacă stadionul nu există")
    void getSectorsByStadiumId_NotFound_ThrowsException() {
        when(stadiumRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> stadiumService.getSectorsByStadiumId(99L))
                .isInstanceOf(CatalogException.class)
                .hasMessageContaining("nu a fost găsit");
    }

    @Test
    @DisplayName("Delete Sector - Succes")
    void deleteSector_Success() {
        Sector sector = new Sector();
        sector.setId(1L);
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));

        stadiumService.deleteSector(1L);

        verify(seatRepository).deleteBySectorId(1L);
        verify(sectorRepository).delete(sector);
    }
}
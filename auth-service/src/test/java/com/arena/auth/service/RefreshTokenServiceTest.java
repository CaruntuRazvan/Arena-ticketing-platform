package com.arena.auth.service;

import com.arena.auth.exception.AuthException;
import com.arena.auth.model.RefreshToken;
import com.arena.auth.model.User;
import com.arena.auth.repository.RefreshTokenRepository;
import com.arena.auth.repository.UserRepository;
import com.arena.auth.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testToken = new RefreshToken();
        testToken.setId(1L);
        testToken.setUser(testUser);
        testToken.setToken(UUID.randomUUID().toString());
        testToken.setExpiryDate(Instant.now().plusSeconds(3600)); // Valabil 1 oră
    }

    @Test
    @DisplayName("Create Refresh Token - Succes (Șterge vechiul token și creează unul nou)")
    void createRefreshToken_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());

        // Verificăm ordinea operațiilor (importantă pentru flush())
        verify(refreshTokenRepository).deleteByUser(testUser);
        verify(refreshTokenRepository).flush();
        verify(refreshTokenRepository).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Verify Expiration - Succes (Token valid)")
    void verifyExpiration_ValidToken() {
        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(testToken);

        // Assert
        assertThat(result).isEqualTo(testToken);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Verify Expiration - Aruncă excepție și șterge dacă este expirat")
    void verifyExpiration_ExpiredToken_ThrowsException() {
        // Arrange
        testToken.setExpiryDate(Instant.now().minusSeconds(10)); // Expira acum 10 secunde

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(testToken))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Refresh token-ul a expirat");

        verify(refreshTokenRepository).delete(testToken);
    }

    @Test
    @DisplayName("Delete by User ID - Succes")
    void deleteByUserId_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        refreshTokenService.deleteByUserId(1L);

        verify(refreshTokenRepository).deleteByUser(testUser);
    }
}
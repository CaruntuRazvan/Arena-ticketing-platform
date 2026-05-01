package com.arena.auth.service.impl;

import com.arena.auth.dto.LoginResponseDTO;
import com.arena.auth.dto.TokenRefreshRequestDTO;
import com.arena.auth.model.RefreshToken;
import com.arena.auth.model.User;
import com.arena.auth.repository.RefreshTokenRepository;
import com.arena.auth.repository.UserRepository;
import com.arena.auth.service.RefreshTokenService;
import com.arena.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // 30 de zile
    private final Long refreshTokenDurationMs = 2592000000L;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        // Opțional: Ștergem token-urile vechi ale userului dacă există
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Userul nu există!"));

        //Ștergem token-ul vechi daca exista
        refreshTokenRepository.deleteByUser(user);
        // 2. FORȚĂM baza de date să execute DELETE-ul
        refreshTokenRepository.flush();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AuthException("Refresh token-ul a expirat. Te rugăm să te loghezi din nou.");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

}
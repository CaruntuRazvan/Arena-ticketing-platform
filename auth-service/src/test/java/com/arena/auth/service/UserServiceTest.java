package com.arena.auth.service;

import com.arena.auth.config.JwtUtils;
import com.arena.auth.client.NotificationClient;
import com.arena.auth.dto.*;
import com.arena.auth.exception.AuthException;
import com.arena.auth.model.User;
import com.arena.auth.model.UserProfile;
import com.arena.auth.repository.UserProfileRepository;
import com.arena.auth.repository.UserRepository;
import com.arena.auth.service.RefreshTokenService;
import com.arena.auth.service.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private NotificationClient notificationClient;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Inițializare User de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@arena.ro");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setRole("USER");
        testUser.setLoyaltyPoints(10);

        UserProfile profile = new UserProfile();
        profile.setFirstName("Andrei");
        profile.setLastName("Ionescu");
        testUser.setProfile(profile);
    }

    @Test
    @DisplayName("Login - Succes (fără RememberMe)")
    void login_Success() {
        LoginRequestDTO request = new LoginRequestDTO("testUser", "password", false);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtils.generateToken(testUser)).thenReturn("fake-jwt-token");

        LoginResponseDTO response = userService.login(request);

        assertThat(response.accessToken()).isEqualTo("fake-jwt-token");
        assertThat(response.username()).isEqualTo("testUser");
        assertThat(response.refreshToken()).isNull();
        verify(refreshTokenService, never()).createRefreshToken(anyLong());
    }

    @Test
    @DisplayName("Login - Aruncă excepție pentru parolă greșită")
    void login_WrongPassword_ThrowsException() {
        LoginRequestDTO request = new LoginRequestDTO("testUser", "wrong", false);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Parolă incorectă!");
    }

    @Test
    @DisplayName("Register - Succes și salvare în Redis")
    void register_Success() {
        RegisterRequestDTO request = new RegisterRequestDTO("new", "email@test.com", "pass", "Ion", "Pop", "0722");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        UserResponseDTO result = userService.registerUser(request);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
        verify(valueOperations).set(startsWith("verify:"), anyString(), eq(10L), eq(TimeUnit.MINUTES));
        verify(notificationClient).sendEmail(any(NotificationRequestDTO.class));
    }

    @Test
    @DisplayName("Verify Account - Succes")
    void verifyAccount_Success() {
        String email = "test@test.com";
        String code = "123456";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("verify:" + email)).thenReturn(code);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        userService.verifyAccount(email, code);

        assertThat(testUser.isEnabled()).isTrue();
        verify(userRepository).save(testUser);
        verify(redisTemplate).delete("verify:" + email);
    }

    @Test
    @DisplayName("Get All Users - Testare Paginare")
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("Get All Emails - Succes")
    void getAllEmails_Success() {
        when(userRepository.findAllEmails()).thenReturn(List.of("test@arena.ro"));

        List<String> result = userService.getAllEmails();

        assertThat(result).contains("test@arena.ro");
    }

    @Test
    @DisplayName("Delete User - Succes")
    void deleteUser_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        userService.deleteUser(1L);
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser); // Verifică dacă în codul tău e .delete(testUser) sau .deleteById(1L)
    }

    @Test
    @DisplayName("Update Loyalty Points - Succes")
    void updateLoyaltyPoints_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.updateLoyaltyPoints(1L, 20);

        assertThat(testUser.getLoyaltyPoints()).isEqualTo(30); // 10 inițial + 20
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Get My Profile - Succes")
    void getMyProfile_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        UserResponseDTO result = userService.getMyProfile("testUser");

        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getFirstName()).isEqualTo("Andrei");
    }

    @Test
    @DisplayName("Logout - Verificare Blacklist Redis și ștergere Refresh Token")
    void logout_Success() {
        String token = "Bearer fake.jwt.token";
        String jwt = "fake.jwt.token";
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // +1h

        when(jwtUtils.getExpirationDateFromToken(jwt)).thenReturn(futureDate);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testUser");
        when(jwtUtils.getClaimsFromToken(jwt)).thenReturn(claims);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        userService.logout(token);

        // Verificăm Blacklist în Redis
        verify(valueOperations).set(eq("blacklist:" + jwt), eq("logout"), anyLong(), eq(TimeUnit.MILLISECONDS));
        // Verificăm ștergerea Refresh Token
        verify(refreshTokenService).deleteByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Logout - Ignoră dacă token-ul este invalid")
    void logout_InvalidToken_Ignored() {
        userService.logout("InvalidToken");
        verify(redisTemplate, never()).opsForValue();
    }
}
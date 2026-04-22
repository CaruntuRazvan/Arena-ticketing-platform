package com.arena.auth.service.impl;

import com.arena.auth.dto.LoginRequestDTO;
import com.arena.auth.dto.LoginResponseDTO;
import com.arena.auth.dto.RegisterRequestDTO;
import com.arena.auth.dto.UserResponseDTO;
import com.arena.auth.model.User;
import com.arena.auth.model.UserProfile;
import com.arena.auth.repository.UserRepository;
import com.arena.auth.service.UserService;
import com.arena.auth.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.arena.auth.exception.AuthException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.arena.auth.service.EmailService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("Utilizator negăsit!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Parolă incorectă!");
        }

        // Generezi token-ul
        String token = jwtUtils.generateToken(user);

        // Returnezi DTO-ul (dacă ai ales varianta cu record)
        return new LoginResponseDTO(token, user.getUsername(), user.getRole());
    }
    @Override
    @Transactional
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthException("Username-ul există deja!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setLoyaltyPoints(0);
        user.setEnabled(false); // <--- DEVENIT FALSE (trebuie verificat)

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        user.setProfile(profile);
        profile.setUser(user);

        User savedUser = userRepository.save(user);

        // --- LOGICA REDIS (NoSQL) ---
        String code = String.valueOf((int)((Math.random() * 900000) + 100000));
        String redisKey = "verify:" + user.getEmail();

        // Salvăm în Redis pentru 10 minute
        redisTemplate.opsForValue().set(redisKey, code, 10, TimeUnit.MINUTES);

        // Trimitem Email
        emailService.sendSimpleMessage(
                user.getEmail(),
                "Activare Cont Arena auth",
                "Codul tău de activare este: " + code
        );

        return mapToDTO(savedUser);
    }
    @Override
    @Transactional
    public void verifyAccount(String email, String code) {
        String redisKey = "verify:" + email;
        String savedCode = redisTemplate.opsForValue().get(redisKey);

        if (savedCode == null) {
            throw new AuthException("Codul a expirat sau nu a fost generat!");
        }

        if (!savedCode.equals(code)) {
            throw new AuthException("Cod incorect!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Utilizator negăsit!"));

        user.setEnabled(true); // ACTIVAM ÎN SQL
        userRepository.save(user);

        redisTemplate.delete(redisKey); // Curatam NoSQL
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        // 1. Verificăm dacă userul există în SQL și dacă e deja activat
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Utilizatorul nu există!"));

        if (user.isEnabled()) {
            throw new AuthException("Acest cont este deja activat!");
        }
        /*
        // 2. Generăm un cod NOU
        String newCode = String.valueOf((int)((Math.random() * 900000) + 100000));

        // 3. Îl punem în Redis (va suprascrie codul vechi dacă mai exista)

        redisTemplate.opsForValue().set(redisKey, newCode, 10, TimeUnit.MINUTES);
        */
        String redisKey = "verify:" + email;
        // 2. Verificăm timpul rămas (getExpire returnează -2 dacă cheia nu există)
        long remainingSeconds = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);

        // Dacă codul vechi mai are peste 8 minute de viață (adică a fost cerut acum < 2 min)
        // 600 total - 480 rămase = 120 secunde trecute (2 minute)
        if (remainingSeconds > 480) {
            long waitTime = remainingSeconds - 480;
            throw new AuthException("Te rugăm să aștepți " + waitTime + " secunde înainte de a cere un cod nou.");
        }

        // 3. Generăm și salvăm codul NOU (suprascrie automat în Redis)
        String newCode = String.valueOf((int)((Math.random() * 900000) + 100000));
        redisTemplate.opsForValue().set(redisKey, newCode, 10, TimeUnit.MINUTES);

        // 4. Trimitem noul mail
        emailService.sendSimpleMessage(
                email,
                "Cod Nou de Verificare Arena auth",
                "Noul tău cod de activare este: " + newCode + ". Acesta expiră în 10 minute."
        );

        System.out.println("[REDIS] Cod nou generat pentru: " + email);
    }

    @Override
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateLoyaltyPoints(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("Utilizator negăsit!"));

        user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
        userRepository.save(user);
    }

    private UserResponseDTO mapToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfile() != null ? user.getProfile().getFirstName() : null,
                user.getProfile() != null ? user.getProfile().getLastName() : null,
                user.getRole(),
                user.getLoyaltyPoints() // <--- TRIMITE PUNCTELE AICI
        );
    }
}
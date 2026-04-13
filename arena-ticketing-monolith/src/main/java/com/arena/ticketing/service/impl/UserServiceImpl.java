package com.arena.ticketing.service.impl;

import com.arena.ticketing.dto.LoginRequestDTO;
import com.arena.ticketing.dto.RegisterRequestDTO;
import com.arena.ticketing.dto.UserResponseDTO;
import com.arena.ticketing.model.User;
import com.arena.ticketing.model.UserProfile;
import com.arena.ticketing.repository.UserRepository;
import com.arena.ticketing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.arena.ticketing.service.EmailService;
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
    @Override
    public UserResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit!"));
        if (!user.isEnabled()) {
            throw new RuntimeException("Contul nu este activat! Verifică email-ul.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Parolă incorectă!");
        }

        return mapToDTO(user);
    }
    @Override
    @Transactional
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username-ul există deja!");
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
                "Activare Cont Arena Ticketing",
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
            throw new RuntimeException("Codul a expirat sau nu a fost generat!");
        }

        if (!savedCode.equals(code)) {
            throw new RuntimeException("Cod incorect!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizator negăsit!"));

        user.setEnabled(true); // ACTIVAM ÎN SQL
        userRepository.save(user);

        redisTemplate.delete(redisKey); // Curatam NoSQL
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        // 1. Verificăm dacă userul există în SQL și dacă e deja activat
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu există!"));

        if (user.isEnabled()) {
            throw new RuntimeException("Acest cont este deja activat!");
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
            throw new RuntimeException("Te rugăm să aștepți " + waitTime + " secunde înainte de a cere un cod nou.");
        }

        // 3. Generăm și salvăm codul NOU (suprascrie automat în Redis)
        String newCode = String.valueOf((int)((Math.random() * 900000) + 100000));
        redisTemplate.opsForValue().set(redisKey, newCode, 10, TimeUnit.MINUTES);

        // 4. Trimitem noul mail
        emailService.sendSimpleMessage(
                email,
                "Cod Nou de Verificare Arena Ticketing",
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

    private UserResponseDTO mapToDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfile() != null ? user.getProfile().getFirstName() : null,
                user.getProfile() != null ? user.getProfile().getLastName() : null,
                user.getRole()
        );
    }
}
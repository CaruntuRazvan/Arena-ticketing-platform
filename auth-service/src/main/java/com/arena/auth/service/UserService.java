package com.arena.auth.service;


import com.arena.auth.dto.*;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDTO registerUser(RegisterRequestDTO request);
    void verifyAccount(String email, String code);
    void resendVerificationCode(String email);
    //UserResponseDTO login(LoginRequestDTO loginRequest);
    LoginResponseDTO login(LoginRequestDTO request);
    Optional<UserResponseDTO> getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    void deleteUser(Long id);
    void updateLoyaltyPoints(Long userId, int points);
    void logout(String token);
}

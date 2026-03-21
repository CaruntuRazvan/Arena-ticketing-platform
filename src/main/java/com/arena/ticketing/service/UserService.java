package com.arena.ticketing.service;

import com.arena.ticketing.dto.RegisterRequestDTO;
import com.arena.ticketing.dto.LoginRequestDTO;
import com.arena.ticketing.dto.UserResponseDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDTO registerUser(RegisterRequestDTO request);
    UserResponseDTO login(LoginRequestDTO loginRequest);
    Optional<UserResponseDTO> getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    void deleteUser(Long id);
}

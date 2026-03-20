package com.arena.ticketing.service;

import com.arena.ticketing.dto.RegisterRequestDTO;
import com.arena.ticketing.dto.LoginRequestDTO;
import com.arena.ticketing.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(RegisterRequestDTO request);
    User login(LoginRequestDTO loginRequest);
    // Alte metode utile pentru managementul userilor
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    void deleteUser(Long id);
}

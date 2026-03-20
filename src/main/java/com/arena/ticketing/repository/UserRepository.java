package com.arena.ticketing.repository;
import com.arena.ticketing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Util pentru validare la înregistrare: "Email-ul există deja"
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}
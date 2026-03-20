package com.arena.ticketing.repository;

import com.arena.ticketing.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Această metodă va returna "true" dacă locul e deja ocupat la meciul respectiv
    boolean existsByMatchIdAndSeatId(Long matchId, Long seatId);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByMatchId(Long matchId);

    long countByMatchId(Long matchId);

    long countByMatchIdAndSeatSectorId(Long matchId, Long id);

    // În TicketRepository.java
    Optional<Ticket> findByTicketCode(String ticketCode);
    long countByMatchIdAndUserId(Long matchId, Long userId);
}
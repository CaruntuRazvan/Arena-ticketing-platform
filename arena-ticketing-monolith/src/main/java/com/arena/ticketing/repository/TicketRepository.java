package com.arena.ticketing.repository;

import com.arena.ticketing.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Această metodă va returna "true" dacă locul e deja ocupat la meciul respectiv
    boolean existsByMatchIdAndSeatId(Long matchId, Long seatId);
    List<Ticket> findByUserId(Long userId);
    List<Ticket> findByMatchId(Long matchId);

    @Query("SELECT t.seat.id FROM Ticket t WHERE t.match.id = :matchId AND t.seat.sector.id = :sectorId")
    List<Long> findOccupiedSeatIdsByMatchAndSector(@Param("matchId") Long matchId, @Param("sectorId") Long sectorId);

    long countByMatchId(Long matchId);

    long countByMatchIdAndSeatSectorId(Long matchId, Long id);

    // În TicketRepository.java
    Optional<Ticket> findByTicketCode(String ticketCode);
    long countByMatchIdAndUserId(Long matchId, Long userId);
    // ACEASTA ESTE LOGICA DE 15 MINUTE
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.match.id = :matchId AND t.seat.id = :seatId " +
            "AND (t.status = 'CONFIRMED' OR (t.status = 'PENDING' AND t.createdAt > :timeout))")
    boolean isSeatOccupied(
            @Param("matchId") Long matchId,
            @Param("seatId") Long seatId,
            @Param("timeout") LocalDateTime timeout
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Ticket t WHERE (t.status = 'CANCELLED') OR (t.status = 'PENDING' AND t.createdAt < :threshold)")
    void deleteExpiredOrCancelledTickets(@Param("threshold") LocalDateTime threshold);
}
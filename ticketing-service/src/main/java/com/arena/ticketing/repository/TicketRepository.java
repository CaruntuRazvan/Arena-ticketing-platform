package com.arena.ticketing.repository;

import com.arena.ticketing.model.Ticket;
import com.arena.ticketing.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    boolean existsByMatchIdAndSeatId(Long matchId, Long seatId);
    /*
    List<Ticket> findByUserId(Long userId);
    */
    Page<Ticket> findByUserId(Long userId, Pageable pageable);
    Page<Ticket> findByMatchId(Long matchId, Pageable pageable);
    // for revenue statistics
    List<Ticket> findByMatchId(Long matchId);
    @Query("SELECT t.seatId FROM Ticket t WHERE t.matchId = :matchId")
    List<Long> findOccupiedSeatIdsByMatch(@Param("matchId") Long matchId);

    long countByMatchId(Long matchId);

    // long countByMatchIdAndSeatSectorId(Long matchId, Long sectorId);


    Optional<Ticket> findByTicketCode(String ticketCode);

    long countByMatchIdAndUserId(Long matchId, Long userId);

    @Query("SELECT t.seatId FROM Ticket t WHERE t.matchId = :matchId " +
            "AND t.seatId IN :seatIds " +
            "AND (t.status = 'CONFIRMED' OR (t.status = 'PENDING' AND t.createdAt > :timeout))")
    List<Long> findOccupiedSeatIdsInList(
            @Param("matchId") Long matchId,
            @Param("seatIds") List<Long> seatIds,
            @Param("timeout") LocalDateTime timeout
    );

    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.matchId = :matchId AND t.seatId = :seatId " +
            "AND (t.status = 'CONFIRMED' OR (t.status = 'PENDING' AND t.createdAt > :timeout))")
    boolean isSeatOccupied(
            @Param("matchId") Long matchId,
            @Param("seatId") Long seatId,
            @Param("timeout") LocalDateTime timeout
    );

    List<Ticket> findByStatusAndMailSentFalse(TicketStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Ticket t WHERE (t.status = 'CANCELLED') OR (t.status = 'PENDING' AND t.createdAt < :threshold)")
    void deleteExpiredOrCancelledTickets(@Param("threshold") LocalDateTime threshold);


    @Query("SELECT DISTINCT t.matchId FROM Ticket t WHERE t.status = com.arena.ticketing.model.TicketStatus.CONFIRMED AND t.used = false")
    List<Long> findDistinctMatchIdsWithConfirmedTickets();

    @Modifying
    @Query("UPDATE Ticket t SET t.status = com.arena.ticketing.model.TicketStatus.CANCELLED " +
            "WHERE t.status = com.arena.ticketing.model.TicketStatus.CONFIRMED " +
            "AND t.used = false AND t.matchId = :matchId")
    void expireUnusedTicketsForMatch(@Param("matchId") Long matchId);

}
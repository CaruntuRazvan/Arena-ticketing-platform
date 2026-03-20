package com.arena.ticketing.repository;
import com.arena.ticketing.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findBySectorId(Long sectorId);

    long countBySectorStadiumId(Long id);

    long countBySectorId(Long id);

    boolean existsBySectorId(Long sectorId);
}
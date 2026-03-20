package com.arena.ticketing.repository;

import com.arena.ticketing.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {
}
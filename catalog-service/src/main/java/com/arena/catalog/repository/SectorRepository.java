package com.arena.catalog.repository;


import com.arena.catalog.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {
    Optional<Sector> findByStadiumIdAndName(Long stadiumId, String name);
    List<Sector> findByStadiumId(Long stadiumId);
}
package com.arena.catalog.repository;


import com.arena.catalog.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {
}
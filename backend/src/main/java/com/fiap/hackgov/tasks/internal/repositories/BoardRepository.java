package com.fiap.hackgov.tasks.internal.repositories;

import com.fiap.hackgov.tasks.internal.entities.Board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    Page<Board> findAll(Pageable pageable);

    Page<Board> findAllByCityHall_Id(UUID cityHallId, Pageable pageable);

    Optional<Board> findById(UUID id);

    Optional<Board> findByIdAndCityHall_Id(UUID id, UUID cityHallId);
    Optional<Board> findFirstByCityHall_IdAndSector_Id(UUID cityHallId, UUID sectorId);

}

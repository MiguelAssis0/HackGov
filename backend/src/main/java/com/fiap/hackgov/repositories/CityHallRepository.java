package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.CityHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CityHallRepository extends JpaRepository<CityHall, UUID> {
}

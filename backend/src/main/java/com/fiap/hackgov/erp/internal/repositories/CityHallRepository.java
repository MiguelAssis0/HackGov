package com.fiap.hackgov.erp.internal.repositories;

import com.fiap.hackgov.erp.internal.entities.CityHall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityHallRepository extends JpaRepository<CityHall, UUID> {
    Optional<CityHall> findByCnpj(String cnpj);
}

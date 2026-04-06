package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.State;
import com.fiap.hackgov.entities.enums.UF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {
    Optional<State> findByUf(UF uf);
}

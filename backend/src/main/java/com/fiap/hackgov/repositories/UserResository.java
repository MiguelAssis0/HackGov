package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserResository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
}

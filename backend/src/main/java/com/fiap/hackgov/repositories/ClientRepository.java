package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.Client;
import com.fiap.hackgov.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findUserByEmail(String email);
}

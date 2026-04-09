package com.fiap.hackgov.auth.internal.repositories;

import com.fiap.hackgov.auth.internal.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findUserByEmail(String email);
    Optional<User> findByFirstName(String name);
    Optional<User> findByLastName(String name);
}

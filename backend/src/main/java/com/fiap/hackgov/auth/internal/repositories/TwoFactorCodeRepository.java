package com.fiap.hackgov.auth.internal.repositories;

import com.fiap.hackgov.auth.internal.entities.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, String> {

    Optional<TwoFactorCode> findByEmail(String email);

    @Modifying
    @Transactional
    void deleteByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM TwoFactorCode t WHERE t.expiration < :now")
    void deleteAllExpired(LocalDateTime now);
}
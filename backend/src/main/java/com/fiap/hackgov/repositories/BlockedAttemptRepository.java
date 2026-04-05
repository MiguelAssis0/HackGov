package com.fiap.hackgov.repositories;

import com.fiap.hackgov.entities.BlockedAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BlockedAttemptRepository extends JpaRepository<BlockedAttempt, Long> {

    Optional<BlockedAttempt> findByKey(String key);

    @Query("SELECT b FROM BlockedAttempt b WHERE b.permanentlyBlocked = true " +
            "OR b.blockedUntil > :now")
    java.util.List<BlockedAttempt> findAllCurrentlyBlocked(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM BlockedAttempt b WHERE b.permanentlyBlocked = false " +
            "AND b.blockedUntil IS NULL " +
            "AND b.totalAttempts < 5 " +
            "AND b.updatedAt < :before")
    void deleteIrrelevantOldRecords(LocalDateTime before);
}
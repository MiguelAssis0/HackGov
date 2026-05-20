package com.fiap.hackgov.bidding.internal.repositories;

import com.fiap.hackgov.bidding.internal.entities.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NoticeRepository extends JpaRepository<Notice, UUID> {

    Optional<Notice> findByLicitationProcessId(UUID licitationProcessId);

    boolean existsByNoticeNumber(String noticeNumber);
}

package com.fiap.hackgov.bidding.internal.DTOs.notice;

import com.fiap.hackgov.bidding.internal.entities.enums.NoticeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record NoticeResponseDTO(

        UUID id,

        UUID licitationProcessId,

        String licitationProcessNumber,

        String noticeNumber,

        String title,

        String objectDescription,

        String content,

        NoticeStatus status,

        LocalDate publicationDate,

        LocalDate proposalOpeningDate,

        LocalDate proposalClosingDate,

        BigDecimal estimatedValue,

        UUID createdById,

        String createdByName,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}

package com.fiap.hackgov.bidding.internal.DTOs.notice;

import com.fiap.hackgov.bidding.internal.entities.enums.NoticeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateNoticeDTO(

        @NotNull(message = "Licitation process id is required")
        UUID licitationProcessId,

        @NotBlank(message = "Notice number is required")
        @Size(max = 100, message = "Notice number must have at most 100 characters")
        String noticeNumber,

        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 255, message = "Title must contain between 5 and 255 characters")
        String title,

        @NotBlank(message = "Object description is required")
        @Size(min = 10, max = 2000, message = "Object description must contain between 10 and 2000 characters")
        String objectDescription,

        @Size(max = 10000, message = "Content must have at most 10000 characters")
        String content,

        @NotNull(message = "Notice status is required")
        NoticeStatus status,

        @NotNull(message = "Publication date is required")
        LocalDate publicationDate,

        @NotNull(message = "Proposal opening date is required")
        LocalDate proposalOpeningDate,

        @NotNull(message = "Proposal closing date is required")
        LocalDate proposalClosingDate,

        @NotNull(message = "Estimated value is required")
        @DecimalMin(value = "0.01", message = "Estimated value must be greater than zero")
        BigDecimal estimatedValue
) {
}

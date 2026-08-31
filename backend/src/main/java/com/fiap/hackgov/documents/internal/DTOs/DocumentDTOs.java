package com.fiap.hackgov.documents.internal.DTOs;

import com.fiap.hackgov.documents.internal.entities.MunicipalDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public final class DocumentDTOs {
    private DocumentDTOs() {
    }

    public record GeneratedRequest(
            @NotBlank @Size(max = 180) String title,
            @NotBlank @Size(max = 60) String documentType,
            @Size(max = 2000) String description,
            @NotNull MunicipalDocument.Visibility visibility,
            @NotBlank @Size(max = 100_000) String content,
            Set<UUID> destinationIds
    ) {
    }

    public record ForwardRequest(@NotNull Set<UUID> destinationIds) {
    }

    public record Response(
            UUID id, String title, String documentType, String description,
            MunicipalDocument.Visibility visibility, String originalName, String contentType,
            long sizeBytes, UUID ownerId, String ownerName, Set<UUID> destinationIds,
            String sourceType, UUID sourceId, String sourceUrl,
            MunicipalDocument.SignatureStatus signatureStatus, LocalDateTime signedAt,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
    }
}

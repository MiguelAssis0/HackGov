package com.fiap.hackgov.documents.internal.DTOs;

import com.fiap.hackgov.documents.internal.entities.MunicipalDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
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
            Set<UUID> destinationIds,
            String number, Integer year, LocalDate documentDate, String purpose,
            String keywords, String tags, String structuredContent,
            String destinationMode, Set<UUID> sectorDestinationIds, Set<UUID> occupationDestinationIds,
            Boolean additionalAccess, Set<UUID> relatedSectorIds, Set<UUID> relatedEmployeeIds,
            Set<UUID> relatedOccupationIds,
            MunicipalDocument.Kind kind
    ) {
    }

    public record ForwardRequest(@NotNull Set<UUID> destinationIds) {
    }

    public record SignatureRequest(@AssertTrue(message = "Confirme que revisou o documento e deseja assiná-lo eletronicamente.") boolean consentimento) {
    }

    public record Response(
            UUID id, String title, String documentType, String description,
            MunicipalDocument.Visibility visibility, String originalName, String contentType,
            long sizeBytes, UUID ownerId, String ownerName, Set<UUID> destinationIds,
            String sourceType, UUID sourceId, String sourceUrl,
            MunicipalDocument.SignatureStatus signatureStatus, LocalDateTime signedAt,
            String signedByName, String signatureCode, String signatureHash, String signatureStandard,
            String signatureHolder, String signatureIssuer, String signatureCertificateSerial,
            String signatureCertificateFingerprint, LocalDateTime signatureCertificateValidFrom,
            LocalDateTime signatureCertificateValidUntil, boolean signatureTimestampIncluded,
            String signatureProvider, String signatureEnvironment, String signatureExternalReference,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            MunicipalDocument.Kind kind, String number, Integer year, LocalDate documentDate,
            String purpose, String keywords, String tags, String structuredContent,
            UUID sectorId, String sectorName, UUID sourceDocumentId,
            List<String> relatedSectorNames, List<String> relatedEmployeeNames, List<String> relatedOccupationNames,
            String cityHallName
    ) {
    }
}

package com.fiap.hackgov.documents.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.ForwardRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.GeneratedRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.Response;
import com.fiap.hackgov.documents.internal.entities.MunicipalDocument;
import com.fiap.hackgov.documents.internal.repositories.MunicipalDocumentRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MunicipalDocumentService {
    private static final long MAX_SIZE = 15L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "text/plain", "text/csv",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/png", "image/jpeg"
    );

    private final MunicipalDocumentRepository repository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<Response> list(String query, String type, Employee employee) {
        Employee current = require(employee);
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return repository.findDistinctByCityHall_IdOrderByCreatedAtDesc(cityId(current)).stream()
                .filter(document -> canView(document, current))
                .filter(document -> type == null || type.isBlank() || document.getDocumentType().equalsIgnoreCase(type))
                .filter(document -> q.isBlank() || document.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || document.getDescription().toLowerCase(Locale.ROOT).contains(q)
                        || document.getOriginalName().toLowerCase(Locale.ROOT).contains(q))
                .map(this::toResponse).toList();
    }

    @Transactional
    public Response upload(String title, String documentType, String description,
                           MunicipalDocument.Visibility visibility, Set<UUID> destinationIds,
                           MultipartFile file, Employee employee) {
        validateFile(file);
        try {
            return save(title, documentType, description, visibility, destinationIds,
                    safeFilename(file.getOriginalFilename()), normalizeContentType(file.getContentType()), file.getBytes(), employee);
        } catch (IOException exception) {
            throw new BusinessException("Nao foi possivel ler o arquivo enviado");
        }
    }

    @Transactional
    public Response uploadForProcess(UUID requisitionId, String title, String documentType,
                                     String description, MultipartFile file, Employee employee) {
        Response created = upload(title, documentType, description, MunicipalDocument.Visibility.CITY_HALL,
                Set.of(), file, employee);
        MunicipalDocument document = repository.findById(created.id())
                .orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado"));
        document.setSourceType("requisition");
        document.setSourceId(requisitionId);
        document.setSourceUrl("/processos?requisition=" + requisitionId);
        return toResponse(repository.save(document));
    }

    @Transactional(readOnly = true)
    public List<Response> listForProcess(UUID requisitionId, Employee employee) {
        Employee current = require(employee);
        return repository.findByCityHall_IdAndSourceTypeAndSourceIdOrderByCreatedAtDesc(
                        cityId(current), "requisition", requisitionId).stream().filter(item -> canView(item, current))
                .map(this::toResponse).toList();
    }

    @Transactional
    public Response createGenerated(GeneratedRequest request, Employee employee) {
        byte[] content = request.content().getBytes(StandardCharsets.UTF_8);
        return save(request.title(), request.documentType(), request.description(), request.visibility(),
                request.destinationIds(), slug(request.title()) + ".txt", "text/plain", content, employee);
    }

    @Transactional(readOnly = true)
    public MunicipalDocument download(UUID id, Employee employee) {
        MunicipalDocument document = scoped(id, require(employee));
        if (!canView(document, employee)) throw new ResourceNotFoundException("Documento nao encontrado");
        return document;
    }

    @Transactional
    public Response forward(UUID id, ForwardRequest request, Employee employee) {
        Employee current = require(employee);
        MunicipalDocument document = scoped(id, current);
        requireOwnerOrAdmin(document, current);
        document.getDestinations().addAll(destinations(request.destinationIds(), current));
        return toResponse(repository.save(document));
    }

    @Transactional
    public Response signHomologation(UUID id, Employee employee) {
        Employee current = require(employee);
        MunicipalDocument document = scoped(id, current);
        if (!canView(document, current)) throw new ResourceNotFoundException("Documento nao encontrado");
        if (!"application/pdf".equals(document.getContentType())) {
            throw new BusinessException("A assinatura de homologacao aceita apenas documentos PDF");
        }
        document.setSignatureStatus(MunicipalDocument.SignatureStatus.HOMOLOGATION);
        document.setSignedBy(current);
        document.setSignedAt(LocalDateTime.now());
        return toResponse(repository.save(document));
    }

    @Transactional
    public void delete(UUID id, Employee employee) {
        Employee current = require(employee);
        MunicipalDocument document = scoped(id, current);
        requireOwnerOrAdmin(document, current);
        repository.delete(document);
    }

    private Response save(String title, String documentType, String description,
                          MunicipalDocument.Visibility visibility, Set<UUID> destinationIds,
                          String filename, String contentType, byte[] content, Employee employee) {
        Employee current = require(employee);
        if (title == null || title.isBlank()) throw new BusinessException("O titulo e obrigatorio");
        MunicipalDocument document = new MunicipalDocument();
        document.setCityHall(current.getCityHallId());
        document.setOwner(current);
        document.setSector(current.getSectorId());
        document.setTitle(title.trim());
        document.setDocumentType(documentType == null || documentType.isBlank() ? "OTHER" : documentType.trim().toUpperCase(Locale.ROOT));
        document.setDescription(description == null ? "" : description.trim());
        document.setVisibility(visibility == null ? MunicipalDocument.Visibility.PERSONAL : visibility);
        if (document.getVisibility() == MunicipalDocument.Visibility.SECTOR && current.getSectorId() == null) {
            throw new BusinessException("O funcionario precisa estar vinculado a um setor");
        }
        document.setOriginalName(filename);
        document.setContentType(contentType);
        document.setContent(content);
        document.setSizeBytes(content.length);
        document.setDestinations(destinations(destinationIds, current));
        return toResponse(repository.save(document));
    }

    private Set<Employee> destinations(Set<UUID> ids, Employee current) {
        if (ids == null || ids.isEmpty()) return new LinkedHashSet<>();
        Set<Employee> result = new LinkedHashSet<>();
        for (UUID id : ids) {
            Employee target = employeeRepository.findByIdWithDetails(id)
                    .filter(item -> item.getCityHallId() != null && item.getCityHallId().getId().equals(cityId(current)))
                    .orElseThrow(() -> new BusinessException("Todos os destinatarios devem pertencer a prefeitura ativa"));
            result.add(target);
        }
        return result;
    }

    private boolean canView(MunicipalDocument document, Employee employee) {
        if (Roles.ADMIN.equals(employee.getRole()) || document.getOwner().getId().equals(employee.getId())) return true;
        if (document.getDestinations().stream().anyMatch(item -> item.getId().equals(employee.getId()))) return true;
        if (document.getVisibility() == MunicipalDocument.Visibility.CITY_HALL) return true;
        return document.getVisibility() == MunicipalDocument.Visibility.SECTOR
                && employee.getSectorId() != null && document.getSector() != null
                && document.getSector().getId().equals(employee.getSectorId().getId());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("Selecione um arquivo");
        if (file.getSize() > MAX_SIZE) throw new BusinessException("O arquivo deve ter no maximo 15 MB");
        String type = normalizeContentType(file.getContentType());
        if (!ALLOWED_TYPES.contains(type)) throw new BusinessException("Tipo de arquivo nao permitido");
        String name = safeFilename(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (name.endsWith(".exe") || name.endsWith(".bat") || name.endsWith(".sh")) {
            throw new BusinessException("Arquivo executavel nao permitido");
        }
    }

    private MunicipalDocument scoped(UUID id, Employee employee) {
        return repository.findByIdAndCityHall_Id(id, cityId(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado para esta prefeitura"));
    }

    private void requireOwnerOrAdmin(MunicipalDocument document, Employee employee) {
        if (!Roles.ADMIN.equals(employee.getRole()) && !document.getOwner().getId().equals(employee.getId())) {
            throw new BusinessException("Somente o proprietario ou um administrador pode alterar o documento");
        }
    }

    private Employee require(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        cityId(employee);
        return employee;
    }

    private UUID cityId(Employee employee) {
        if (employee.getCityHallId() == null)
            throw new BusinessException("O usuario precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }

    private Response toResponse(MunicipalDocument document) {
        return new Response(document.getId(), document.getTitle(), document.getDocumentType(), document.getDescription(),
                document.getVisibility(), document.getOriginalName(), document.getContentType(), document.getSizeBytes(),
                document.getOwner().getId(), document.getOwner().getFullName(),
                document.getDestinations().stream().map(Employee::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                document.getSourceType(), document.getSourceId(), document.getSourceUrl(),
                document.getSignatureStatus(), document.getSignedAt(), document.getCreatedAt(), document.getUpdatedAt());
    }

    private String safeFilename(String name) {
        if (name == null || name.isBlank()) return "documento";
        return name.replace('\\', '/').substring(name.replace('\\', '/').lastIndexOf('/') + 1).replaceAll("[^a-zA-Z0-9._ -]", "_");
    }

    private String normalizeContentType(String value) {
        return value == null || value.isBlank() ? "application/octet-stream" : value.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private String slug(String value) {
        String result = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return result.isBlank() ? "documento" : result;
    }
}

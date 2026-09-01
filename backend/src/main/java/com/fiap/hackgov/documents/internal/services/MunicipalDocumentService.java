package com.fiap.hackgov.documents.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.ForwardRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.GeneratedRequest;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.Response;
import com.fiap.hackgov.documents.internal.DTOs.DocumentDTOs.SignatureRequest;
import com.fiap.hackgov.documents.internal.entities.MunicipalDocument;
import com.fiap.hackgov.documents.internal.repositories.MunicipalDocumentRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final SectorRepository sectorRepository;
    private final OccupationRepository occupationRepository;

    @Transactional(readOnly = true)
    public List<Response> list(String query, String type, Employee employee) {
        return list(query, type, null, null, null, null, null, null, employee);
    }

    @Transactional(readOnly = true)
    public List<Response> list(String query, String type, String number, Integer year,
                               LocalDate dateStart, LocalDate dateEnd, String related, String tags,
                               Employee employee) {
        Employee current = require(employee);
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String relation = related == null ? "" : related.trim().toLowerCase(Locale.ROOT);
        String tagQuery = tags == null ? "" : tags.trim().toLowerCase(Locale.ROOT);
        return repository.findDistinctByCityHall_IdOrderByCreatedAtDesc(cityId(current)).stream()
                .filter(document -> canView(document, current))
                .filter(document -> type == null || type.isBlank() || document.getDocumentType().equalsIgnoreCase(type))
                .filter(document -> q.isBlank() || document.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || document.getDescription().toLowerCase(Locale.ROOT).contains(q)
                        || document.getOriginalName().toLowerCase(Locale.ROOT).contains(q)
                        || value(document.getKeywords()).contains(q) || value(document.getTags()).contains(q))
                .filter(document -> number == null || number.isBlank() || value(document.getNumber()).contains(number.trim().toLowerCase(Locale.ROOT)))
                .filter(document -> year == null || year.equals(document.getYear()))
                .filter(document -> dateStart == null || (document.getDocumentDate() != null && !document.getDocumentDate().isBefore(dateStart)))
                .filter(document -> dateEnd == null || (document.getDocumentDate() != null && !document.getDocumentDate().isAfter(dateEnd)))
                .filter(document -> relation.isBlank() || related(document, relation))
                .filter(document -> tagQuery.isBlank() || value(document.getTags()).contains(tagQuery))
                .map(this::toResponse).toList();
    }

    @Transactional
    public Response upload(String title, String documentType, String description,
                           MunicipalDocument.Visibility visibility, Set<UUID> destinationIds,
                           MultipartFile file, Employee employee) {
        return upload(title, documentType, description, visibility, destinationIds,
                null, null, null, null, null, null, MunicipalDocument.Kind.SECTOR_FILE, file, employee);
    }

    @Transactional
    public Response upload(String title, String documentType, String description,
                           MunicipalDocument.Visibility visibility, Set<UUID> destinationIds,
                           String number, Integer year, LocalDate documentDate, String purpose,
                           String keywords, String tags, MunicipalDocument.Kind kind,
                           MultipartFile file, Employee employee) {
        validateFile(file);
        if (kind == MunicipalDocument.Kind.SEND && !"application/pdf".equals(normalizeContentType(file.getContentType()))) {
            throw new BusinessException("Documentos enviados devem estar em PDF");
        }
        try {
            return save(title, documentType, description, visibility, destinationIds,
                    number, year, documentDate, purpose, keywords, tags, kind,
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
        return createGenerated(request, employee, null);
    }

    @Transactional
    public Response createGenerated(GeneratedRequest request, Employee employee, MultipartFile attachment) {
        Employee current = require(employee);
        Set<UUID> destinationIds = generatedDestinationIds(request, current);
        if (destinationIds.isEmpty()) {
            throw new BusinessException("Informe ao menos um destinatario");
        }
        byte[] content;
        String filename;
        String contentType;
        if (attachment == null || attachment.isEmpty()) {
            content = pdf(request.content());
            filename = slug(request.title()) + ".pdf";
            contentType = "application/pdf";
        } else {
            validateFile(attachment);
            try {
                content = attachment.getBytes();
            } catch (IOException exception) {
                throw new BusinessException("Nao foi possivel ler o anexo enviado");
            }
            filename = safeFilename(attachment.getOriginalFilename());
            contentType = normalizeContentType(attachment.getContentType());
        }
        Response response = save(request.title(), request.documentType(), request.description(), request.visibility(),
                destinationIds, request.number(), request.year(), request.documentDate(), request.purpose(),
                request.keywords(), request.tags(), request.kind() == null ? MunicipalDocument.Kind.SECTOR_FILE : request.kind(),
                filename, contentType, content, current);
        MunicipalDocument document = repository.findById(response.id()).orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado"));
        document.setStructuredContent(request.structuredContent() == null ? request.content() : request.structuredContent());
        if (Boolean.TRUE.equals(request.additionalAccess())) {
            document.setRelatedSectors(relatedSectors(request.relatedSectorIds(), current));
            document.setRelatedEmployees(destinations(request.relatedEmployeeIds(), current));
            document.setRelatedOccupations(relatedOccupations(request.relatedOccupationIds(), current));
        }
        if (document.getKind() == MunicipalDocument.Kind.SECTOR_FILE) document.setSignatureStatus(MunicipalDocument.SignatureStatus.PENDING);
        return toResponse(repository.save(document));
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
        return toResponse(repository.save(copy(document, destinations(request.destinationIds(), current), current)));
    }

    @Transactional
    public Response signHomologation(UUID id, SignatureRequest request, Employee employee) {
        Employee current = require(employee);
        MunicipalDocument document = scoped(id, current);
        requireOwnerOrAdmin(document, current);
        if (!request.consentimento()) {
            throw new BusinessException("Confirme que revisou o documento e deseja assiná-lo eletronicamente");
        }
        if (document.getSignatureStatus() != MunicipalDocument.SignatureStatus.PENDING) {
            throw new BusinessException("Este documento nao esta aguardando assinatura");
        }
        if (!"application/pdf".equals(document.getContentType())) {
            throw new BusinessException("A assinatura de homologacao aceita apenas documentos PDF");
        }
        byte[] homologation = homologationPdf(document.getContent());
        LocalDateTime signedAt = LocalDateTime.now();
        document.setSignatureStatus(MunicipalDocument.SignatureStatus.HOMOLOGATION);
        document.setSignedBy(current);
        document.setSignedAt(signedAt);
        document.setContent(homologation);
        document.setSizeBytes(homologation.length);
        document.setOriginalName(slug(document.getTitle()) + "-homologacao.pdf");
        document.setSignatureCode(UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT));
        document.setSignatureHash(sha256(homologation));
        document.setSignatureStandard("SIMULAÇÃO REMOTA");
        document.setSignatureHolder(current.getFullName());
        document.setSignatureIssuer("Provedor remoto simulado");
        document.setSignatureProvider("Provedor remoto simulado");
        document.setSignatureEnvironment("HOMOLOGAÇÃO");
        document.setSignatureExternalReference("HML-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(Locale.ROOT));
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
                          String number, Integer year, LocalDate documentDate, String purpose,
                          String keywords, String tags, MunicipalDocument.Kind kind,
                          String filename, String contentType, byte[] content, Employee employee) {
        Employee current = require(employee);
        if (title == null || title.isBlank()) throw new BusinessException("O titulo e obrigatorio");
        if (kind == MunicipalDocument.Kind.SEND && (destinationIds == null || destinationIds.isEmpty())) {
            throw new BusinessException("Informe ao menos um destinatario");
        }
        MunicipalDocument document = new MunicipalDocument();
        document.setCityHall(current.getCityHallId());
        document.setOwner(current);
        document.setSector(current.getSectorId());
        document.setTitle(title.trim());
        document.setDocumentType(documentType == null || documentType.isBlank() ? "OTHER" : documentType.trim().toUpperCase(Locale.ROOT));
        document.setKind(kind == null ? MunicipalDocument.Kind.SEND : kind);
        document.setNumber(blankToNull(number)); document.setYear(year); document.setDocumentDate(documentDate);
        document.setPurpose(blankToNull(purpose)); document.setKeywords(keywords == null ? "" : keywords.trim());
        document.setTags(tags == null ? "" : tags.trim());
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
        document.setSignatureStatus(document.getKind() == MunicipalDocument.Kind.SEND
                ? MunicipalDocument.SignatureStatus.PENDING : MunicipalDocument.SignatureStatus.NONE);
        return toResponse(repository.save(document));
    }

    private MunicipalDocument copy(MunicipalDocument source, Set<Employee> destinations, Employee current) {
        MunicipalDocument copy = new MunicipalDocument();
        copy.setCityHall(source.getCityHall()); copy.setOwner(current); copy.setSector(current.getSectorId());
        copy.setTitle(source.getTitle()); copy.setDocumentType(source.getDocumentType()); copy.setKind(source.getKind());
        copy.setNumber(source.getNumber()); copy.setYear(source.getYear()); copy.setDocumentDate(source.getDocumentDate());
        copy.setPurpose(source.getPurpose()); copy.setKeywords(source.getKeywords()); copy.setTags(source.getTags());
        copy.setStructuredContent(source.getStructuredContent()); copy.setDescription(source.getDescription());
        copy.setVisibility(source.getVisibility()); copy.setOriginalName(source.getOriginalName());
        copy.setContentType(source.getContentType()); copy.setContent(source.getContent()); copy.setSizeBytes(source.getSizeBytes());
        copy.setSourceDocument(source); copy.setDestinations(destinations);
        copy.setSignatureStatus("application/pdf".equals(source.getContentType()) ? MunicipalDocument.SignatureStatus.PENDING : MunicipalDocument.SignatureStatus.NONE);
        return copy;
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

    private Set<UUID> generatedDestinationIds(GeneratedRequest request, Employee current) {
        String mode = request.destinationMode() == null ? "pessoas" : request.destinationMode();
        UUID city = cityId(current);
        List<Employee> people = employeeRepository.findAllByCityHallId_IdAndStatusTrueOrderByFirstNameAscLastNameAsc(city);
        if ("todos".equals(mode)) return people.stream().map(Employee::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if ("setores".equals(mode)) {
            Set<UUID> ids = request.sectorDestinationIds() == null ? Set.of() : request.sectorDestinationIds();
            ids.forEach(id -> sectorRepository.findByIdAndCityHall_Id(id, city).orElseThrow(() -> new BusinessException("Setor invalido para a prefeitura ativa")));
            return people.stream().filter(item -> item.getSectorId() != null && ids.contains(item.getSectorId().getId())).map(Employee::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
        if ("cargos".equals(mode)) {
            Set<UUID> ids = request.occupationDestinationIds() == null ? Set.of() : request.occupationDestinationIds();
            ids.forEach(id -> occupationRepository.findById(id).filter(item -> item.getSectorId() != null && item.getSectorId().getCityHall() != null && city.equals(item.getSectorId().getCityHall().getId())).orElseThrow(() -> new BusinessException("Cargo invalido para a prefeitura ativa")));
            return people.stream().filter(item -> item.getOccupationId() != null && ids.contains(item.getOccupationId().getId())).map(Employee::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
        return request.destinationIds() == null ? Set.of() : request.destinationIds();
    }

    private Set<Sector> relatedSectors(Set<UUID> ids, Employee current) {
        if (ids == null) return new LinkedHashSet<>();
        return ids.stream().map(id -> sectorRepository.findByIdAndCityHall_Id(id, cityId(current)).orElseThrow(() -> new BusinessException("Setor invalido para a prefeitura ativa"))).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Occupation> relatedOccupations(Set<UUID> ids, Employee current) {
        if (ids == null) return new LinkedHashSet<>();
        return ids.stream().map(id -> occupationRepository.findById(id).filter(item -> item.getSectorId() != null && item.getSectorId().getCityHall() != null && cityId(current).equals(item.getSectorId().getCityHall().getId())).orElseThrow(() -> new BusinessException("Cargo invalido para a prefeitura ativa"))).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean canView(MunicipalDocument document, Employee employee) {
        if (Roles.ADMIN.equals(employee.getRole()) || document.getOwner().getId().equals(employee.getId())) return true;
        if (document.getSignatureStatus() == MunicipalDocument.SignatureStatus.PENDING) return false;
        if (document.getDestinations().stream().anyMatch(item -> item.getId().equals(employee.getId()))) return true;
        if (document.getRelatedEmployees().stream().anyMatch(item -> item.getId().equals(employee.getId()))) return true;
        if (employee.getSectorId() != null && document.getRelatedSectors().stream().anyMatch(item -> item.getId().equals(employee.getSectorId().getId()))) return true;
        if (employee.getOccupationId() != null && document.getRelatedOccupations().stream().anyMatch(item -> item.getId().equals(employee.getOccupationId().getId()))) return true;
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
        String sectorName = document.getSector() != null && Hibernate.isInitialized(document.getSector())
                ? document.getSector().getName() : null;
        String cityHallName = document.getCityHall() != null && Hibernate.isInitialized(document.getCityHall())
                ? document.getCityHall().getName() : null;
        return new Response(document.getId(), document.getTitle(), document.getDocumentType(), document.getDescription(),
                document.getVisibility(), document.getOriginalName(), document.getContentType(), document.getSizeBytes(),
                document.getOwner().getId(), document.getOwner().getFullName(),
                document.getDestinations().stream().map(Employee::getId).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                document.getSourceType(), document.getSourceId(), document.getSourceUrl(),
                document.getSignatureStatus(), document.getSignedAt(),
                document.getSignedBy() == null ? null : document.getSignedBy().getFullName(), document.getSignatureCode(),
                document.getSignatureHash(), document.getSignatureStandard(), document.getSignatureHolder(),
                document.getSignatureIssuer(), document.getSignatureCertificateSerial(),
                document.getSignatureCertificateFingerprint(), document.getSignatureCertificateValidFrom(),
                document.getSignatureCertificateValidUntil(), document.isSignatureTimestampIncluded(),
                document.getSignatureProvider(), document.getSignatureEnvironment(), document.getSignatureExternalReference(),
                document.getCreatedAt(), document.getUpdatedAt(),
                document.getKind(), document.getNumber(), document.getYear(), document.getDocumentDate(), document.getPurpose(),
                document.getKeywords(), document.getTags(), document.getStructuredContent(),
                document.getSector() == null ? null : document.getSector().getId(),
                sectorName,
                document.getSourceDocument() == null ? null : document.getSourceDocument().getId(),
                document.getRelatedSectors().stream().map(Sector::getName).toList(),
                document.getRelatedEmployees().stream().map(Employee::getFullName).toList(),
                document.getRelatedOccupations().stream().map(Occupation::getName).toList(),
                cityHallName);
    }

    private boolean related(MunicipalDocument document, String query) {
        return value(document.getOwner().getFullName()).contains(query)
                || value(document.getSector() == null ? null : document.getSector().getName()).contains(query)
                || document.getDestinations().stream().anyMatch(item -> value(item.getFullName()).contains(query));
    }

    private String value(String item) { return item == null ? "" : item.toLowerCase(Locale.ROOT); }
    private String blankToNull(String item) { return item == null || item.isBlank() ? null : item.trim(); }

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

    private byte[] homologationPdf(byte[] original) {
        if (original == null || original.length < 5 || !new String(original, 0, 5, StandardCharsets.ISO_8859_1).equals("%PDF-")) {
            throw new BusinessException("O arquivo informado nao e um PDF valido");
        }
        try (PDDocument pdf = Loader.loadPDF(original); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (pdf.getNumberOfPages() == 0) throw new BusinessException("O PDF nao possui paginas");
            PDPage page = pdf.getPage(0);
            try (PDPageContentStream stamp = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                stamp.setNonStrokingColor(1f, 245f / 255f, 245f / 255f);
                stamp.addRect(45, 35, 330, 48);
                stamp.fill();
                stamp.setStrokingColor(190f / 255f, 45f / 255f, 45f / 255f);
                stamp.setLineWidth(1);
                stamp.addRect(45, 35, 330, 48);
                stamp.stroke();
                stamp.beginText();
                stamp.setNonStrokingColor(145f / 255f, 25f / 255f, 25f / 255f);
                stamp.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
                stamp.newLineAtOffset(55, 65);
                stamp.showText("HOMOLOGACAO - SEM VALIDADE JURIDICA");
                stamp.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 7);
                stamp.newLineAtOffset(0, -14);
                stamp.showText("Fluxo remoto simulado em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                stamp.endText();
            }
            pdf.save(output);
            return output.toByteArray();
        } catch (IOException | IllegalArgumentException exception) {
            throw new BusinessException("Nao foi possivel gerar o PDF de homologacao");
        }
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception exception) {
            throw new BusinessException("Nao foi possivel calcular o hash da assinatura");
        }
    }

    private byte[] pdf(String value) {
        String text = value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\r", "").replace("\n", " ) Tj 0 -16 Td ( ");
        String stream = "BT /F1 10 Tf 48 760 Td (" + text + ") Tj ET";
        String[] objects = {"<< /Type /Catalog /Pages 2 0 R >>", "<< /Type /Pages /Kids [3 0 R] /Count 1 >>", "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>", "<< /Length " + stream.getBytes(StandardCharsets.ISO_8859_1).length + " >>\nstream\n" + stream + "\nendstream", "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"};
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int index = 0; index < objects.length; index++) { offsets.add(pdf.length()); pdf.append(index + 1).append(" 0 obj\n").append(objects[index]).append("\nendobj\n"); }
        int xref = pdf.length(); pdf.append("xref\n0 ").append(objects.length + 1).append("\n0000000000 65535 f \n");
        offsets.forEach(offset -> pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset)));
        pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}

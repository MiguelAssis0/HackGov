package com.fiap.hackgov.audit.internal.services;

import com.fiap.hackgov.audit.internal.DTOs.AuditDtos;
import com.fiap.hackgov.audit.internal.entities.AuditEvent;
import com.fiap.hackgov.audit.internal.repositories.AuditEventRepository;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuditEventService {
    private static final String GENESIS = "0".repeat(64);
    private static final int PAGE_SIZE = 25;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final List<AuditDtos.ActionOption> ACTIONS = List.of(
            new AuditDtos.ActionOption("CREATE", "Criação"), new AuditDtos.ActionOption("UPDATE", "Alteração"),
            new AuditDtos.ActionOption("DELETE", "Exclusão"), new AuditDtos.ActionOption("DOWNLOAD", "Download"),
            new AuditDtos.ActionOption("LOGIN", "Login"), new AuditDtos.ActionOption("LOGOUT", "Logout"),
            new AuditDtos.ActionOption("VIEW", "Visualização"), new AuditDtos.ActionOption("SEND", "Envio"),
            new AuditDtos.ActionOption("CUSTOM", "Personalizado"), new AuditDtos.ActionOption("AUTH_FAILURE", "Falha de autenticação"),
            new AuditDtos.ActionOption("ACCESS_DENIED", "Acesso negado"), new AuditDtos.ActionOption("EXPORT", "Exportação"),
            new AuditDtos.ActionOption("REVEAL", "Revelação de dado sensível"), new AuditDtos.ActionOption("CONFIG_CHANGE", "Alteração de configuração"),
            new AuditDtos.ActionOption("PASSWORD_CHANGE", "Alteração de senha"), new AuditDtos.ActionOption("MFA", "Autenticação multifator"),
            new AuditDtos.ActionOption("SERVICE_AUTH", "Autenticação de serviço")
    );

    private final AuditEventRepository repository;
    private final CityHallRepository cityHallRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void append(Employee employee, String method, String path, int status,
                                    String remoteAddress, String userAgent) {
        if (employee == null || employee.getCityHallId() == null) return;
        UUID cityId = employee.getCityHallId().getId();
        String previous = repository.findTopByCityHallIdOrderByIdDesc(cityId)
                .map(AuditEvent::getEventHash).orElse(GENESIS);
        LocalDateTime occurredAt = LocalDateTime.now().withNano(0);
        AuditEvent event = new AuditEvent();
        event.setCityHallId(cityId);
        event.setActorId(employee.getId());
        event.setActorEmail(employee.getEmail());
        event.setMethod(method);
        event.setPath(path);
        event.setResponseStatus(status);
        event.setRemoteAddress(trim(remoteAddress, 80));
        event.setUserAgent(trim(userAgent, 400));
        event.setPreviousHash(previous);
        event.setCreatedAt(occurredAt);
        event.setEventHash(sha256(canonical(event)));
        repository.save(event);
    }

    @Transactional(readOnly = true)
    public AuditDtos.Page list(String scope, String cityHallId, String query, String type, String module,
                               String action, String user, String start, String end, int page,
                               Employee employee) {
        Employee current = requireAdmin(employee);
        boolean platform = isPlatformAdmin(current);
        String resolvedScope = platform && "global".equalsIgnoreCase(scope) ? "global" : "prefeitura";
        UUID selectedCity = resolveCityHall(current, cityHallId, resolvedScope);
        List<AuditEvent> source = eventsFor(resolvedScope, selectedCity, false);
        Map<UUID, String> cityNames = cityNames(source);
        List<AuditDtos.Row> rows = filter(source, query, type, module, action, user, start, end)
                .map(event -> toRow(event, cityNames.getOrDefault(event.getCityHallId(), "-"), platform)).toList();

        int totalPages = rows.isEmpty() ? 0 : (rows.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int safePage = totalPages == 0 ? 0 : Math.min(Math.max(0, page), totalPages - 1);
        int from = safePage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());
        List<AuditDtos.Row> content = rows.subList(from, to);
        List<AuditDtos.CityHallOption> cityOptions = platform
                ? cityHallRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(city -> new AuditDtos.CityHallOption(city.getId().toString(), city.getName())).toList()
                : List.of();
        return new AuditDtos.Page(content, safePage, PAGE_SIZE, rows.size(), totalPages,
                safePage == 0, totalPages == 0 || safePage >= totalPages - 1, resolvedScope,
                true, platform, ACTIONS, cityOptions);
    }

    @Transactional(readOnly = true)
    public String exportCsv(String scope, String cityHallId, String query, String type, String module,
                            String action, String user, String start, String end, Employee employee) {
        Employee current = requireAdmin(employee);
        boolean platform = isPlatformAdmin(current);
        String resolvedScope = platform && "global".equalsIgnoreCase(scope) ? "global" : "prefeitura";
        UUID selectedCity = resolveCityHall(current, cityHallId, resolvedScope);
        List<AuditEvent> source = eventsFor(resolvedScope, selectedCity, true);
        Map<UUID, String> cityNames = cityNames(source);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("data_hora,usuario,prefeitura,modulo,acao,resultado,objeto,descricao,ip,request_id\n");
        filter(source, query, type, module, action, user, start, end)
                .map(event -> toRow(event, cityNames.getOrDefault(event.getCityHallId(), "-"), platform))
                .map(this::csvRow).forEach(row -> csv.append(row).append('\n'));
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public boolean verify(Employee employee) {
        Employee current = requireAdmin(employee);
        List<AuditEvent> events = repository.findTop500ByCityHallIdOrderByIdDesc(current.getCityHallId().getId());
        for (int index = 0; index < events.size(); index++) {
            AuditEvent event = events.get(index);
            if (!event.getEventHash().equals(sha256(canonical(event)))) return false;
            if (index < events.size() - 1 && !event.getPreviousHash().equals(events.get(index + 1).getEventHash())) return false;
        }
        return true;
    }

    private List<AuditEvent> eventsFor(String scope, UUID cityId, boolean export) {
        if ("global".equals(scope) && cityId == null) return export ? repository.findAllByOrderByIdDesc() : repository.findTop500ByOrderByIdDesc();
        return export ? repository.findAllByCityHallIdOrderByIdDesc(cityId) : repository.findTop500ByCityHallIdOrderByIdDesc(cityId);
    }

    private Stream<AuditEvent> filter(List<AuditEvent> events, String query, String type, String module,
                                      String action, String user, String start, String end) {
        String q = lower(query), selectedType = lower(type), selectedModule = lower(module);
        String selectedAction = lower(action), selectedUser = lower(user);
        LocalDate startDate = parseDate(start), endDate = parseDate(end);
        return events.stream().filter(event -> {
            AuditDtos.Row row = toRow(event, "", true);
            if (!q.isBlank() && !String.join(" ", row.descricao(), row.modulo(), row.objeto(), row.usuario()).toLowerCase(Locale.ROOT).contains(q)) return false;
            if ("manual".equals(selectedType)) return false;
            if (!selectedType.isBlank() && !"todos".equals(selectedType) && !"automatico".equals(selectedType)) return false;
            if (!selectedModule.isBlank() && !row.modulo().toLowerCase(Locale.ROOT).contains(selectedModule)) return false;
            if (!selectedAction.isBlank() && !row.acao().equalsIgnoreCase(selectedAction)) return false;
            if (!selectedUser.isBlank() && !row.usuario().toLowerCase(Locale.ROOT).contains(selectedUser)) return false;
            LocalDate date = event.getCreatedAt().toLocalDate();
            return (startDate == null || !date.isBefore(startDate)) && (endDate == null || !date.isAfter(endDate));
        });
    }

    private Map<UUID, String> cityNames(List<AuditEvent> events) {
        Set<UUID> ids = events.stream().map(AuditEvent::getCityHallId).collect(Collectors.toCollection(HashSet::new));
        return cityHallRepository.findAllById(ids).stream().collect(Collectors.toMap(CityHall::getId, CityHall::getName));
    }

    private AuditDtos.Row toRow(AuditEvent event, String cityName, boolean sensitive) {
        String email = event.getActorEmail() == null ? "-" : event.getActorEmail();
        String masked = maskEmail(email), path = event.getPath() == null ? "-" : event.getPath();
        String date = event.getCreatedAt() == null ? "-" : event.getCreatedAt().format(DATE_TIME);
        return new AuditDtos.Row(event.getId(), date, sensitive ? email : masked, masked, cityName,
                module(path), action(event.getMethod()), event.getResponseStatus() < 400 ? "sucesso" : "erro",
                path, "Requisição " + event.getMethod() + " " + path,
                sensitive ? value(event.getRemoteAddress()) : "restrito", "automatico", "-", event.getEventHash());
    }

    private String csvRow(AuditDtos.Row row) {
        return Stream.of(row.dataHora(), row.usuario(), row.prefeitura(), row.modulo(), row.acao(), row.resultado(),
                row.objeto(), row.descricao(), row.ip(), row.requestId()).map(this::csv).collect(Collectors.joining(","));
    }

    private String csv(String value) {
        return '"' + (value == null ? "" : value.replace("\"", "\"\"")) + '"';
    }

    private UUID resolveCityHall(Employee employee, String cityHallId, String scope) {
        if (isPlatformAdmin(employee) && cityHallId != null && !cityHallId.isBlank()) {
            try {
                UUID requested = UUID.fromString(cityHallId);
                cityHallRepository.findById(requested).orElseThrow(() -> new BusinessException("Prefeitura não encontrada"));
                return requested;
            } catch (IllegalArgumentException exception) {
                throw new BusinessException("Prefeitura inválida");
            }
        }
        if ("global".equals(scope)) return null;
        if (employee.getCityHallId() == null) throw new BusinessException("O usuário precisa estar vinculado a uma prefeitura");
        return employee.getCityHallId().getId();
    }

    private Employee requireAdmin(Employee employee) {
        if (employee == null) throw new UnauthorizedException("É necessário estar autenticado");
        if (employee.getCityHallId() == null) throw new BusinessException("O usuário precisa estar vinculado a uma prefeitura");
        if (!Roles.ADMIN.equals(employee.getRole())) throw new BusinessException("Somente administradores podem consultar a auditoria");
        return employee;
    }

    private boolean isPlatformAdmin(Employee employee) {
        return "admin@admin.com".equalsIgnoreCase(employee.getEmail());
    }

    private String action(String method) {
        return switch (String.valueOf(method).toUpperCase(Locale.ROOT)) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "VIEW";
        };
    }

    private String module(String path) {
        if (path == null || path.isBlank()) return "-";
        String[] parts = path.split("/");
        return parts.length > 2 && !parts[2].isBlank() ? parts[2] : "-";
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "-";
        int at = email.indexOf('@');
        return (at <= 1 ? "*" : email.substring(0, 1) + "***") + email.substring(at);
    }

    private String lower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }

    private String canonical(AuditEvent event) {
        return String.join("|", event.getPreviousHash(), event.getCityHallId().toString(), event.getActorId().toString(),
                event.getMethod(), event.getPath(), Integer.toString(event.getResponseStatus()), event.getCreatedAt().toString());
    }

    private String trim(String value, int limit) {
        if (value == null) return "";
        return value.length() <= limit ? value : value.substring(0, limit);
    }
}

package com.fiap.hackgov.tools.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tools.internal.entities.ToolConfiguration;
import com.fiap.hackgov.tools.internal.entities.ToolPermissionRule;
import com.fiap.hackgov.tools.internal.repositories.ToolConfigurationRepository;
import com.fiap.hackgov.tools.internal.repositories.ToolPermissionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToolPermissionService {
    private final ToolPermissionRuleRepository repository;
    private final ToolConfigurationRepository toolRepository;
    private final SectorRepository sectorRepository;
    private final OccupationRepository occupationRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<Response> list(Employee employee) {
        Employee current = admin(employee);
        return repository.findByCityHall_IdOrderByToolSlugAsc(city(current)).stream().map(this::response).toList();
    }

    @Transactional
    public Response create(Create dto, Employee employee) {
        Employee current = admin(employee);
        if (Set.of("setores", "cargos", "controle-acesso").contains(dto.toolSlug()))
            throw new BusinessException("Esta ferramenta possui acesso administrativo fixo");
        toolRepository.findByCityHall_IdAndSlug(city(current), dto.toolSlug()).orElseThrow(() -> new BusinessException("Ferramenta invalida"));
        if (dto.employeeId() != null && (dto.sectorId() != null || dto.occupationId() != null))
            throw new BusinessException("Selecione um usuario ou um grupo por setor/cargo, nao ambos");
        ToolPermissionRule rule = repository.findByCityHall_IdAndToolSlug(city(current), dto.toolSlug()).stream()
                .filter(item -> Objects.equals(item.getEmployee() == null ? null : item.getEmployee().getId(), dto.employeeId()))
                .filter(item -> Objects.equals(item.getSector() == null ? null : item.getSector().getId(), dto.sectorId()))
                .filter(item -> Objects.equals(item.getOccupation() == null ? null : item.getOccupation().getId(), dto.occupationId()))
                .findFirst().orElseGet(ToolPermissionRule::new);
        rule.setCityHall(current.getCityHallId());
        rule.setToolSlug(dto.toolSlug());
        rule.setLevel(dto.level() == null ? ToolPermissionRule.Level.VIEW : dto.level());
        rule.setEnabled(dto.enabled() == null || dto.enabled());
        if (dto.sectorId() != null)
            rule.setSector(sectorRepository.findByIdAndCityHall_Id(dto.sectorId(), city(current)).orElseThrow(() -> new BusinessException("Setor invalido")));
        if (dto.occupationId() != null)
            rule.setOccupation(occupationRepository.findById(dto.occupationId()).filter(item -> item.getSectorId() != null && item.getSectorId().getCityHall().getId().equals(city(current))).orElseThrow(() -> new BusinessException("Cargo invalido")));
        if (rule.getSector() != null && rule.getOccupation() != null && !rule.getOccupation().getSectorId().getId().equals(rule.getSector().getId()))
            throw new BusinessException("O cargo precisa pertencer ao setor selecionado");
        if (dto.employeeId() != null)
            rule.setEmployee(employeeRepository.findByIdWithDetails(dto.employeeId()).filter(item -> item.getCityHallId() != null && item.getCityHallId().getId().equals(city(current))).orElseThrow(() -> new BusinessException("Funcionario invalido")));
        ToolPermissionRule.DataScope scope = "relatorios".equals(dto.toolSlug()) && dto.dataScope() != null
                ? dto.dataScope() : ToolPermissionRule.DataScope.ALL_SECTORS;
        Set<UUID> visibleIds = dto.visibleSectorIds() == null ? Set.of() : dto.visibleSectorIds();
        if (scope == ToolPermissionRule.DataScope.SELECTED_SECTORS && visibleIds.isEmpty())
            throw new BusinessException("Selecione ao menos um setor para o escopo restrito");
        rule.setDataScope(scope);
        rule.getVisibleSectors().clear();
        for (UUID sectorId : visibleIds)
            rule.getVisibleSectors().add(sectorRepository.findByIdAndCityHall_Id(sectorId, city(current)).orElseThrow(() -> new BusinessException("Setor invalido")));
        return response(repository.save(rule));
    }

    @Transactional
    public void delete(UUID id, Employee employee) {
        Employee current = admin(employee);
        repository.delete(repository.findByIdAndCityHall_Id(id, city(current)).orElseThrow(() -> new ResourceNotFoundException("Permissao nao encontrada")));
    }

    public boolean canAccess(String slug, Employee employee) {
        List<ToolPermissionRule> rules = repository.findByCityHall_IdAndToolSlug(city(employee), slug);
        List<ToolPermissionRule> personal = rules.stream().filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(employee.getId())).toList();
        if (!personal.isEmpty()) return personal.stream().anyMatch(ToolPermissionRule::isEnabled);
        return rules.stream().filter(r -> r.getEmployee() == null).sorted(Comparator.comparingInt(this::specificity).reversed()).filter(r -> matches(r, employee)).findFirst().map(ToolPermissionRule::isEnabled).orElse(false);
    }

    public boolean canManage(String slug, Employee employee) {
        List<ToolPermissionRule> rules = repository.findByCityHall_IdAndToolSlug(city(employee), slug);
        List<ToolPermissionRule> personal = rules.stream().filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(employee.getId())).toList();
        if (!personal.isEmpty()) return personal.stream().anyMatch(r -> r.isEnabled() && r.getLevel() != ToolPermissionRule.Level.VIEW);
        return rules.stream().filter(r -> r.getEmployee() == null && matches(r, employee))
                .max(Comparator.comparingInt(this::specificity).thenComparingInt(r -> levelRank(r.getLevel())))
                .map(r -> r.isEnabled() && r.getLevel() != ToolPermissionRule.Level.VIEW).orElse(false);
    }

    private int levelRank(ToolPermissionRule.Level level) {
        return level == ToolPermissionRule.Level.ADMIN ? 2 : level == ToolPermissionRule.Level.MANAGE ? 1 : 0;
    }

    private int specificity(ToolPermissionRule r) {
        return (r.getSector() != null ? 1 : 0) + (r.getOccupation() != null ? 1 : 0);
    }

    private boolean matches(ToolPermissionRule r, Employee e) {
        return (r.getSector() == null || e.getSectorId() != null && r.getSector().getId().equals(e.getSectorId().getId())) && (r.getOccupation() == null || e.getOccupationId() != null && r.getOccupation().getId().equals(e.getOccupationId().getId()));
    }

    private Employee admin(Employee e) {
        if (e == null) throw new UnauthorizedException("E necessario estar autenticado");
        if (!Roles.ADMIN.equals(e.getRole()))
            throw new UnauthorizedException("Somente administradores podem configurar permissoes");
        city(e);
        return e;
    }

    private UUID city(Employee e) {
        if (e.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        return e.getCityHallId().getId();
    }

    private Response response(ToolPermissionRule r) {
        boolean restricted = toolRepository.findByCityHall_IdAndSlug(r.getCityHall().getId(), r.getToolSlug())
                .map(ToolConfiguration::isRestricted).orElse(false);
        List<String> visibleSectorNames = r.getVisibleSectors().stream().map(Sector::getName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        return new Response(r.getId(), r.getToolSlug(), r.getEmployee() == null ? null : r.getEmployee().getId(), r.getEmployee() == null ? null : r.getEmployee().getFullName(), r.getSector() == null ? null : r.getSector().getId(), r.getSector() == null ? null : r.getSector().getName(), r.getEmployee() == null ? null : r.getEmployee().getEmail(), r.getOccupation() == null ? null : r.getOccupation().getId(), r.getOccupation() == null ? null : r.getOccupation().getName(), r.getLevel(), r.isEnabled(), restricted, r.getDataScope(), visibleSectorNames);
    }

    public record Create(String toolSlug, UUID employeeId, UUID sectorId, UUID occupationId,
                         ToolPermissionRule.Level level, Boolean enabled, ToolPermissionRule.DataScope dataScope,
                         Set<UUID> visibleSectorIds) {
    }

    public record Response(UUID id, String toolSlug, UUID employeeId, String employeeName, UUID sectorId,
                           String sectorName, String employeeEmail, UUID occupationId, String occupationName,
                           ToolPermissionRule.Level level, boolean enabled, boolean accessRestricted,
                           ToolPermissionRule.DataScope dataScope, List<String> visibleSectorNames) {
    }
}

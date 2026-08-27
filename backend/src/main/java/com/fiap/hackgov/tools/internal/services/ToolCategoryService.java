package com.fiap.hackgov.tools.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tools.internal.entities.ToolCategory;
import com.fiap.hackgov.tools.internal.repositories.ToolCategoryRepository;
import com.fiap.hackgov.tools.internal.repositories.ToolConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToolCategoryService {
    private static final int MAX_NAME = 120;
    private final ToolCategoryRepository repository;
    private final ToolConfigurationRepository toolRepository;

    @Transactional(readOnly = true)
    public List<Response> list(Employee employee) {
        Employee current = require(employee);
        return repository.findByCityHall_IdOrderByOrderAscNameAsc(city(current)).stream()
                .map(this::response).toList();
    }

    @Transactional
    public Response create(Request request, Employee employee) {
        Employee current = admin(employee);
        String name = validName(request.name());
        String slug = slug(name);
        if (repository.existsByCityHall_IdAndSlug(city(current), slug)) {
            throw new BusinessException("Ja existe uma pasta com esse nome nesta prefeitura");
        }
        ToolCategory category = new ToolCategory();
        category.setCityHall(current.getCityHallId());
        apply(category, request, name, slug);
        return response(repository.save(category));
    }

    @Transactional
    public Response update(UUID id, Request request, Employee employee) {
        Employee current = admin(employee);
        ToolCategory category = find(id, current);
        String name = validName(request.name());
        String slug = slug(name);
        if (repository.existsByCityHall_IdAndSlugAndIdNot(city(current), slug, id)) {
            throw new BusinessException("Ja existe uma pasta com esse nome nesta prefeitura");
        }
        apply(category, request, name, slug);
        return response(repository.save(category));
    }

    @Transactional
    public void delete(UUID id, Employee employee) {
        Employee current = admin(employee);
        ToolCategory category = find(id, current);
        toolRepository.findByCustomCategory_Id(category.getId()).forEach(tool -> tool.setCustomCategory(null));
        toolRepository.flush();
        repository.delete(category);
    }

    private void apply(ToolCategory category, Request request, String name, String slug) {
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(request.description() == null ? "" : request.description().trim());
        category.setIcon(request.icon() == null || request.icon().isBlank() ? "bi-folder-fill" : request.icon().trim());
        category.setOrder(Math.max(0, request.order()));
        category.setActive(request.active());
    }

    private String validName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.isBlank()) throw new BusinessException("Informe o nome da pasta");
        if (name.length() > MAX_NAME) throw new BusinessException("O nome da pasta deve ter no maximo 120 caracteres");
        return name;
    }

    private String slug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.substring(0, Math.min(80, normalized.length()));
    }

    private ToolCategory find(UUID id, Employee employee) {
        return repository.findByIdAndCityHall_Id(id, city(employee))
                .orElseThrow(() -> new ResourceNotFoundException("Pasta nao encontrada"));
    }

    private Employee require(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        city(employee);
        return employee;
    }

    private Employee admin(Employee employee) {
        Employee current = require(employee);
        if (!Roles.ADMIN.equals(current.getRole())) throw new UnauthorizedException("Somente administradores podem configurar pastas");
        return current;
    }

    private UUID city(Employee employee) {
        if (employee.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        return employee.getCityHallId().getId();
    }

    private Response response(ToolCategory category) {
        return new Response(category.getId(), category.getName(), category.getSlug(), category.getDescription(),
                category.getIcon(), category.getOrder(), category.isActive(),
                toolRepository.countByCustomCategory_Id(category.getId()));
    }

    public record Request(String name, String description, String icon, int order, boolean active) {}
    public record Response(UUID id, String name, String slug, String description, String icon, int order,
                           boolean active, long total) {}
}

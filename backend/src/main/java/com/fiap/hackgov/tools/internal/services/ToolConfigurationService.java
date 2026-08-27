package com.fiap.hackgov.tools.internal.services;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.exceptions.BusinessException;
import com.fiap.hackgov.shared.infra.exceptions.ResourceNotFoundException;
import com.fiap.hackgov.shared.infra.exceptions.UnauthorizedException;
import com.fiap.hackgov.tools.internal.entities.ToolCategory;
import com.fiap.hackgov.tools.internal.entities.ToolConfiguration;
import com.fiap.hackgov.tools.internal.entities.UserToolFavorite;
import com.fiap.hackgov.tools.internal.repositories.ToolCategoryRepository;
import com.fiap.hackgov.tools.internal.repositories.ToolConfigurationRepository;
import com.fiap.hackgov.tools.internal.repositories.UserToolFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToolConfigurationService {
    private static final Set<String> ADMIN_ONLY = Set.of("setores", "cargos", "controle-acesso");
    private static final Set<String> FIXED_VISIBLE = Set.of(
            "caixa-entrada", "documentos", "compras-licitacoes", "clientes-gerais", "patrulha-agricola"
    );
    private final ToolConfigurationRepository repository;
    private final ToolCategoryRepository categoryRepository;
    private final UserToolFavoriteRepository favoriteRepository;
    private final ToolPermissionService permissionService;

    private static final List<Seed> SEEDS = List.of(
            new Seed("caixa-entrada", "Caixa de Entrada", "Usuarios", "bi-inbox-fill", "Receba documentos, alertas e solicitações por setor ou por funcionário.", "/caixa-entrada", true, true),
            new Seed("funcionarios", "Funcionários", "Usuarios", "bi-people-fill", "Cadastro e gerenciamento de usuários, funcionários, cargos e setores.", "/funcionarios", true, true),
            new Seed("tarefas", "Tarefas", "Gestao", "bi-kanban-fill", "Quadro kanban por setor, delegação de responsáveis e solicitações entre setores.", "/tarefas", false, true),
            new Seed("setores", "Setores", "Gestao", "bi-building-gear", "Cadastro e organização dos setores da prefeitura.", "/setores", true, true),
            new Seed("cargos", "Cargos", "Gestao", "bi-person-badge-fill", "Cadastro e organização dos cargos por setor da prefeitura.", "/cargos", true, true),
            new Seed("compras-licitacoes", "Compras e Licitações", "Processos", "bi-bag-check-fill", "Gerencie processos de compra e licitação com fases configuráveis por prefeitura.", "/processos", true, true),
            new Seed("processos", "Processos", "Processos", "bi-diagram-3-fill", "Acompanhamento de processos administrativos por etapa e setor.", null, false, true),
            new Seed("controle-acesso", "Controle de Acesso", "Usuarios", "bi-shield-lock-fill", "Regras granulares de acesso por setor, cargo, usuário e ferramenta.", "/controle-acesso", true, true),
            new Seed("relatorios", "Relatórios", "Gestao", "bi-bar-chart-fill", "Indicadores e relatórios gerenciais da prefeitura.", "/gestao", false, true),
            new Seed("agenda", "Agenda Municipal", "Gestao", "bi-calendar2-check-fill", "Agenda de prazos, reuniões, eventos e compromissos.", "/agenda", false, true),
            new Seed("importacao-dados", "Importação de Dados", "Dados", "bi-cloud-arrow-up-fill", "Importação de bases legadas e planilhas.", null, false, false),
            new Seed("spreadsheet-import", "Importação de Planilhas", "Dados", "bi-file-earmark-spreadsheet-fill", "Upload, prévia e mapeamento de planilhas CSV ou XLSX antes da importação.", "/importacao", false, true),
            new Seed("backup-exportacao", "Backup e Exportação", "Dados", "bi-file-earmark-arrow-down-fill", "Exportação de dados e cópias de segurança.", null, false, false),
            new Seed("documentos", "Documentos", "Gestao", "bi-file-earmark-text-fill", "Envie e receba arquivos e documentos entre setores ou funcionários.", "/documentos", false, true),
            new Seed("clientes-gerais", "Clientes", "Usuarios", "bi-person-vcard-fill", "Cadastre clientes da prefeitura e acompanhe sua pasta de atendimentos.", "/clientes", false, true),
            new Seed("patrulha-agricola", "Patrulha Agrícola", "Processos", "bi-truck-front-fill", "Gerencie agendamentos, serviços, pagamentos e doações da patrulha agrícola.", "/patrulha-agricola", false, true),
            new Seed("auditoria", "Auditoria", "Sistema", "bi-shield-check", "Consulte logs de auditoria e eventos do sistema por prefeitura.", "/auditoria", false, true)
    );

    @Transactional
    public List<Response> list(Employee employee) {
        Employee current = require(employee);
        ensure(current);
        Set<String> favorites = favoriteRepository.findByEmployee_Id(current.getId()).stream()
                .map(UserToolFavorite::getToolSlug).collect(Collectors.toSet());
        return repository.findByCityHall_IdOrderByCategoryAscNameAsc(city(current)).stream()
                .filter(item -> visible(item, current))
                .map(item -> response(item, favorites.contains(item.getSlug())))
                .toList();
    }

    private boolean visible(ToolConfiguration item, Employee employee) {
        if (Roles.ADMIN.equals(employee.getRole())) return true;
        if (ADMIN_ONLY.contains(item.getSlug()) || (!item.isEnabled() && !item.isMandatory())) return false;
        boolean explicitAccess = permissionService.canAccess(item.getSlug(), employee);
        if (item.isRestricted()) return explicitAccess;
        if ("funcionarios".equals(item.getSlug()) || FIXED_VISIBLE.contains(item.getSlug())) return true;
        if (Set.of("tarefas", "agenda").contains(item.getSlug())) return employee.getSectorId() != null;
        return explicitAccess;
    }

    @Transactional
    public Response update(String slug, Update request, Employee employee) {
        Employee current = admin(employee);
        ensure(current);
        ToolConfiguration item = find(slug, current);
        if (item.isMandatory() && !request.enabled()) {
            throw new BusinessException("Ferramentas obrigatorias nao podem ser desativadas");
        }
        item.setEnabled(item.isMandatory() || request.enabled());
        item.setRestricted(request.restricted());
        return response(repository.save(item), favoriteRepository
                .findByEmployee_IdAndToolSlug(current.getId(), slug).isPresent());
    }

    @Transactional
    public Response updateCategory(String slug, CategoryUpdate request, Employee employee) {
        Employee current = admin(employee);
        ensure(current);
        ToolConfiguration item = find(slug, current);
        ToolCategory category = request.categoryId() == null ? null : categoryRepository
                .findByIdAndCityHall_Id(request.categoryId(), city(current))
                .orElseThrow(() -> new BusinessException("A pasta precisa pertencer a mesma prefeitura"));
        item.setCustomCategory(category);
        return response(repository.save(item), favoriteRepository
                .findByEmployee_IdAndToolSlug(current.getId(), slug).isPresent());
    }

    @Transactional
    public Favorite favorite(String slug, Employee employee) {
        Employee current = require(employee);
        ensure(current);
        ToolConfiguration tool = repository.findByCityHall_IdAndSlug(city(current), slug)
                .filter(item -> item.isEnabled() || item.isMandatory())
                .orElseThrow(() -> new BusinessException("Ferramenta indisponivel"));
        Optional<UserToolFavorite> found = favoriteRepository.findByEmployee_IdAndToolSlug(current.getId(), slug);
        if (found.isPresent()) {
            favoriteRepository.delete(found.get());
            return new Favorite(slug, false);
        }
        UserToolFavorite value = new UserToolFavorite();
        value.setEmployee(current);
        value.setToolSlug(tool.getSlug());
        favoriteRepository.save(value);
        return new Favorite(slug, true);
    }

    private ToolConfiguration find(String slug, Employee employee) {
        return repository.findByCityHall_IdAndSlug(city(employee), slug)
                .orElseThrow(() -> new ResourceNotFoundException("Ferramenta nao encontrada"));
    }

    private void ensure(Employee employee) {
        for (Seed seed : SEEDS) {
            ToolConfiguration item = repository.findByCityHall_IdAndSlug(city(employee), seed.slug)
                    .orElseGet(() -> {
                        ToolConfiguration created = new ToolConfiguration();
                        created.setCityHall(employee.getCityHallId());
                        created.setSlug(seed.slug);
                        created.setEnabled(seed.enabled);
                        return created;
                    });
            item.setSlug(seed.slug);
            item.setName(seed.name);
            item.setCategory(seed.category);
            item.setIcon(seed.icon);
            item.setDescription(seed.description);
            item.setRoute(seed.route);
            item.setMandatory(seed.mandatory);
            if (seed.mandatory) item.setEnabled(true);
            repository.save(item);
        }
    }

    private Employee require(Employee employee) {
        if (employee == null) throw new UnauthorizedException("E necessario estar autenticado");
        city(employee);
        return employee;
    }

    private Employee admin(Employee employee) {
        Employee current = require(employee);
        if (!Roles.ADMIN.equals(current.getRole())) {
            throw new UnauthorizedException("Somente administradores podem configurar ferramentas");
        }
        return current;
    }

    private UUID city(Employee employee) {
        if (employee.getCityHallId() == null) throw new BusinessException("Usuario sem prefeitura");
        return employee.getCityHallId().getId();
    }

    private Response response(ToolConfiguration item, boolean favorite) {
        String route = item.getRoute() == null && "importacao-dados".equals(item.getSlug()) ? "/importacao" : item.getRoute();
        ToolCategory category = item.getCustomCategory();
        return new Response(item.getSlug(), item.getName(), item.getCategory(),
                category == null ? null : category.getId(), item.getIcon(), item.getDescription(), route,
                item.isMandatory(), item.isEnabled(), item.isRestricted(), favorite);
    }

    private record Seed(String slug, String name, String category, String icon, String description, String route,
                        boolean mandatory, boolean enabled) {}
    public record Update(boolean enabled, boolean restricted) {}
    public record CategoryUpdate(UUID categoryId) {}
    public record Favorite(String slug, boolean favorite) {}
    public record Response(String id, String name, String category, UUID categoryId, String icon, String description,
                           String route, boolean mandatory, boolean enabled, boolean restricted, boolean favorite) {}
}

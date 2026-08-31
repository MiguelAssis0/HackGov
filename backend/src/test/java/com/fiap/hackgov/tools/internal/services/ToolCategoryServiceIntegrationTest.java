package com.fiap.hackgov.tools.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class ToolCategoryServiceIntegrationTest {
    @Autowired
    private ToolCategoryService categoryService;
    @Autowired
    private ToolConfigurationService toolService;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void createsAssignsAndDeletesMunicipalFolderWithoutLosingToolConfiguration() {
        Employee admin = employeeRepository.findByEmail("admin@admin.com").orElseThrow();
        ToolCategoryService.Response category = categoryService.create(
                new ToolCategoryService.Request("Secretaria de Saúde", "Rotinas locais", "bi-heart-pulse-fill", 2, true),
                admin
        );

        ToolConfigurationService.Response assigned = toolService.updateCategory(
                "documentos", new ToolConfigurationService.CategoryUpdate(category.id()), admin
        );
        assertThat(assigned.categoryId()).isEqualTo(category.id());
        assertThat(categoryService.list(admin)).anyMatch(item -> item.id().equals(category.id()) && item.total() == 1);

        categoryService.delete(category.id(), admin);

        assertThat(categoryService.list(admin)).noneMatch(item -> item.id().equals(category.id()));
        assertThat(toolService.list(admin)).anyMatch(tool -> tool.id().equals("documentos") && tool.categoryId() == null);
    }

    @Test
    void employeeLauncherExcludesAdministrativeStructuralTools() {
        Employee employee = employeeRepository.findByEmail("joao@sp.gov.br").orElseThrow();

        assertThat(toolService.list(employee))
                .extracting(ToolConfigurationService.Response::id)
                .doesNotContain("setores", "cargos", "controle-acesso")
                .contains("tarefas", "agenda", "documentos", "funcionarios");
    }
}

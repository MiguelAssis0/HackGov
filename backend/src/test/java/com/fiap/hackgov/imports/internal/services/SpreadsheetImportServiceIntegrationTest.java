package com.fiap.hackgov.imports.internal.services;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.imports.internal.DTOs.ImportDTOs.ValidateRequest;
import com.fiap.hackgov.imports.internal.entities.ImportBatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SpreadsheetImportServiceIntegrationTest {
    @Autowired
    SpreadsheetImportService service;
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    SectorRepository sectorRepository;

    @Test
    void previewsValidatesAndImportsCsvDepartment() {
        Employee admin = employeeRepository.findByEmail("admin@admin.com").orElseThrow();
        String sectorName = "Setor Importado Teste";
        var file = new MockMultipartFile("file", "setores.csv", "text/csv",
                ("Nome;Descricao;Ativo\n" + sectorName + ";Criado pela importacao;sim\n")
                        .getBytes(StandardCharsets.UTF_8));

        var preview = service.preview("departments", file, admin);
        assertThat(preview.totalRows()).isEqualTo(1);
        assertThat(preview.suggestedMapping()).containsEntry("nome", "Nome");

        var report = service.validate(preview.id(), new ValidateRequest(
                ImportBatch.Mode.CREATE,
                Map.of("nome", "Nome", "descricao", "Descricao", "ativo", "Ativo")), admin);
        assertThat(report.invalidRows()).isZero();

        var result = service.execute(preview.id(), admin);
        assertThat(result.createdRecords()).isEqualTo(1);
        assertThat(sectorRepository.findByNameAndCityHall_Id(sectorName, admin.getCityHallId().getId()))
                .isPresent().get().extracting("description").isEqualTo("Criado pela importacao");
    }
}

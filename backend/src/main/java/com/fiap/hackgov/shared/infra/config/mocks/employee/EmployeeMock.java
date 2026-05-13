package com.fiap.hackgov.shared.infra.config.mocks.employee;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmployeeMock {
    private final EmployeeRepository repository;
    private final EmployeeFactory factory;

    public void load(MockContext ctx) {
        Employee admin = factory.create("Admin", "Sistema", "admin@admin.com", "123.456.789-00", "SP-001", 5500.0, ctx.analista, ctx.cityHallSP);
        Employee maria = factory.create("Maria", "Oliveira", "maria@sp.gov.br", "987.654.321-00", "SP-002", 12000.0, ctx.gerente, ctx.cityHallSP);
        Employee joao = factory.create("João", "Silva", "joao@sp.gov.br", "555.666.777-88", "SP-003", 4800.0, ctx.analista, ctx.cityHallSP);
        Employee carlos = factory.create("Carlos", "Mendes", "carlos@rj.gov.br", "111.222.333-44", "RJ-001", 3200.0, ctx.assistente, ctx.cityHallRJ);
        repository.saveAll(List.of(admin, maria, joao, carlos));
        ctx.admin = admin;
        ctx.maria = maria;
        ctx.joao = joao;
        ctx.carlos = carlos;
    }
}
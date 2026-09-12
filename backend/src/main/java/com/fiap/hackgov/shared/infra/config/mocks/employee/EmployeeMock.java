package com.fiap.hackgov.shared.infra.config.mocks.employee;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
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
        Employee adminSistema = factory.create("Admin", "Sistema", "admin@admin.com", "443.227.360-76", "SP-ADM-001", 18500.0, ctx.administradorMunicipal, ctx.tiSectorsSP, ctx.cityHallSP, Roles.ADMIN);

        Employee admin = factory.create("Adriana", "Souza", "admin.sp@prefeitura.gov.br", "123.456.789-00", "SP-ADM-001", 18500.0, ctx.administradorMunicipal, ctx.tiSectorsSP, ctx.cityHallSP, Roles.ADMIN);
        Employee geraldo = factory.create("Geraldo", "Pereira", "geraldo.admin@sp.gov.br", "892.036.741-52", "SP-ADM-010", 18500.0, ctx.administradorMunicipal, ctx.tiSectorsSP, ctx.cityHallSP, Roles.ADMIN);

        Employee maria = factory.create("Maria", "Oliveira", "maria@sp.gov.br", "987.654.321-00", "SP-CMP-002", 12000.0, ctx.pregoeiro, ctx.comprasSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee joao = factory.create("João", "Silva", "joao@sp.gov.br", "555.666.777-88", "SP-TI-003", 4800.0, ctx.analista, ctx.tiSectorsSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee ana = factory.create("Ana", "Ribeiro", "ana.compras@sp.gov.br", "214.315.416-17", "SP-CMP-004", 7600.0, ctx.agenteCompras, ctx.comprasSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee roberto = factory.create("Roberto", "Nunes", "roberto.financeiro@sp.gov.br", "318.219.412-11", "SP-FIN-005", 8100.0, ctx.analistaFinanceiro, ctx.financeiroSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee fernanda = factory.create("Fernanda", "Campos", "fernanda.contratos@sp.gov.br", "462.573.684-95", "SP-CTR-006", 9800.0, ctx.gestorContratos, ctx.contratosSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee paula = factory.create("Paula", "Bittencourt", "paula.juridico@sp.gov.br", "596.607.718-29", "SP-JUR-007", 10200.0, ctx.assessorJuridico, ctx.juridicoSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee camila = factory.create("Camila", "Santos", "camila.cultura@sp.gov.br", "673.814.529-30", "SP-CUL-008", 7400.0, ctx.coordenadorCultura, ctx.culturaSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);
        Employee diego = factory.create("Diego", "Ferreira", "diego.educacao@sp.gov.br", "781.925.630-41", "SP-EDU-009", 6900.0, ctx.professor, ctx.educacaoSectorSP, ctx.cityHallSP, Roles.EMPLOYEE);

        Employee carlos = factory.create("Carlos", "Mendes", "carlos@rj.gov.br", "111.222.333-44", "RJ-ADM-001", 17600.0, ctx.administradorMunicipal, ctx.tiSectorRJ, ctx.cityHallRJ, Roles.ADMIN);
        Employee juliana = factory.create("Juliana", "Costa", "juliana.compras@rj.gov.br", "142.253.364-75", "RJ-CMP-002", 11800.0, ctx.pregoeiro, ctx.comprasSectorRJ, ctx.cityHallRJ, Roles.EMPLOYEE);
        Employee bruno = factory.create("Bruno", "Almeida", "bruno.financeiro@rj.gov.br", "275.386.497-08", "RJ-FIN-003", 7900.0, ctx.analistaFinanceiro, ctx.financeiroSectorRJ, ctx.cityHallRJ, Roles.EMPLOYEE);
        Employee patricia = factory.create("Patrícia", "Lemos", "patricia.contratos@rj.gov.br", "326.437.548-19", "RJ-CTR-004", 9400.0, ctx.gestorContratos, ctx.contratosSectorRJ, ctx.cityHallRJ, Roles.EMPLOYEE);
        Employee lucas = factory.create("Lucas", "Freitas", "lucas.juridico@rj.gov.br", "437.548.659-20", "RJ-JUR-005", 9700.0, ctx.assessorJuridico, ctx.juridicoSectorRJ, ctx.cityHallRJ, Roles.EMPLOYEE);
        Employee beatriz = factory.create("Beatriz", "Lima", "beatriz.cultura@rj.gov.br", "548.659.760-31", "RJ-CUL-006", 7100.0, ctx.coordenadorCultura, ctx.culturaSectorRJ, ctx.cityHallRJ, Roles.EMPLOYEE);
        Employee thiago = factory.create("Thiago", "Rocha", "thiago.educacao@rj.gov.br", "659.760.871-42", "RJ-EDU-007", 6700.0, ctx.professor, ctx.educacaoSectorRJ, ctx.cityHallRJ, Roles.EMPLOYEE);

        repository.saveAll(List.of(adminSistema, admin, geraldo, maria, joao, ana, roberto, fernanda, paula, camila, diego, carlos, juliana, bruno, patricia, lucas, beatriz, thiago));
        ctx.adminSistema = adminSistema;
        ctx.admin = admin;
        ctx.geraldo = geraldo;
        ctx.maria = maria;
        ctx.joao = joao;
        ctx.carlos = carlos;
        ctx.ana = ana;
        ctx.roberto = roberto;
        ctx.fernanda = fernanda;
        ctx.paula = paula;
        ctx.camila = camila;
        ctx.diego = diego;
        ctx.juliana = juliana;
        ctx.bruno = bruno;
        ctx.patricia = patricia;
        ctx.lucas = lucas;
        ctx.beatriz = beatriz;
        ctx.thiago = thiago;
    }
}

package com.fiap.hackgov.infra.config;

import com.fiap.hackgov.entities.*;
import com.fiap.hackgov.entities.enums.LevelJobLevel;
import com.fiap.hackgov.entities.enums.Roles;
import com.fiap.hackgov.entities.enums.TypeJobLevel;
import com.fiap.hackgov.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@Profile("dev")
public class Mocks implements CommandLineRunner {

    @Autowired private StateRepository stateRepository;
    @Autowired private CityHallRepository cityHallRepository;
    @Autowired private SectorRepository sectorRepository;
    @Autowired private JobLevelRepository jobLevelRepository;
    @Autowired private JobLevelSectorRepository jobLevelSectorRepository;
    @Autowired private PermissionsRepository permissionsRepository;
    @Autowired private PermissionsJobLevelRepository permissionsJobLevelRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // -------------------------
        // States
        // -------------------------
        State sp = new State();
        sp.setName("São Paulo");
        sp.setUf("SP");
        stateRepository.save(sp);

        State rj = new State();
        rj.setName("Rio de Janeiro");
        rj.setUf("RJ");
        stateRepository.save(rj);

        // -------------------------
        // CityHalls
        // -------------------------
        CityHall cityHallSP = new CityHall();
        cityHallSP.setName("Prefeitura de São Paulo");
        cityHallSP.setCnpj("46.395.000/0001-39");
        cityHallSP.setState(sp);
        cityHallRepository.save(cityHallSP);

        CityHall cityHallRJ = new CityHall();
        cityHallRJ.setName("Prefeitura do Rio de Janeiro");
        cityHallRJ.setCnpj("42.498.383/0001-48");
        cityHallRJ.setState(rj);
        cityHallRepository.save(cityHallRJ);

        // -------------------------
        // Sectors
        // -------------------------
        Sector sectorTI = new Sector();
        sectorTI.setName("Tecnologia da Informação");
        sectorTI.setCityhall(cityHallSP);
        sectorRepository.save(sectorTI);

        Sector sectorRH = new Sector();
        sectorRH.setName("Recursos Humanos");
        sectorRH.setCityhall(cityHallSP);
        sectorRepository.save(sectorRH);

        Sector sectorFinanceiro = new Sector();
        sectorFinanceiro.setName("Financeiro");
        sectorFinanceiro.setCityhall(cityHallRJ);
        sectorRepository.save(sectorFinanceiro);

        // -------------------------
        // JobLevels
        // -------------------------
        JobLevel analista = new JobLevel();
        analista.setId(UUID.randomUUID());
        analista.setName("Analista de Sistemas");
        analista.setDescription("Responsável por análise e desenvolvimento de sistemas");
        analista.getTypes().add(TypeJobLevel.CARGO_COMISSAO);
        analista.setLevel(LevelJobLevel.JUNIOR);
        jobLevelRepository.save(analista);

        JobLevel gerente = new JobLevel();
        gerente.setId(UUID.randomUUID());
        gerente.setName("Gerente de TI");
        gerente.setDescription("Responsável pela gestão da equipe de TI");
        gerente.getTypes().add(TypeJobLevel.CONCURSADO);
        gerente.setLevel(LevelJobLevel.SENIOR);
        jobLevelRepository.save(gerente);

        JobLevel assistente = new JobLevel();
        assistente.setId(UUID.randomUUID());
        assistente.setName("Assistente Administrativo");
        assistente.setDescription("Suporte administrativo geral");
        assistente.getTypes().add(TypeJobLevel.TERCEIRIZADO);
        assistente.setLevel(LevelJobLevel.JUNIOR);
        jobLevelRepository.save(assistente);

        // -------------------------
        // JobLevelSector (Many-to-Many)
        // -------------------------
        jobLevelSectorRepository.save(new JobLevelSector(sectorTI, analista));
        jobLevelSectorRepository.save(new JobLevelSector(sectorTI, gerente));
        jobLevelSectorRepository.save(new JobLevelSector(sectorRH, assistente));
        jobLevelSectorRepository.save(new JobLevelSector(sectorFinanceiro, assistente));
        // analista também pode estar no financeiro
        jobLevelSectorRepository.save(new JobLevelSector(sectorFinanceiro, analista));

        // -------------------------
        // Permissions
        // -------------------------
        Permissions permRead = new Permissions();
        permRead.setName("Leitura");
        permRead.setCodename("READ");
        permissionsRepository.save(permRead);

        Permissions permWrite = new Permissions();
        permWrite.setName("Escrita");
        permWrite.setCodename("WRITE");
        permissionsRepository.save(permWrite);

        Permissions permDelete = new Permissions();
        permDelete.setName("Exclusão");
        permDelete.setCodename("DELETE");
        permissionsRepository.save(permDelete);

        Permissions permAdmin = new Permissions();
        permAdmin.setName("Administrador");
        permAdmin.setCodename("ADMIN");
        permissionsRepository.save(permAdmin);

        // -------------------------
        // PermissionsJobLevel (Many-to-Many)
        // -------------------------
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permRead, analista));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permWrite, analista));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permRead, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permWrite, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permDelete, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permAdmin, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permRead, assistente));

        // -------------------------
        // Employees
        // -------------------------
        Employee emp1 = new Employee();
        emp1.setName("João Silva");
        emp1.setEmail("joao.silva@sp.gov.br");
        emp1.setPassword(passwordEncoder.encode("senha123"));
        emp1.setStatus(true);
        emp1.setRole(Roles.ADMIN);
        emp1.setCpf("123.456.789-00");
        emp1.setSalary(5500.00);
        emp1.setAdmissionDate(LocalDateTime.of(2020, 3, 15, 8, 0));
        emp1.setRegistrationNumber("SP-001");
        emp1.setHoursWorked(1840.0);
        emp1.setJobLevel(analista);
        employeeRepository.save(emp1);

        Employee emp2 = new Employee();
        emp2.setName("Maria Oliveira");
        emp2.setEmail("maria.oliveira@sp.gov.br");
        emp2.setPassword(passwordEncoder.encode("senha123"));
        emp2.setStatus(true);
        emp2.setRole(Roles.ADMIN);
        emp2.setCpf("987.654.321-00");
        emp2.setSalary(12000.00);
        emp2.setAdmissionDate(LocalDateTime.of(2018, 6, 1, 8, 0));
        emp2.setRegistrationNumber("SP-002");
        emp2.setHoursWorked(2200.0);
        emp2.setJobLevel(gerente);
        employeeRepository.save(emp2);

        Employee emp3 = new Employee();
        emp3.setName("Carlos Mendes");
        emp3.setEmail("carlos.mendes@rj.gov.br");
        emp3.setPassword(passwordEncoder.encode("senha123"));
        emp3.setStatus(true);
        emp3.setRole(Roles.ADMIN);
        emp3.setCpf("111.222.333-44");
        emp3.setSalary(3200.00);
        emp3.setAdmissionDate(LocalDateTime.of(2022, 1, 10, 8, 0));
        emp3.setRegistrationNumber("RJ-001");
        emp3.setHoursWorked(920.0);
        emp3.setJobLevel(assistente);
        employeeRepository.save(emp3);

        System.out.println("✅ Mocks carregados com sucesso!");
    }
}
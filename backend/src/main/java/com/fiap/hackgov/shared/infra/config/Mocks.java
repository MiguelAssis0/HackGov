package com.fiap.hackgov.shared.infra.config;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.*;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelJobLevel;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.UF;
import com.fiap.hackgov.cityhall_management.internal.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

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
        sp.setUf(UF.SP);
        stateRepository.save(sp);

        State rj = new State();
        rj.setName("Rio de Janeiro");
        rj.setUf(UF.RJ);
        stateRepository.save(rj);

        // -------------------------
        // CityHalls
        // -------------------------
        CityHall cityHallSP = new CityHall();
        cityHallSP.setName("Prefeitura de São Paulo");
        cityHallSP.setCnpj("46.395.000/0001-39");
        cityHallSP.setState(sp);
        sp.getCityHalls().add(cityHallSP);
        cityHallRepository.save(cityHallSP);

        CityHall cityHallRJ = new CityHall();
        cityHallRJ.setName("Prefeitura do Rio de Janeiro");
        cityHallRJ.setCnpj("42.498.383/0001-48");
        cityHallRJ.setState(rj);
        rj.getCityHalls().add(cityHallRJ);
        cityHallRepository.save(cityHallRJ);

        // -------------------------
        // Sectors
        // -------------------------
        Sector sectorTI = new Sector();
        sectorTI.setName("Tecnologia da Informação");
        sectorTI.setCityHall(cityHallSP);
        sectorRepository.save(sectorTI);

        Sector sectorRH = new Sector();
        sectorRH.setName("Recursos Humanos");
        sectorRH.setCityHall(cityHallSP);
        sectorRepository.save(sectorRH);

        Sector sectorFinanceiro = new Sector();
        sectorFinanceiro.setName("Financeiro");
        sectorFinanceiro.setCityHall(cityHallRJ);
        sectorRepository.save(sectorFinanceiro);

        // -------------------------
        // JobLevels
        // -------------------------
        JobLevel analista = new JobLevel();
        analista.setName("Analista de Sistemas");
        analista.setDescription("Responsável por análise e desenvolvimento de sistemas");
        analista.getTypes().add(TypeJobLevel.CARGO_COMISSAO);
        analista.setLevel(LevelJobLevel.JUNIOR);
        jobLevelRepository.save(analista);

        JobLevel gerente = new JobLevel();
        gerente.setName("Gerente de TI");
        gerente.setDescription("Responsável pela gestão da equipe de TI");
        gerente.getTypes().add(TypeJobLevel.CONCURSADO);
        gerente.setLevel(LevelJobLevel.SENIOR);
        jobLevelRepository.save(gerente);

        JobLevel assistente = new JobLevel();
        assistente.setName("Assistente Administrativo");
        assistente.setDescription("Suporte administrativo geral");
        assistente.getTypes().add(TypeJobLevel.TERCEIRIZADO);
        assistente.setLevel(LevelJobLevel.JUNIOR);
        jobLevelRepository.save(assistente);

        // -------------------------
        // JobLevelSector
        // -------------------------
        jobLevelSectorRepository.save(new JobLevelSector(sectorTI, analista));
        jobLevelSectorRepository.save(new JobLevelSector(sectorTI, gerente));
        jobLevelSectorRepository.save(new JobLevelSector(sectorRH, assistente));
        jobLevelSectorRepository.save(new JobLevelSector(sectorFinanceiro, assistente));
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
        // PermissionsJobLevel
        // -------------------------
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permRead, analista));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permWrite, analista));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permRead, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permWrite, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permDelete, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permAdmin, gerente));
        permissionsJobLevelRepository.save(new PermissionsJobLevel(permRead, assistente));

        Employee admin = new Employee();
        admin.setFirstName("Admin");
        admin.setLastName("Sistema");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setStatus(true);
        admin.setRole(Roles.ADMIN);
        admin.setCpf("123.456.789-00");
        admin.setTwoFactor(false);

        admin.setSalary(5500.00);
        admin.setAdmissionDate(LocalDateTime.of(2020, 3, 15, 8, 0));
        admin.setRegistrationNumber("SP-001");
        admin.setHoursWorked(1840.0);
        admin.setCityHallId(cityHallSP);

        admin.getEmployeeJobLevels().add(new EmployeeJobLevel(admin, analista));

        employeeRepository.save(admin);


        Employee maria = new Employee();
        maria.setFirstName("Maria");
        maria.setLastName("Oliveira");
        maria.setEmail("maria.oliveira@sp.gov.br");
        maria.setPassword(passwordEncoder.encode("senha123"));
        maria.setStatus(true);
        maria.setRole(Roles.ADMIN);
        maria.setCpf("987.654.321-00");
        maria.setTwoFactor(false);

        maria.setSalary(12000.00);
        maria.setAdmissionDate(LocalDateTime.of(2018, 6, 1, 8, 0));
        maria.setRegistrationNumber("SP-002");
        maria.setHoursWorked(2200.0);
        maria.setCityHallId(cityHallSP);

        maria.getEmployeeJobLevels().add(new EmployeeJobLevel(maria, gerente));

        employeeRepository.save(maria);


        Employee carlos = new Employee();
        carlos.setFirstName("Carlos");
        carlos.setLastName("Mendes");
        carlos.setEmail("carlos.mendes@rj.gov.br");
        carlos.setPassword(passwordEncoder.encode("senha123"));
        carlos.setStatus(true);
        carlos.setRole(Roles.ADMIN);
        carlos.setCpf("111.222.333-44");
        carlos.setTwoFactor(false);

        carlos.setSalary(3200.00);
        carlos.setAdmissionDate(LocalDateTime.of(2022, 1, 10, 8, 0));
        carlos.setRegistrationNumber("RJ-001");
        carlos.setHoursWorked(920.0);
        carlos.setCityHallId(cityHallRJ);

        carlos.getEmployeeJobLevels().add(new EmployeeJobLevel(carlos, assistente));

        employeeRepository.save(carlos);

        System.out.println("Mocks carregados com sucesso!");
    }
}
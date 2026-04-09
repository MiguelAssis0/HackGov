package com.fiap.hackgov.shared.infra.config;

import com.fiap.hackgov.auth.internal.entities.User;
import com.fiap.hackgov.auth.internal.repositories.UserRepository;
import com.fiap.hackgov.erp.internal.entities.*;
import com.fiap.hackgov.erp.internal.repositories.*;
import com.fiap.hackgov.erp.internal.entities.enums.LevelJobLevel;
import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.erp.internal.entities.enums.TypeJobLevel;
import com.fiap.hackgov.erp.internal.entities.enums.UF;
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
    @Autowired private UserRepository userRepository; // ✅ adicione o UserRepository
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

        // -------------------------
        // Users (apenas autenticação)
        // -------------------------
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Sistema");
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));
        admin.setStatus(true);
        admin.setRole(Roles.ADMIN);
        admin.setCpf("123.456.789-00");
        admin.setTwoFactor(false);
        userRepository.save(admin); // ✅ salva o User primeiro para ter o UUID

        User userMaria = new User();
        userMaria.setFirstName("Maria");
        userMaria.setLastName("Oliveira");
        userMaria.setEmail("maria.oliveira@sp.gov.br");
        userMaria.setPassword(passwordEncoder.encode("senha123"));
        userMaria.setStatus(true);
        userMaria.setRole(Roles.ADMIN);
        userMaria.setCpf("987.654.321-00");
        userMaria.setTwoFactor(false);
        userRepository.save(userMaria);

        User userCarlos = new User();
        userCarlos.setFirstName("Carlos");
        userCarlos.setLastName("Mendes");
        userCarlos.setEmail("carlos.mendes@rj.gov.br");
        userCarlos.setPassword(passwordEncoder.encode("senha123"));
        userCarlos.setStatus(true);
        userCarlos.setRole(Roles.ADMIN);
        userCarlos.setCpf("111.222.333-44");
        userCarlos.setTwoFactor(false);
        userRepository.save(userCarlos);

        // -------------------------
        // Employees (dados funcionais vinculados ao User via userId)
        // -------------------------
        Employee emp1 = new Employee();
        emp1.setUserId(admin.getId()); // ✅ vincula ao User pelo UUID
        emp1.setSalary(5500.00);
        emp1.setAdmissionDate(LocalDateTime.of(2020, 3, 15, 8, 0));
        emp1.setRegistrationNumber("SP-001");
        emp1.setHoursWorked(1840.0);
        emp1.setCityhallId(cityHallSP);
        emp1.getEmployeeJobLevels().add(new EmployeeJobLevel(emp1, analista));
        employeeRepository.save(emp1);

        Employee emp2 = new Employee();
        emp2.setUserId(userMaria.getId());
        emp2.setSalary(12000.00);
        emp2.setAdmissionDate(LocalDateTime.of(2018, 6, 1, 8, 0));
        emp2.setRegistrationNumber("SP-002");
        emp2.setHoursWorked(2200.0);
        emp2.setCityhallId(cityHallSP);
        emp2.getEmployeeJobLevels().add(new EmployeeJobLevel(emp2, gerente));
        employeeRepository.save(emp2);

        Employee emp3 = new Employee();
        emp3.setUserId(userCarlos.getId());
        emp3.setSalary(3200.00);
        emp3.setAdmissionDate(LocalDateTime.of(2022, 1, 10, 8, 0));
        emp3.setRegistrationNumber("RJ-001");
        emp3.setHoursWorked(920.0);
        emp3.setCityhallId(cityHallRJ);
        emp3.getEmployeeJobLevels().add(new EmployeeJobLevel(emp3, assistente));
        employeeRepository.save(emp3);

        System.out.println("Mocks carregados com sucesso!");
    }
}
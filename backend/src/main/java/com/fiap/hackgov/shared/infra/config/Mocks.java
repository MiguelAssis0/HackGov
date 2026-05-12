package com.fiap.hackgov.shared.infra.config;

import com.fiap.hackgov.auth.internal.entities.enums.Roles;
import com.fiap.hackgov.cityhall_management.internal.entities.*;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.Actions;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.UF;
import com.fiap.hackgov.cityhall_management.internal.repositories.*;
import com.fiap.hackgov.messages.internal.entities.Chat;
import com.fiap.hackgov.messages.internal.entities.ChatParticipant;
import com.fiap.hackgov.messages.internal.entities.enums.ChatRole;
import com.fiap.hackgov.messages.internal.entities.enums.ChatType;
import com.fiap.hackgov.messages.internal.repositories.ChatParticipantRepository;
import com.fiap.hackgov.messages.internal.repositories.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("dev")
public class Mocks implements CommandLineRunner {

    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CityHallRepository cityHallRepository;
    @Autowired
    private SectorRepository sectorRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private PermissionsRepository permissionsRepository;
    @Autowired
    private PermissionsOccupationRepository permissionsOccupationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

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
        Occupation analista = new Occupation();
        analista.setName("Analista de Sistemas");
        analista.setDescription("Responsável por análise e desenvolvimento de sistemas");
        analista.setTypes(TypeJobLevel.CARGO_COMISSAO);
        analista.setLevel(LevelOccupation.JUNIOR);
        occupationRepository.save(analista);

        Occupation gerente = new Occupation();
        gerente.setName("Gerente de TI");
        gerente.setDescription("Responsável pela gestão da equipe de TI");
        gerente.setTypes(TypeJobLevel.CONCURSADO);
        gerente.setLevel(LevelOccupation.SENIOR);
        occupationRepository.save(gerente);

        Occupation assistente = new Occupation();
        assistente.setName("Assistente Administrativo");
        assistente.setDescription("Suporte administrativo geral");
        assistente.setTypes(TypeJobLevel.TERCEIRIZADO);
        assistente.setLevel(LevelOccupation.JUNIOR);
        occupationRepository.save(assistente);

        // -------------------------
        // Permissions
        // -------------------------
        Permissions permRead = new Permissions();
        permRead.setResource("SECTOR");
        permRead.getAction().add(Actions.READ);
        permissionsRepository.save(permRead);

        Permissions permWrite = new Permissions();
        permWrite.setResource("SECTOR");
        permWrite.getAction().add(Actions.CREATE);
        permissionsRepository.save(permWrite);

        Permissions permDelete = new Permissions();
        permDelete.setResource("SECTOR");
        permDelete.getAction().add(Actions.DELETE);
        permissionsRepository.save(permDelete);

        Permissions permAdmin = new Permissions();
        permAdmin.setResource("SECTOR");
        permAdmin.getAction().add(Actions.UPDATE);
        permissionsRepository.save(permAdmin);

        // -------------------------
        // PermissionsJobLevel
        // -------------------------
        permissionsOccupationRepository.save(new PermissionsOccupation(permRead, analista));
        permissionsOccupationRepository.save(new PermissionsOccupation(permWrite, analista));
        permissionsOccupationRepository.save(new PermissionsOccupation(permRead, gerente));
        permissionsOccupationRepository.save(new PermissionsOccupation(permWrite, gerente));
        permissionsOccupationRepository.save(new PermissionsOccupation(permDelete, gerente));
        permissionsOccupationRepository.save(new PermissionsOccupation(permAdmin, gerente));
        permissionsOccupationRepository.save(new PermissionsOccupation(permRead, assistente));

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

        admin.setOccupationId(analista);

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

        maria.setOccupationId(gerente);

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

        carlos.setOccupationId(assistente);

        employeeRepository.save(carlos);

        Employee ana = new Employee();
        ana.setFirstName("Ana");
        ana.setLastName("Souza");
        ana.setEmail("ana.souza@sp.gov.br");
        ana.setPassword(passwordEncoder.encode("senha123"));
        ana.setStatus(true);
        ana.setRole(Roles.ADMIN);
        ana.setCpf("999.888.777-66");
        ana.setTwoFactor(false);

        ana.setSalary(4300.00);
        ana.setAdmissionDate(LocalDateTime.of(2023, 5, 5, 8, 0));
        ana.setRegistrationNumber("SP-004");
        ana.setHoursWorked(700.0);
        ana.setCityHallId(cityHallSP);

        ana.setOccupationId(assistente);

        employeeRepository.save(ana);

        Employee joao = new Employee();
        joao.setFirstName("João");
        joao.setLastName("Silva");
        joao.setEmail("joao.silva@sp.gov.br");
        joao.setPassword(passwordEncoder.encode("senha123"));
        joao.setStatus(true);
        joao.setRole(Roles.ADMIN);
        joao.setCpf("555.666.777-88");
        joao.setTwoFactor(false);

        joao.setSalary(4800.00);
        joao.setAdmissionDate(LocalDateTime.of(2021, 2, 20, 8, 0));
        joao.setRegistrationNumber("SP-003");
        joao.setHoursWorked(1600.0);
        joao.setCityHallId(cityHallSP);

        joao.setOccupationId(analista);

        employeeRepository.save(joao);

        Employee fernanda = new Employee();
        fernanda.setFirstName("Fernanda");
        fernanda.setLastName("Costa");
        fernanda.setEmail("fernanda.costa@rj.gov.br");
        fernanda.setPassword(passwordEncoder.encode("senha123"));
        fernanda.setStatus(true);
        fernanda.setRole(Roles.ADMIN);
        fernanda.setCpf("444.555.666-77");
        fernanda.setTwoFactor(false);

        fernanda.setSalary(6100.00);
        fernanda.setAdmissionDate(LocalDateTime.of(2019, 9, 12, 8, 0));
        fernanda.setRegistrationNumber("RJ-002");
        fernanda.setHoursWorked(2100.0);
        fernanda.setCityHallId(cityHallRJ);

        fernanda.setOccupationId(analista);

        employeeRepository.save(fernanda);

        Employee lucas = new Employee();
        lucas.setFirstName("Lucas");
        lucas.setLastName("Pereira");
        lucas.setEmail("lucas.pereira@rj.gov.br");
        lucas.setPassword(passwordEncoder.encode("senha123"));
        lucas.setStatus(true);
        lucas.setRole(Roles.ADMIN);
        lucas.setCpf("222.333.444-55");
        lucas.setTwoFactor(false);

        lucas.setSalary(3900.00);
        lucas.setAdmissionDate(LocalDateTime.of(2024, 1, 8, 8, 0));
        lucas.setRegistrationNumber("RJ-003");
        lucas.setHoursWorked(300.0);
        lucas.setCityHallId(cityHallRJ);

        lucas.setOccupationId(assistente);

        employeeRepository.save(lucas);

        LocalDateTime now = LocalDateTime.now();

        Chat privateSP = new Chat();
        privateSP.setType(ChatType.PRIVATE);
        privateSP.setCityHall(cityHallSP);
        privateSP.setCreatedAt(now);

        chatRepository.save(privateSP);

        ChatParticipant privateSPAdmin = new ChatParticipant();
        privateSPAdmin.setChat(privateSP);
        privateSPAdmin.setEmployee(admin);
        privateSPAdmin.setJoinedAt(now);
        privateSPAdmin.setRole(ChatRole.MEMBER);

        ChatParticipant privateSPJoao = new ChatParticipant();
        privateSPJoao.setChat(privateSP);
        privateSPJoao.setEmployee(joao);
        privateSPJoao.setJoinedAt(now);
        privateSPJoao.setRole(ChatRole.MEMBER);

        chatParticipantRepository.saveAll(List.of(privateSPAdmin, privateSPJoao));

        privateSP.getParticipants().add(privateSPAdmin);
        privateSP.getParticipants().add(privateSPJoao);

        Chat privateRJ = new Chat();
        privateRJ.setType(ChatType.PRIVATE);
        privateRJ.setCityHall(cityHallRJ);
        privateRJ.setCreatedAt(now);

        chatRepository.save(privateRJ);

        ChatParticipant privateRJCarlos = new ChatParticipant();
        privateRJCarlos.setChat(privateRJ);
        privateRJCarlos.setEmployee(carlos);
        privateRJCarlos.setJoinedAt(now);
        privateRJCarlos.setRole(ChatRole.MEMBER);

        ChatParticipant privateRJFernanda = new ChatParticipant();
        privateRJFernanda.setChat(privateRJ);
        privateRJFernanda.setEmployee(fernanda);
        privateRJFernanda.setJoinedAt(now);
        privateRJFernanda.setRole(ChatRole.MEMBER);

        chatParticipantRepository.saveAll(List.of(privateRJCarlos, privateRJFernanda));

        privateRJ.getParticipants().add(privateRJCarlos);
        privateRJ.getParticipants().add(privateRJFernanda);

        Chat groupSP = new Chat();
        groupSP.setTitle("TI São Paulo");
        groupSP.setType(ChatType.GROUP);
        groupSP.setCityHall(cityHallSP);
        groupSP.setCreatedAt(now);

        chatRepository.save(groupSP);

        ChatParticipant groupSPAdmin = new ChatParticipant();
        groupSPAdmin.setChat(groupSP);
        groupSPAdmin.setEmployee(admin);
        groupSPAdmin.setJoinedAt(now);
        groupSPAdmin.setRole(ChatRole.ADMIN);

        ChatParticipant groupSPMaria = new ChatParticipant();
        groupSPMaria.setChat(groupSP);
        groupSPMaria.setEmployee(maria);
        groupSPMaria.setJoinedAt(now);
        groupSPMaria.setRole(ChatRole.MEMBER);

        ChatParticipant groupSPAna = new ChatParticipant();
        groupSPAna.setChat(groupSP);
        groupSPAna.setEmployee(ana);
        groupSPAna.setJoinedAt(now);
        groupSPAna.setRole(ChatRole.MEMBER);

        chatParticipantRepository.saveAll(List.of(groupSPAdmin, groupSPMaria, groupSPAna));

        groupSP.getParticipants().addAll(List.of(groupSPAdmin, groupSPMaria, groupSPAna));

        Chat groupRJ = new Chat();
        groupRJ.setTitle("Financeiro RJ");
        groupRJ.setType(ChatType.GROUP);
        groupRJ.setCityHall(cityHallRJ);
        groupRJ.setCreatedAt(now);

        chatRepository.save(groupRJ);

        ChatParticipant groupRJCarlos = new ChatParticipant();
        groupRJCarlos.setChat(groupRJ);
        groupRJCarlos.setEmployee(carlos);
        groupRJCarlos.setJoinedAt(now);
        groupRJCarlos.setRole(ChatRole.ADMIN);

        ChatParticipant groupRJFernanda = new ChatParticipant();
        groupRJFernanda.setChat(groupRJ);
        groupRJFernanda.setEmployee(fernanda);
        groupRJFernanda.setJoinedAt(now);
        groupRJFernanda.setRole(ChatRole.MEMBER);

        ChatParticipant groupRJLucas = new ChatParticipant();
        groupRJLucas.setChat(groupRJ);
        groupRJLucas.setEmployee(lucas);
        groupRJLucas.setJoinedAt(now);
        groupRJLucas.setRole(ChatRole.MEMBER);

        chatParticipantRepository.saveAll(List.of(groupRJCarlos, groupRJFernanda, groupRJLucas));

        groupRJ.getParticipants().addAll(List.of(groupRJCarlos, groupRJFernanda, groupRJLucas));

        System.out.println("Mocks carregados com sucesso!");
    }
}
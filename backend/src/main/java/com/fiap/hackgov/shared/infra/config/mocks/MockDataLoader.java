package com.fiap.hackgov.shared.infra.config.mocks;

import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.shared.infra.config.mocks.chat.ChatMock;
import com.fiap.hackgov.shared.infra.config.mocks.cityhall.CityHallMock;
import com.fiap.hackgov.shared.infra.config.mocks.employee.EmployeeMock;
import com.fiap.hackgov.shared.infra.config.mocks.licitation.LicitationMock;
import com.fiap.hackgov.shared.infra.config.mocks.licitation.ContractPaymentMock;
import com.fiap.hackgov.shared.infra.config.mocks.licitation.NoticeContractMock;
import com.fiap.hackgov.shared.infra.config.mocks.occupation.OccupationMock;
import com.fiap.hackgov.shared.infra.config.mocks.permission.PermissionMock;
import com.fiap.hackgov.shared.infra.config.mocks.requisition.RequisitionMock;
import com.fiap.hackgov.shared.infra.config.mocks.sector.SectorMock;
import com.fiap.hackgov.shared.infra.config.mocks.state.StateMock;
import com.fiap.hackgov.shared.infra.config.mocks.task.BoardMock;
import com.fiap.hackgov.shared.infra.config.mocks.task.TaskMock;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class MockDataLoader implements CommandLineRunner {
    private static final String SEED_CITY_HALL_CNPJ = "46.395.000/0001-39";

    private final CityHallRepository cityHallRepository;
    private final StateMock stateMock;
    private final CityHallMock cityHallMock;
    private final OccupationMock occupationMock;
    private final PermissionMock permissionMock;
    private final EmployeeMock employeeMock;
    private final ChatMock chatMock;
    private final SectorMock sectorMock;
    private final BoardMock boardMock;
    private final TaskMock taskMock;
    private final RequisitionMock requisitionMock;
    private final LicitationMock licitationMock;
    private final NoticeContractMock noticeContractMock;
    private final ContractPaymentMock contractPaymentMock;

    @Override
    public void run(String... args) {
        if (cityHallRepository.findByCnpj(SEED_CITY_HALL_CNPJ).isPresent()) {
            log.info("Mocks já carregados; carga inicial ignorada.");
            return;
        }

        MockContext ctx = new MockContext();
        stateMock.load(ctx);
        cityHallMock.load(ctx);
        sectorMock.load(ctx);
        boardMock.load(ctx);
        occupationMock.load(ctx);
        permissionMock.load(ctx);
        employeeMock.load(ctx);
        taskMock.load(ctx);
        chatMock.load(ctx);
        requisitionMock.load(ctx);
        licitationMock.load(ctx);
        noticeContractMock.load(ctx);
        contractPaymentMock.load(ctx);
        log.info("Mocks carregados!");
    }
}

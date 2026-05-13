package com.fiap.hackgov.shared.infra.config.mocks;

import com.fiap.hackgov.shared.infra.config.mocks.chat.ChatMock;
import com.fiap.hackgov.shared.infra.config.mocks.cityhall.CityHallMock;
import com.fiap.hackgov.shared.infra.config.mocks.employee.EmployeeMock;
import com.fiap.hackgov.shared.infra.config.mocks.occupation.OccupationMock;
import com.fiap.hackgov.shared.infra.config.mocks.permission.PermissionMock;
import com.fiap.hackgov.shared.infra.config.mocks.state.StateMock;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class MockDataLoader implements CommandLineRunner {
    private final StateMock stateMock;
    private final CityHallMock cityHallMock;
    private final OccupationMock occupationMock;
    private final PermissionMock permissionMock;
    private final EmployeeMock employeeMock;
    private final ChatMock chatMock;

    @Override
    public void run(String... args) {
        MockContext ctx = new MockContext();
        stateMock.load(ctx);
        cityHallMock.load(ctx);
        occupationMock.load(ctx);
        permissionMock.load(ctx);
        employeeMock.load(ctx);
        chatMock.load(ctx);
        System.out.println("Mocks carregados!");
    }
}

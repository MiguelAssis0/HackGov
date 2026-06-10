package com.fiap.hackgov.shared.infra.config.mocks.state;

import com.fiap.hackgov.cityhall_management.internal.entities.State;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.UF;
import com.fiap.hackgov.cityhall_management.internal.repositories.StateRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StateMock {
    private final StateRepository repository;

    public void load(MockContext ctx) {
        State sp = new State();
        sp.setName("São Paulo");
        sp.setUf(UF.SP);
        State rj = new State();
        rj.setName("Rio de Janeiro");
        rj.setUf(UF.RJ);
        repository.saveAll(List.of(sp, rj));
        ctx.sp = sp;
        ctx.rj = rj;
    }
}
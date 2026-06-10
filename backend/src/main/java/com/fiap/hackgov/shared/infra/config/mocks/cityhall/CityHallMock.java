package com.fiap.hackgov.shared.infra.config.mocks.cityhall;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.repositories.CityHallRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CityHallMock {
    private final CityHallRepository repository;

    public void load(MockContext ctx) {
        CityHall sp = new CityHall();
        sp.setName("Prefeitura de São Paulo");
        sp.setCnpj("46.395.000/0001-39");
        sp.setState(ctx.sp);
        CityHall rj = new CityHall();
        rj.setName("Prefeitura do Rio de Janeiro");
        rj.setCnpj("42.498.383/0001-48");
        rj.setState(ctx.rj);
        repository.saveAll(List.of(sp, rj));
        ctx.cityHallSP = sp;
        ctx.cityHallRJ = rj;
    }
}
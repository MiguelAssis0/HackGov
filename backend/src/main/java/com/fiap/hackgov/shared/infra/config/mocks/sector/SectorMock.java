package com.fiap.hackgov.shared.infra.config.mocks.sector;

import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SectorMock {

    private final SectorRepository repository;

    public void load(MockContext ctx) {
        Sector ti = createSector("Tecnologia da Informação", ctx.cityHallSP);
        Sector compras = createSector("Compras", ctx.cityHallSP);
        Sector financeiro = createSector("Financeiro", ctx.cityHallSP);
        Sector contratos = createSector("Contratos", ctx.cityHallSP);
        Sector juridico = createSector("Jurídico", ctx.cityHallSP);

        Sector tiRJ = createSector("Tecnologia da Informação", ctx.cityHallRJ);
        Sector comprasRJ = createSector("Compras", ctx.cityHallRJ);
        Sector financeiroRJ = createSector("Financeiro", ctx.cityHallRJ);
        Sector contratosRJ = createSector("Contratos", ctx.cityHallRJ);
        Sector juridicoRJ = createSector("Jurídico", ctx.cityHallRJ);

        repository.saveAll(List.of(ti, compras, financeiro, contratos, juridico, tiRJ, comprasRJ, financeiroRJ, contratosRJ, juridicoRJ));

        ctx.tiSectorsSP = ti;
        ctx.comprasSectorSP = compras;
        ctx.financeiroSectorSP = financeiro;
        ctx.contratosSectorSP = contratos;
        ctx.juridicoSectorSP = juridico;
        ctx.tiSectorRJ = tiRJ;
        ctx.comprasSectorRJ = comprasRJ;
        ctx.financeiroSectorRJ = financeiroRJ;
        ctx.contratosSectorRJ = contratosRJ;
        ctx.juridicoSectorRJ = juridicoRJ;
    }

    private Sector createSector(String name, com.fiap.hackgov.cityhall_management.internal.entities.CityHall cityHall) {
        Sector sector = new Sector();
        sector.setName(name);
        sector.setCityHall(cityHall);
        return sector;
    }
}

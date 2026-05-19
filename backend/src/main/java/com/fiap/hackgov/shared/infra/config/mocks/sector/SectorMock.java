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

        Sector ti = new Sector();
        ti.setName("Tecnologia da Informação");

        Sector compras = new Sector();
        compras.setName("Compras");

        Sector financeiro = new Sector();
        financeiro.setName("Financeiro");

        Sector contratos = new Sector();
        contratos.setName("Contratos");

        Sector juridico = new Sector();
        juridico.setName("Jurídico");

        repository.saveAll(
                List.of(
                        ti,
                        compras,
                        financeiro,
                        contratos,
                        juridico
                )
        );

        ctx.tiSector = ti;
        ctx.comprasSector = compras;
        ctx.financeiroSector = financeiro;
        ctx.contratosSector = contratos;
        ctx.juridicoSector = juridico;
    }
}

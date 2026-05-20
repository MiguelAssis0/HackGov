package com.fiap.hackgov.shared.infra.config.mocks.occupation;

import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.LevelOccupation;
import com.fiap.hackgov.cityhall_management.internal.entities.enums.TypeJobLevel;
import com.fiap.hackgov.cityhall_management.internal.repositories.OccupationRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OccupationMock {
    private final OccupationRepository repository;

    public void load(MockContext ctx) {
        Occupation administradorMunicipal = new Occupation();
        administradorMunicipal.setName("Administrador Municipal");
        administradorMunicipal.setDescription("Responsável pela administração geral da prefeitura e governança do sistema");
        administradorMunicipal.setTypes(TypeJobLevel.CARGO_COMISSAO);
        administradorMunicipal.setLevel(LevelOccupation.SENIOR);
        administradorMunicipal.setSectorId(ctx.tiSectorsSP);

        Occupation analista = new Occupation();
        analista.setName("Analista de Sistemas");
        analista.setDescription("Responsável por análise e desenvolvimento");
        analista.setTypes(TypeJobLevel.CARGO_COMISSAO);
        analista.setLevel(LevelOccupation.JUNIOR);
        analista.setSectorId(ctx.tiSectorsSP);

        Occupation gerente = new Occupation();
        gerente.setName("Gerente de TI");
        gerente.setDescription("Gestão da equipe");
        gerente.setTypes(TypeJobLevel.CONCURSADO);
        gerente.setLevel(LevelOccupation.SENIOR);
        gerente.setSectorId(ctx.tiSectorsSP);

        Occupation assistente = new Occupation();
        assistente.setName("Assistente Administrativo");
        assistente.setDescription("Suporte administrativo");
        assistente.setTypes(TypeJobLevel.TERCEIRIZADO);
        assistente.setLevel(LevelOccupation.JUNIOR);
        assistente.setSectorId(ctx.comprasSectorSP);

        Occupation agenteCompras = new Occupation();
        agenteCompras.setName("Agente de Compras");
        agenteCompras.setDescription("Conduz cotações, instrução da fase interna e recebimento de demandas");
        agenteCompras.setTypes(TypeJobLevel.CONCURSADO);
        agenteCompras.setLevel(LevelOccupation.MID);
        agenteCompras.setSectorId(ctx.comprasSectorSP);

        Occupation pregoeiro = new Occupation();
        pregoeiro.setName("Pregoeiro");
        pregoeiro.setDescription("Responsável pela condução do processo licitatório e julgamento");
        pregoeiro.setTypes(TypeJobLevel.CARGO_COMISSAO);
        pregoeiro.setLevel(LevelOccupation.SENIOR);
        pregoeiro.setSectorId(ctx.comprasSectorSP);

        Occupation analistaFinanceiro = new Occupation();
        analistaFinanceiro.setName("Analista Financeiro");
        analistaFinanceiro.setDescription("Atua em reserva orçamentária, empenho e liquidação");
        analistaFinanceiro.setTypes(TypeJobLevel.CONCURSADO);
        analistaFinanceiro.setLevel(LevelOccupation.MID);
        analistaFinanceiro.setSectorId(ctx.financeiroSectorSP);

        Occupation gestorContratos = new Occupation();
        gestorContratos.setName("Gestor de Contratos");
        gestorContratos.setDescription("Acompanha execução contratual, vigência e obrigações do fornecedor");
        gestorContratos.setTypes(TypeJobLevel.CARGO_COMISSAO);
        gestorContratos.setLevel(LevelOccupation.SENIOR);
        gestorContratos.setSectorId(ctx.contratosSectorSP);

        Occupation assessorJuridico = new Occupation();
        assessorJuridico.setName("Assessor Jurídico");
        assessorJuridico.setDescription("Emite pareceres e apoio jurídico em editais, impugnações e contratos");
        assessorJuridico.setTypes(TypeJobLevel.CONCURSADO);
        assessorJuridico.setLevel(LevelOccupation.SENIOR);
        assessorJuridico.setSectorId(ctx.juridicoSectorSP);

        repository.saveAll(List.of(
                administradorMunicipal,
                analista,
                gerente,
                assistente,
                agenteCompras,
                pregoeiro,
                analistaFinanceiro,
                gestorContratos,
                assessorJuridico
        ));
        ctx.administradorMunicipal = administradorMunicipal;
        ctx.analista = analista;
        ctx.gerente = gerente;
        ctx.assistente = assistente;
        ctx.agenteCompras = agenteCompras;
        ctx.pregoeiro = pregoeiro;
        ctx.analistaFinanceiro = analistaFinanceiro;
        ctx.gestorContratos = gestorContratos;
        ctx.assessorJuridico = assessorJuridico;
    }
}

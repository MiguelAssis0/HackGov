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
        Occupation analista = new Occupation();
        analista.setName("Analista de Sistemas");
        analista.setDescription("Responsável por análise e desenvolvimento");
        analista.setTypes(TypeJobLevel.CARGO_COMISSAO);
        analista.setLevel(LevelOccupation.JUNIOR);
        Occupation gerente = new Occupation();
        gerente.setName("Gerente de TI");
        gerente.setDescription("Gestão da equipe");
        gerente.setTypes(TypeJobLevel.CONCURSADO);
        gerente.setLevel(LevelOccupation.SENIOR);
        Occupation assistente = new Occupation();
        assistente.setName("Assistente Administrativo");
        assistente.setDescription("Suporte administrativo");
        assistente.setTypes(TypeJobLevel.TERCEIRIZADO);
        assistente.setLevel(LevelOccupation.JUNIOR);
        repository.saveAll(List.of(analista, gerente, assistente));
        ctx.analista = analista;
        ctx.gerente = gerente;
        ctx.assistente = assistente;
    }
}

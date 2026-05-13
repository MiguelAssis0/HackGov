package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ApprovalSector {

    REQUISICAO_SECRETARIO(1, "Homologação da Requisição"),
    ANALISE_COMPRAS(2, "Homologação da Área de Compras"),
    DECLARACAO_PAGAMENTO(3, "Autorização de Pagamento"),
    PRESTACAO_CONTAS(4, "Homologação da Prestação de Contas");

    private final int id;
    private final String description;

    ApprovalSector(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static ApprovalSector fromId(int id) {
        return Arrays.stream(values())
                .filter(stage -> stage.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid ApprovalStage id: " + id
                        )
                );
    }
}
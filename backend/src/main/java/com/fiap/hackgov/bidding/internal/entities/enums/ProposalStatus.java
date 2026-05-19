package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum ProposalStatus {

    SUBMITTED("Proposta enviada"),
    UNDER_REVIEW("Em análise"),
    CLASSIFIED("Classificada"),
    DISQUALIFIED("Desclassificada"),
    IMPUGNED("Impugnada"),
    WINNER("Vencedora"),
    REJECTED("Rejeitada");

    private final String description;

    ProposalStatus(String description) {
        this.description = description;
    }
}
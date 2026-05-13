package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum HistoryEventType {

    REQUISITION_CREATED("Requisição criada"),

    STAGE_SENT("Encaminhamento de etapa"),

    APPROVAL_REQUESTED("Homologação solicitada"),

    APPROVED("Homologação aprovada"),

    REJECTED("Homologação reprovada"),

    COMMENT_ADDED("Comentário registrado");

    private final String description;

    HistoryEventType(String description) {
        this.description = description;
    }

}
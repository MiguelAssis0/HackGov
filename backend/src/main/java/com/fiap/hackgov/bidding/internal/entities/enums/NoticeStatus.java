package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum NoticeStatus {

    DRAFT("Rascunho"),
    UNDER_REVIEW("Em revisão"),
    PUBLISHED("Publicado"),
    IMPUGNED("Impugnado"),
    SUSPENDED("Suspenso"),
    CLOSED("Encerrado"),
    CANCELED("Cancelado");

    private final String description;

    NoticeStatus(String description) {
        this.description = description;
    }
}
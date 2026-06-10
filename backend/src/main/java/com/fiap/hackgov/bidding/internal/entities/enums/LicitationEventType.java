package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LicitationEventType {

    PROCESS_CREATED("Processo licitatório criado"),

    NOTICE_CREATED("Edital criado"),

    STATUS_CHANGED("Status do processo alterado"),

    NOTICE_PUBLISHED("Edital publicado"),

    CONTRACT_CREATED("Contrato criado"),

    PROCESS_OPENED("Processo aberto"),

    PROCESS_FINISHED("Processo finalizado"),

    PROCESS_CANCELED("Processo cancelado");

    private final String description;
}

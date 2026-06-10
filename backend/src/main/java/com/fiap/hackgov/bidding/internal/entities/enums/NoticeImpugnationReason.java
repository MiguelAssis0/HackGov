package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum NoticeImpugnationReason {

    RESTRICTIVE_REQUIREMENT("Exigência restritiva"),
    ILLEGAL_CLAUSE("Cláusula ilegal"),
    TECHNICAL_ERROR("Erro técnico"),
    DEADLINE_IRREGULARITY("Irregularidade de prazo"),
    OBJECT_DESCRIPTION_ERROR("Erro na descrição do objeto"),
    OTHER("Outro");

    private final String description;

    NoticeImpugnationReason(String description) {
        this.description = description;
    }
}

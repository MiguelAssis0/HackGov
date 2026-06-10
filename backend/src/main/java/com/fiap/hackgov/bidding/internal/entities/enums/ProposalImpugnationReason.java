package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum ProposalImpugnationReason {

    MISSING_DOCUMENTATION("Documentação incompleta"),
    INVALID_CERTIFICATE("Certidão inválida"),
    TAX_IRREGULARITY("Irregularidade fiscal"),
    TECHNICAL_NONCOMPLIANCE("Não conformidade técnica"),
    DEADLINE_VIOLATION("Descumprimento de prazo"),
    PRICE_INEXEQUIBLE("Preço inexequível"),
    OTHER("Outro");

    private final String description;

    ProposalImpugnationReason(String description) {
        this.description = description;
    }
}

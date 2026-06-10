package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum ImpugnationStatus {

    PENDING("Pendente"),
    ACCEPTED("Deferida"),
    REJECTED("Indeferida");

    private final String description;

    ImpugnationStatus(String description) {
        this.description = description;
    }
}
package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ApprovalStatus {

    PENDENTE(1, "Pendente"),
    APROVADO(2, "Aprovado"),
    REPROVADO(3, "Reprovado");

    private final int id;
    private final String description;

    ApprovalStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static ApprovalStatus fromId(int id) {
        return Arrays.stream(values())
                .filter(status -> status.getId() == id)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid ApprovalStatus id: " + id
                        )
                );
    }
}

package com.fiap.hackgov.bidding.internal.entities.enums;

public enum RequestStatus {

    CADASTRADA(1, "Cadastrada"),
    APROVADA(2, "Aprovada"),
    CORRECAO(3, "Correção Necessária"),
    REPROVADA(4, "Reprovada"),
    CANCELADA(5, "Cancelada");

    private final int id;
    private final String description;

    RequestStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public static RequestStatus fromId(int id) {
        for (RequestStatus status : values()) {
            if (status.getId() == id) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid RequestStatus id: " + id);
    }
}

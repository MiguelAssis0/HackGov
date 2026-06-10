package com.fiap.hackgov.auth.internal.entities.enums;

public enum Roles {
    //Fazer id da role
    //ex: EMPLOYEE -> 1
    EMPLOYEE(1),
    CITIZEN(2),
    ADMIN(3);

    private final int id;

    Roles(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

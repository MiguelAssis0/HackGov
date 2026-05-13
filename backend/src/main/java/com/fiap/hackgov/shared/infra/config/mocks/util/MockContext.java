package com.fiap.hackgov.shared.infra.config.mocks.util;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Occupation;
import com.fiap.hackgov.cityhall_management.internal.entities.State;

public class MockContext {

    public State sp;
    public State rj;

    public CityHall cityHallSP;
    public CityHall cityHallRJ;

    public Occupation analista;
    public Occupation gerente;
    public Occupation assistente;

    public Employee admin;
    public Employee maria;
    public Employee joao;
    public Employee carlos;
}
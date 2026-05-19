package com.fiap.hackgov.shared.infra.config.mocks.util;

import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.cityhall_management.internal.entities.*;

public class MockContext {

    public Sector tiSector;
    public Sector comprasSector;
    public Sector financeiroSector;

    public State sp;
    public State rj;

    public Requisition requisitionPendingApproval;
    public Requisition requisitionInLicitation;

    public LicitationProcess licitationProcess;

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
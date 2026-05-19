package com.fiap.hackgov.shared.infra.config.mocks.util;

import com.fiap.hackgov.bidding.internal.entities.LicitationProcess;
import com.fiap.hackgov.bidding.internal.entities.Requisition;
import com.fiap.hackgov.bidding.internal.entities.Supplier;
import com.fiap.hackgov.cityhall_management.internal.entities.*;

public class MockContext {

    public Sector tiSector;
    public Sector comprasSector;
    public Sector financeiroSector;
    public Sector contratosSector;
    public Sector juridicoSector;

    public State sp;
    public State rj;

    public Requisition requisitionPendingApproval;
    public Requisition requisitionInLicitation;
    public Requisition requisitionFinishedLicitation;

    public LicitationProcess licitationProcess;
    public LicitationProcess finishedLicitationProcess;

    public Supplier supplierWinner;

    public CityHall cityHallSP;
    public CityHall cityHallRJ;

    public Occupation analista;
    public Occupation gerente;
    public Occupation assistente;
    public Occupation administradorMunicipal;
    public Occupation agenteCompras;
    public Occupation pregoeiro;
    public Occupation analistaFinanceiro;
    public Occupation gestorContratos;
    public Occupation assessorJuridico;

    public Employee admin;
    public Employee maria;
    public Employee joao;
    public Employee carlos;
    public Employee ana;
    public Employee roberto;
    public Employee fernanda;
    public Employee paula;
    public Employee ricardo;
    public Employee juliana;
    public Employee bruno;
    public Employee patricia;
    public Employee lucas;
}

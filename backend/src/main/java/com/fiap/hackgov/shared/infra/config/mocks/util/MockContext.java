package com.fiap.hackgov.shared.infra.config.mocks.util;

import com.fiap.hackgov.bidding.internal.entities.*;
import com.fiap.hackgov.cityhall_management.internal.entities.*;

public class MockContext {

    public Sector tiSectorsSP;
    public Sector comprasSectorSP;
    public Sector financeiroSectorSP;
    public Sector contratosSectorSP;
    public Sector juridicoSectorSP;
    public Sector tiSectorRJ;
    public Sector comprasSectorRJ;
    public Sector financeiroSectorRJ;
    public Sector contratosSectorRJ;
    public Sector juridicoSectorRJ;

    public State sp;
    public State rj;

    public Requisition requisitionPendingApproval;
    public Requisition requisitionInLicitation;
    public Requisition requisitionFinishedLicitation;
    public Requisition requisitionInPaymentStage;

    public LicitationProcess licitationProcess;
    public LicitationProcess finishedLicitationProcess;
    public LicitationProcess paymentStageLicitationProcess;

    public Supplier supplierWinner;
    public Supplier paymentStageSupplier;

    public Contract paymentStageContract;
    public ExecutionOrder paymentStageExecutionOrder;
    public Commitment paymentStageCommitment;
    public PaymentDeclaration paymentStageDeclaration;
    public Payment paymentStagePayment;

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

package com.fiap.hackgov.bidding.internal.entities.enums;

public enum LicitationType {

    CHAMADA_PUBLICA,
    CONCORRENCIA_PUBLICA,
    CONVITES_NP,
    EXTRATO_FOMENTO,
    PREGAO_ELETRONICO,
    PREGAO_PRESENCIAL,
    TOMADA_PRECOS,
    OUTROS,

    // Mantidos para compatibilidade com processos já persistidos.
    CONCORRENCIA,
    DISPENSA,
    INEXIGIBILIDADE
}

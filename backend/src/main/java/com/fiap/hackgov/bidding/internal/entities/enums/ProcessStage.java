package com.fiap.hackgov.bidding.internal.entities.enums;

import lombok.Getter;

@Getter
public enum ProcessStage {

    REQUISICAO_CADASTRADA(1, "Requisição Cadastrada"),
    HOMOLOGACAO_SECRETARIO(2, "Homologação do Secretário"),
    RECEBIMENTO_COMPRAS(3, "Recebimento pela Área de Compras"),
    ANALISE_REQUISICAO(4, "Análise da Requisição"),
    HOMOLOGACAO_COMPRAS(5, "Homologação da Área de Compras"),
    COMPOSICAO_PROCESSO(6, "Composição do Processo"),
    PROCESSO_LICITATORIO(7, "Processo Licitatório"),
    SETOR_CONTRATOS(8, "Setor de Contratos"),
    INICIO_SERVICOS(9, "Início dos Serviços"),
    EMISSAO_EMPENHO(10, "Emissão de Empenho"),
    DECLARACAO_PAGAMENTO(11, "Declaração para Pagamento"),
    EXECUCAO_PAGAMENTO(12, "Execução do Pagamento"),
    PRESTACAO_CONTAS(13, "Prestação de Contas"),
    ANALISE_PRESTACAO_CONTAS(14, "Análise da Prestação de Contas"),
    HOMOLOGACAO_PRESTACAO_CONTAS(15, "Homologação da Prestação de Contas");

    private final int step;
    private final String description;

    ProcessStage(int step, String description) {
        this.step = step;
        this.description = description;
    }
}

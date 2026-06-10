package com.fiap.hackgov.shared.infra.config.mocks.task;

import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskMock {

    private final TaskReporitory repository;

    public void load(MockContext ctx) {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = createTask(
                "Revisar cronograma de modernizacao do portal",
                "Consolidar ajustes tecnicos com a equipe de TI e validar a janela de implantacao do novo portal de servicos.",
                ctx.joao,
                ctx.admin,
                ctx.tiBoardSP,
                now.minusDays(2),
                now.plusDays(3)
        );
        Task task2 = createTask(
                "Conferir mapa comparativo da compra de notebooks",
                "Validar fornecedores habilitados, documentacao e justificativa da melhor proposta antes da homologacao.",
                ctx.ana,
                ctx.maria,
                ctx.comprasBoardSP,
                now.minusDays(4),
                now.plusDays(1)
        );
        Task task3 = createTask(
                "Atualizar previsao de empenho do convenio escolar",
                "Recalcular saldo disponivel e registrar observacoes para o fechamento da dotacao do processo.",
                ctx.roberto,
                ctx.admin,
                ctx.financeiroBoardSP,
                now.minusDays(6),
                now.minusDays(1)
        );
        Task task4 = createTask(
                "Preparar minuta do termo aditivo de manutencao",
                "Ajustar clausulas de vigencia e indicadores de desempenho para envio ao juridico.",
                ctx.fernanda,
                ctx.admin,
                ctx.contratosBoardSP,
                now.plusDays(1),
                now.plusDays(5)
        );
        Task task5 = createTask(
                "Emitir parecer sobre contratacao emergencial de limpeza",
                "Analisar riscos legais e apontar ressalvas para subsidiar a decisao do gabinete.",
                ctx.paula,
                ctx.admin,
                ctx.juridicoBoardSP,
                now.minusDays(1),
                now.plusDays(2)
        );

        Task task6 = createTask(
                "Mapear integracao do protocolo digital com arrecadacao",
                "Levantar dependencias tecnicas e estimar impacto para a proxima sprint de integracao.",
                ctx.carlos,
                ctx.carlos,
                ctx.tiBoardRJ,
                now.minusDays(3),
                now.plusDays(4)
        );
        Task task7 = createTask(
                "Validar termo de referencia da frota escolar",
                "Conferir criterios de julgamento, quantitativos e memoria de calculo antes da publicacao.",
                ctx.juliana,
                ctx.carlos,
                ctx.comprasBoardRJ,
                now.minusDays(2),
                now.plusDays(2)
        );
        Task task8 = createTask(
                "Fechar conciliacao de repasses do programa de saude",
                "Ajustar divergencias entre empenho, liquidacao e pagamento para envio ao controle interno.",
                ctx.bruno,
                ctx.carlos,
                ctx.financeiroBoardRJ,
                now.minusDays(5),
                now.minusHours(6)
        );
        Task task9 = createTask(
                "Revisar matriz de riscos do contrato de iluminacao",
                "Atualizar responsabilidades contratuais e plano de contingencia para apresentacao ao gestor.",
                ctx.patricia,
                ctx.carlos,
                ctx.contratosBoardRJ,
                now.plusHours(8),
                now.plusDays(6)
        );
        Task task10 = createTask(
                "Analisar defesa administrativa do fornecedor autuado",
                "Verificar fundamentacao juridica e redigir recomendacao para decisao da autoridade competente.",
                ctx.lucas,
                ctx.carlos,
                ctx.juridicoBoardRJ,
                now.minusDays(1),
                now.plusDays(1)
        );

        repository.saveAll(List.of(task1, task2, task3, task4, task5, task6, task7, task8, task9, task10));
    }

    private Task createTask(String title, String description, Employee responsible, Employee createdBy, Board board,
                            LocalDateTime startDate, LocalDateTime endDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setResponsible(responsible);
        task.setCreatedBy(createdBy);
        task.setBoard(board);
        task.setStartDate(startDate);
        task.setEndDate(endDate);
        return task;
    }
}

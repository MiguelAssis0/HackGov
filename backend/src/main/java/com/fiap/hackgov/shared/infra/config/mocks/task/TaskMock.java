package com.fiap.hackgov.shared.infra.config.mocks.task;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMock {
    private static final String PREDICTIVE_PREFIX = "MOCK-PREDICTIVE-";

    private final TaskReporitory repository;
    private final BoardRepository boardRepository;
    private final EmployeeRepository employeeRepository;

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

        List<Task> tasks = new ArrayList<>(List.of(task1, task2, task3, task4, task5, task6, task7, task8, task9, task10));
        tasks.addAll(predictiveTasks(ctx, now));
        repository.saveAll(tasks);
    }

    public void loadPredictiveData(CityHall cityHall) {
        if (repository.existsByProtocolStartingWithAndBoard_CityHall_Id(PREDICTIVE_PREFIX, cityHall.getId())) return;

        List<Board> boards = boardRepository.findAllByCityHall_Id(cityHall.getId());
        Map<String, Board> boardsByName = boards.stream().collect(Collectors.toMap(Board::getName, Function.identity()));
        MockContext ctx = new MockContext();
        ctx.admin = employee("admin.sp@prefeitura.gov.br");
        ctx.joao = employee("joao@sp.gov.br");
        ctx.maria = employee("maria@sp.gov.br");
        ctx.ana = employee("ana.compras@sp.gov.br");
        ctx.roberto = employee("roberto.financeiro@sp.gov.br");
        ctx.fernanda = employee("fernanda.contratos@sp.gov.br");
        ctx.paula = employee("paula.juridico@sp.gov.br");
        ctx.tiBoardSP = board(boardsByName, "Quadro TI SP");
        ctx.comprasBoardSP = board(boardsByName, "Quadro Compras SP");
        ctx.financeiroBoardSP = board(boardsByName, "Quadro Financeiro SP");
        ctx.contratosBoardSP = board(boardsByName, "Quadro Contratos SP");
        ctx.juridicoBoardSP = board(boardsByName, "Quadro Juridico SP");
        repository.saveAll(predictiveTasks(ctx, LocalDateTime.now()));
    }

    private List<Task> predictiveTasks(MockContext ctx, LocalDateTime now) {
        return List.of(
                completedTask("Publicar painel de transparencia", ctx.joao, ctx.admin, ctx.tiBoardSP, completedAt(now, 8, 8), 18, "01"),
                completedTask("Revisar documentos da compra anual", ctx.maria, ctx.admin, ctx.comprasBoardSP, completedAt(now, 7, 12), 24, "02"),
                completedTask("Atualizar relatorio de arrecadacao", ctx.roberto, ctx.admin, ctx.financeiroBoardSP, completedAt(now, 6, 9), 20, "03"),
                completedTask("Validar medicao do contrato de limpeza", ctx.fernanda, ctx.admin, ctx.contratosBoardSP, completedAt(now, 5, 17), 32, "04"),
                completedTask("Emitir parecer do processo prioritario", ctx.paula, ctx.admin, ctx.juridicoBoardSP, completedAt(now, 4, 14), 28, "05"),
                completedTask("Implantar melhoria no portal", ctx.joao, ctx.admin, ctx.tiBoardSP, completedAt(now, 3, 6), 35, "06"),
                completedTask("Conferir propostas habilitadas", ctx.maria, ctx.admin, ctx.comprasBoardSP, completedAt(now, 2, 11), 38, "07"),
                completedTask("Fechar conciliacao mensal", ctx.roberto, ctx.admin, ctx.financeiroBoardSP, completedAt(now, 1, 15), 42, "08"),
                completedTask("Atualizar plano de fiscalizacao", ctx.fernanda, ctx.admin, ctx.contratosBoardSP, completedAt(now, 0, 1), 45, "09"),
                completedTask("Concluir analise de risco", ctx.paula, ctx.admin, ctx.juridicoBoardSP, completedAt(now, 0, 2), 30, "10"),
                completedTask("Corrigir integracao de servicos", ctx.joao, ctx.admin, ctx.tiBoardSP, completedAt(now, 0, 4), 48, "11"),
                completedTask("Homologar mapa comparativo", ctx.maria, ctx.admin, ctx.comprasBoardSP, completedAt(now, 0, 5), 52, "12"),
                completedTask("Registrar fechamento orcamentario", ctx.roberto, ctx.admin, ctx.financeiroBoardSP, completedAt(now, 0, 6), 40, "13"),
                completedTask("Revisar minuta contratual", ctx.fernanda, ctx.admin, ctx.contratosBoardSP, completedAt(now, 0, 7), 36, "14")
        );
    }

    private Task completedTask(String title, Employee responsible, Employee createdBy, Board board,
                               LocalDateTime completedAt, int points, String key) {
        Task task = createTask(title, "Dado demonstrativo para indicadores e análise preditiva.", responsible,
                createdBy, board, completedAt.minusDays(3), completedAt);
        task.setStatus(Task.Status.COMPLETED);
        task.setBusinessPoints(points);
        task.setCompletedAt(completedAt);
        task.setProtocol(PREDICTIVE_PREFIX + key);
        return task;
    }

    private LocalDateTime completedAt(LocalDateTime now, int monthsAgo, int day) {
        LocalDateTime base = now.minusMonths(monthsAgo);
        int latestDay = monthsAgo == 0 ? now.getDayOfMonth() : base.toLocalDate().lengthOfMonth();
        return base.withDayOfMonth(Math.min(day, latestDay)).withHour(10).withMinute(0).withSecond(0).withNano(0);
    }

    private Employee employee(String email) {
        return employeeRepository.findByEmail(email).orElseThrow();
    }

    private Board board(Map<String, Board> boards, String name) {
        Board board = boards.get(name);
        if (board == null) throw new IllegalStateException("Mock de quadro não encontrado: " + name);
        return board;
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

package com.fiap.hackgov.shared.infra.config.mocks.task;

import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.cityhall_management.internal.entities.Sector;
import com.fiap.hackgov.cityhall_management.internal.repositories.EmployeeRepository;
import com.fiap.hackgov.cityhall_management.internal.repositories.SectorRepository;
import com.fiap.hackgov.shared.infra.config.mocks.util.MockContext;
import com.fiap.hackgov.tasks.internal.entities.Board;
import com.fiap.hackgov.tasks.internal.entities.Task;
import com.fiap.hackgov.tasks.internal.repositories.BoardRepository;
import com.fiap.hackgov.tasks.internal.repositories.TaskReporitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private static final String BULK_PREFIX = "MOCK-BULK-";

    private static final String[][] BULK_TITLES = {
            {"Digitalizar processos de alvará de construção", "Escanear, indexar e vincular os processos físicos de alvará ao protocolo digital."},
            {"Revisar folha de pagamento dos temporários", "Conferir rubricas, descontos e encargos dos contratos temporários do mês."},
            {"Atualizar cadastro de fornecedores habilitados", "Validar certidões vencidas e inativar cadastros irregulares."},
            {"Elaborar relatório de merenda escolar", "Consolidar consumo, estoque e prestação de contas da merenda por escola."},
            {"Vistoriar ponte do bairro rural", "Laudo técnico com fotos, medições e recomendação de intervenção."},
            {"Organizar campanha de vacinação antirrábica", "Cronograma por bairro, equipe volante e controle de doses aplicadas."},
            {"Revisar contrato de coleta de lixo", "Medir SLA de rotas, pesagem e aplicar glosas quando cabível."},
            {"Capacitar equipe no novo protocolo digital", "Turmas por setor com material impresso e avaliação de aproveitamento."},
            {"Levantar débitos de IPTU para cobrança", "Relatório de inadimplência por região para o programa de regularização."},
            {"Reformar telhado da escola municipal", "Orçamento, cronograma de obra e plano de aulas durante a reforma."},
            {"Auditar diárias e passagens do trimestre", "Amostrar processos, conferir comprovantes e relatar divergências."},
            {"Implantar ponto eletrônico nas secretarias", "Instalar equipamentos, cadastrar biometrias e treinar gestores."},
            {"Mapear nascentes para o plano de arborização", "Georreferenciar nascentes e priorizar áreas de plantio."},
            {"Revisar frota de ambulâncias", "Checklist mecânico, documentação e escala de manutenção preventiva."},
            {"Elaborar plano de contingência das chuvas", "Rotas de fuga, abrigos temporários e estoque de emergência."},
            {"Digitalizar acervo da biblioteca pública", "Catalogar obras raras e disponibilizar consulta online."},
            {"Negociar dívidas de precatórios", "Simular parcelamentos e submeter proposta ao jurídico."},
            {"Revisar iluminação pública do centro", "Trocar lâmpadas queimadas e medir economia com LED."},
            {"Cadastrar famílias no auxílio municipal", "Visitas domiciliares, comprovação de renda e inclusão no programa."},
            {"Reformar quadra poliesportiva", "Piso, tabelas, alambrado e iluminação noturna."},
            {"Atualizar plano diretor participativo", "Audiências públicas por região e consolidação das propostas."},
            {"Fiscalizar feira livre de domingo", "Alvarás das barracas, higiene e ordenamento do trânsito local."},
            {"Implantar coleta seletiva piloto", "Rota experimental em dois bairros com cooperativa parceira."},
            {"Revisar convênio do transporte escolar", "Quilometragem, lotação e pontualidade das linhas rurais."},
            {"Criar central de atendimento ao cidadão", "Guichê único com senhas, totens e pesquisa de satisfação."},
            {"Pavimentar rua do distrito industrial", "Drenagem, base, asfalto e sinalização horizontal."},
            {"Inventariar patrimônio da saúde", "Plaquetar equipamentos e baixar itens inservíveis."},
            {"Organizar festa junina municipal", "Palco, barracas, segurança e alvará do corpo de bombeiros."},
            {"Revisar estação de tratamento de água", "Análise laboratorial, dosagem de cloro e plano de manutenção."},
            {"Elaborar cartilha de IPTU verde", "Descontos para calçada acessível, captação de chuva e energia solar."},
    };

    private static final String[][] BULK_OPEN = {
            {"Concluir licitação da merenda", "Publicar edital, responder impugnações e homologar o pregão."},
            {"Reparar buracos na avenida principal", "Operação tapa-buraco com massa asfáltica e sinalização."},
            {"Implantar prontuário eletrônico no posto central", "Migrar fichas físicas e treinar recepção e enfermagem."},
            {"Responder auditoria do tribunal de contas", "Juntar documentos dos apontamentos e redigir defesa prévia."},
            {"Construir creche do bairro novo", "Fundação e alvenaria conforme cronograma da construtora."},
            {"Atualizar site da transparência", "Publicar receitas, despesas e salários no prazo legal."},
            {"Podar árvores da praça matriz", "Poda preventiva com interdição parcial e recolhimento de galhos."},
            {"Contratar médicos plantonistas", "Edital de credenciamento para cobrir férias e licenças."},
            {"Revisar guarda municipal", "Escala, armamento menos letal e curso de reciclagem."},
            {"Drenar rua alagadiça do jardim", "Galeria pluvial e bocas de lobo antes do período de chuvas."},
            {"Criar horta comunitária", "Preparar canteiros, doar mudas e organizar mutirão."},
            {"Modernizar iluminação do estádio", "Refletores de LED e gerador reserva para jogos noturnos."},
            {"Cadastrar artesãos na feira cultural", "Inscrições, curadoria e sorteio das barracas."},
            {"Reformar centro de zoonoses", "Baias, centro cirúrgico e sala de pós-operatório."},
            {"Implantar wi-fi na praça", "Link dedicado, totens e termo de uso."},
            {"Recapear estrada vicinal", "Cascalhamento e compactação dos trechos críticos."},
            {"Organizar mutirão de limpeza do rio", "Barcos, sacos e destinação de entulho com a cooperativa."},
            {"Criar programa de estágio municipal", "Convênio com escolas técnicas e bolsa-auxílio."},
            {"Revisar plano de cargos e salários", "Tabela, progressões e impacto orçamentário."},
            {"Instalar câmeras no centro", "Pontos estratégicos com monitoramento 24h."},
    };

    private final TaskReporitory repository;
    private final BoardRepository boardRepository;
    private final EmployeeRepository employeeRepository;
    private final SectorRepository sectorRepository;

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

    // ponytail: carga volumétrica idempotente p/ encher Tarefas/Gestão em qualquer prefeitura com quadros e equipe
    public void loadBulkDemoData(CityHall cityHall) {
        if (repository.existsByProtocolStartingWithAndBoard_CityHall_Id(BULK_PREFIX, cityHall.getId())) return;
        List<Board> boards = boardRepository.findAllByCityHall_Id(cityHall.getId());
        List<Employee> people = employeeRepository.findAllByCityHallId_IdAndStatusTrueOrderByFirstNameAscLastNameAsc(cityHall.getId());
        if (boards.isEmpty() || people.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < BULK_TITLES.length; i++) {
            Board board = boards.get(i % boards.size());
            Employee responsible = people.get((i * 5 + 1) % people.size());
            Employee creator = people.get((i * 3) % people.size());
            LocalDateTime doneAt = now.minusDays(6 + i * 5 + (i % 3)).withHour(10).withMinute(0).withSecond(0).withNano(0);
            Task task = createTask(BULK_TITLES[i][0], BULK_TITLES[i][1], responsible, creator, board,
                    doneAt.minusDays(2 + (i % 8)), doneAt);
            task.setStatus(Task.Status.COMPLETED);
            task.setBusinessPoints(8 + ((i * 13) % 51));
            task.setCompletedAt(doneAt);
            task.setProtocol(BULK_PREFIX + String.format("%03d", i + 1));
            tasks.add(task);
        }
        for (int i = 0; i < BULK_OPEN.length; i++) {
            int k = BULK_TITLES.length + i;
            Board board = boards.get(k % boards.size());
            Employee responsible = people.get((k * 5 + 1) % people.size());
            Employee creator = people.get((k * 3) % people.size());
            Task.Status[] flow = {Task.Status.TODO, Task.Status.IN_PROGRESS, Task.Status.IN_REVIEW};
            boolean overdue = i % 4 == 3;
            LocalDateTime end = overdue
                    ? now.minusDays(1 + (i % 5)).withHour(17).withMinute(0).withSecond(0).withNano(0)
                    : now.plusDays(2 + ((i * 3) % 12)).withHour(17).withMinute(0).withSecond(0).withNano(0);
            Task task = createTask(BULK_OPEN[i][0], BULK_OPEN[i][1], responsible, creator, board,
                    now.minusDays(i % 6).withHour(9).withMinute(0).withSecond(0).withNano(0), end);
            task.setStatus(overdue ? Task.Status.IN_PROGRESS : flow[i % flow.length]);
            Task.Priority[] priorities = {Task.Priority.NORMAL, Task.Priority.HIGH, Task.Priority.NORMAL, Task.Priority.LOW};
            task.setPriority(i % 9 == 8 ? Task.Priority.URGENT : priorities[i % priorities.length]);
            task.setBusinessPoints(5 + ((i * 7) % 40));
            task.setProtocol(BULK_PREFIX + "ABERTA-" + String.format("%02d", i + 1));
            tasks.add(task);
        }
        repository.saveAll(tasks);
    }

    private static final String[][] CULTURA_DONE = {
            {"Realizar mostra de cinema ao ar livre", "Sessões gratuitas na praça central com curadoria de artistas locais."},
            {"Restaurar painel do centro cultural", "Recuperação da fachada e do painel artístico com laudo técnico."},
            {"Cadastrar artistas locais no mapa cultural", "Mapear artesãos, músicos e grupos para o calendário oficial."},
    };

    private static final String[][] CULTURA_OPEN = {
            {"Publicar edital de fomento ao artesanato", "Regras, prazos e contrapartidas para feirantes e artesãos."},
            {"Organizar semana do livro na biblioteca", "Saraus, troca de livros e contação de histórias por bairro."},
    };

    private static final String[][] EDUCACAO_DONE = {
            {"Entregar uniformes da rede municipal", "Distribuição por escola com controle de tamanhos e recibo."},
            {"Implantar cantinho de leitura nas creches", "Acervo inicial, tapetes e rotina de leitura com as cuidadoras."},
            {"Realizar olimpíada de matemática entre escolas", "Provas por série, premiação e relatório de desempenho."},
    };

    private static final String[][] EDUCACAO_OPEN = {
            {"Criar cursinho preparatório municipal", "Turmas noturnas com professores voluntários e material apostilado."},
            {"Reformar refeitório da escola do centro", "Piso, ventilação e mobiliário novo antes do próximo semestre."},
    };

    // ponytail: setor criado pela tela (ou bulk antigo) fica sem quadro/tarefas — preenche por setor, idempotente por quadro
    @Transactional
    public void loadNewSectorDemoData(CityHall cityHall) {
        List<Board> boards = boardRepository.findAllByCityHall_Id(cityHall.getId());
        List<Employee> people = employeeRepository.findAllByCityHallId_IdAndStatusTrueOrderByFirstNameAscLastNameAsc(cityHall.getId());
        if (people.isEmpty()) return;
        LocalDateTime now = LocalDateTime.now();
        LocalDate monthStart = now.toLocalDate().withDayOfMonth(1);
        List<Task> tasks = new ArrayList<>();
        for (Sector sector : sectorRepository.findAllByCityHall_Id(cityHall.getId())) {
            if (!sector.getName().equals("Cultura") && !sector.getName().equals("Educação")) continue;
            Board board = boards.stream().filter(b -> b.getSector() != null && b.getSector().getId().equals(sector.getId())).findFirst().orElseGet(() -> {
                Board value = new Board();
                value.setName("Quadro " + sector.getName());
                value.setCityHall(cityHall);
                value.setSector(sector);
                boards.add(boardRepository.save(value));
                return boards.get(boards.size() - 1);
            });
            // ponytail: seed antigo gerou concluídas fora do mês atual (invisíveis na Gestão) — regenera nesse caso
            List<Task> seed = repository.findByBoard_IdAndProtocolStartingWith(board.getId(), "MOCK-SETOR-");
            boolean healthy = seed.stream().anyMatch(task -> task.getStatus() == Task.Status.COMPLETED
                    && task.getCompletedAt() != null && !task.getCompletedAt().toLocalDate().isBefore(monthStart));
            if (healthy) continue;
            if (!seed.isEmpty()) repository.deleteAll(seed);
            boolean cultura = sector.getName().equals("Cultura");
            String[][] done = cultura ? CULTURA_DONE : EDUCACAO_DONE;
            String[][] open = cultura ? CULTURA_OPEN : EDUCACAO_OPEN;
            String tag = cultura ? "CUL" : "EDU";
            Employee responsible = people.stream().filter(e -> e.getSectorId() != null && e.getSectorId().getId().equals(sector.getId())).findFirst().orElse(people.get(0));
            Employee creator = people.get(0);
            for (int i = 0; i < done.length; i++) {
                // ponytail: Gestão filtra por período (padrão = mês atual) — ancora no mês corrente
                LocalDate day = now.toLocalDate().withDayOfMonth(1).plusDays(1L + i * 3L);
                if (day.isAfter(now.toLocalDate())) day = now.toLocalDate();
                LocalDateTime doneAt = day.atTime(10, 0);
                Task task = createTask(done[i][0], done[i][1], responsible, creator, board, doneAt.minusDays(5), doneAt);
                task.setStatus(Task.Status.COMPLETED);
                task.setBusinessPoints(15 + i * 9);
                task.setCompletedAt(doneAt);
                task.setProtocol("MOCK-SETOR-" + tag + "-" + (i + 1));
                tasks.add(task);
            }
            for (int i = 0; i < open.length; i++) {
                Task task = createTask(open[i][0], open[i][1], responsible, creator, board,
                        now.minusDays(i).withHour(9).withMinute(0).withSecond(0).withNano(0),
                        now.plusDays(5 + i * 4).withHour(17).withMinute(0).withSecond(0).withNano(0));
                task.setStatus(i == 0 ? Task.Status.TODO : Task.Status.IN_PROGRESS);
                task.setBusinessPoints(10 + i * 7);
                task.setProtocol("MOCK-SETOR-" + tag + "-ABERTA-" + (i + 1));
                tasks.add(task);
            }
        }
        if (!tasks.isEmpty()) repository.saveAll(tasks);
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

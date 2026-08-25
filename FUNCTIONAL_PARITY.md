# Mapa de paridade funcional

Fonte funcional revisada: `../Sistema-ERP-Municipal` (Django). Destino: este projeto Spring Boot + React.

## Equivalencias principais

| Django | Spring Boot / React | Situacao inicial |
| --- | --- | --- |
| `accounts.User`, `Funcionario`, `UserSession` | `User`, `Employee`, JWT e perfil | Parcial: sem sessoes/dispositivos e com autorizacao HTTP permissiva |
| `municipios.Prefeitura`, `Setor`, `Cargo` | `CityHall`, `Sector`, `Occupation` | Parcial: faltam atualizacao, inativacao e parte das constraints |
| `modulos.Ferramenta*`, `PermissaoFerramenta` | `Tools`, `Permissions*`, telas de ferramentas/acesso | Incompleto: configuracao e permissoes no React eram locais |
| `tarefas.*` | `Task`, `Board`, `TasksPage` | Incompleto: apenas CRUD de tarefa com um responsavel |
| `agenda.Evento` | inexistente | Ausente |
| `caixa_entrada.EntradaCaixa` | inexistente | Ausente |
| `documentos.DocumentoCompartilhado`, `DocumentoDestino` | inexistente | Ausente |
| `clientes.Cliente`, `AtendimentoCliente` | inexistente | Ausente |
| `patrulha_agricola.*` | inexistente | Ausente |
| `licitacoes.*` | `bidding.*`, `ProcessesPage` | Existe com fluxo proprio mais amplo; faltam isolamento uniforme, documentos preparatorios e caixa integrada |
| `gestao` | `ManagementPage` | Incompleto: indicadores eram majoritariamente simulados |
| `spreadsheet_import.*` | inexistente | Ausente |
| `auditoria.EventoAuditoria` | logs estruturados sem persistencia consultavel | Incompleto |
| `comunicacao.*` | `messages.*`, WebSocket, `Messages` | Parcial: chat existe; faltam anexos/documentos e leitura equivalente |
| `two_factor.VerificationCode` | `TwoFactorCode`, fluxo de login | Parcialmente equivalente |
| `dashboard.Favorito` | inexistente | Ausente |

## Contratos funcionais da referencia

- Todo dado operacional pertence a uma prefeitura ativa; usuario comum fica restrito ao proprio setor quando a ferramenta assim exige.
- Ferramentas obrigatorias nao podem ser desativadas. Ferramentas restritas exigem regra explicita, e regra individual prevalece sobre setor/cargo.
- Tarefas possuem status, prioridade, prazo, valor publico, responsaveis multiplos, historico, comentarios, checklist, anexos, apontamentos de tempo e demandas entre setores.
- Agenda agrega eventos multidiarios e prazos de tarefas acessiveis, permite vinculo apenas com tarefa da mesma prefeitura e aparece no dashboard/navegacao.
- Caixa de entrada aceita destino pessoal ou setorial, leitura, filtros, atribuicao e conclusao; tarefas, documentos e licitacoes geram entradas automaticamente.
- Documentos suportam multiplos destinos, metadados documentais, pasta de setor, encaminhamento, upload privado e assinatura real ou homologacao claramente sem validade juridica.
- Clientes sao unicos por CPF dentro da prefeitura; visualizadores recebem dados mascarados. Patrulha calcula valor por horas, trata doacao, expira pagamento apos seis meses e controla horimetro.
- Importacao aceita CSV/XLSX, preview, mapeamento, validacao, modos criar/atualizar/upsert, historico e auditoria.
- Auditoria e append-only, isolada por prefeitura, exportavel apenas com permissao e encadeada por hash.

## Gaps de seguranca encontrados no destino

- A configuracao inicial usava `anyRequest().permitAll()`. Muitos controllers dependiam apenas de checagens locais de principal.
- Diversos `findAll()` e `findById()` do modulo de compras nao filtravam prefeitura.
- `EmployeeController.getEmployeeById` nao recebia principal e o cadastro de funcionario estava publico.
- O frontend simulava troca de prefeitura apenas no `localStorage`; isso nao altera o escopo autenticado do backend.
- Ferramentas, permissoes, cargos e parte dos indicadores usavam fallback local silencioso, podendo aparentar sucesso sem persistencia.

## Ordem de implementacao

1. Endurecer autenticacao e uniformizar escopo por prefeitura/setor.
2. Completar tarefas e criar agenda/caixa de entrada, pois alimentam dashboard e gestao.
3. Persistir ferramentas/permissoes e remover fluxos locais equivalentes.
4. Adicionar documentos, clientes e patrulha agricola.
5. Adicionar importacao, auditoria consultavel, favoritos e sessoes.
6. Revisar licitacoes e chat contra isolamento, uploads e eventos automaticos.
7. Integrar todas as paginas React e executar testes/builds.

## Estado apos a implementacao desta revisao

Concluidos no destino:

- autenticacao obrigatoria para toda a API operacional e resposta HTTP 403 uniforme;
- agenda mensal, eventos multidiarios, proximos eventos e vinculo seguro com tarefas;
- caixa de entrada pessoal/setorial com leitura, assuncao, conclusao e notificacoes de tarefa;
- tarefas com status e prioridade persistidos, multiplos responsaveis, protocolo, valor publico, resultado esperado, comentarios, checklist, anexos, cronometro, apontamento manual e demandas entre setores;
- clientes municipais com CPF validado e unico por prefeitura, campos sensiveis criptografados/mascarados e historico de atendimento;
- patrulha agricola com catalogos, solicitacoes, calculo por hora, doacao, validade de pagamento, comprovante e controle operacional;
- documentos privados/compartilhados com busca, upload, geracao, encaminhamento por destinatario, download autenticado e homologacao inequivocamente sem validade juridica;
- auditoria persistente append-only, isolada por prefeitura e encadeada por SHA-256;
- indicadores de gestao calculados de tarefas, prazos, conclusoes e servidores reais;
- configuracao persistente de ferramentas, ferramentas obrigatorias, restricao por regra, permissoes por funcionario/setor/cargo e favoritos por servidor.
- importacao CSV/XLSX de ate 5 MB com preview, mapeamento, validacao por linha, modos criar/atualizar/upsert, historico, contadores e auditoria;
- sessoes autenticadas com identificacao de dispositivo/navegador/SO/IP, rotacao segura do refresh token e revogacao individual;
- anexos privados no chat com validacao de participante, limite de tamanho, bloqueio de executaveis e download autenticado;
- isolamento de todos os agregados de licitacao por prefeitura, inclusive cargas por chave, com documentos do processo e eventos automaticos na caixa de entrada;
- migrations Flyway versionadas, baseline seguro para bancos existentes, validacao de schema em producao e perfil de desenvolvimento preservado.

Dependencia externa, fora da paridade executavel local:

- assinatura remota juridicamente valida depende da contratacao, credenciais e homologacao de um PSC/GOV.BR. A mesma fronteira existe na referencia: o fluxo provider-neutral e a homologacao claramente sem validade juridica estao reproduzidos, mas producao permanece desabilitada ate existir um provedor real.

Este documento registra o mapa auditavel da migracao. A revisao final incluiu testes automatizados, build das duas camadas, migracao sobre PostgreSQL vazio e percurso visual dos fluxos principais em Firefox headless, nas resolucoes desktop e movel.

import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api } from "../services/api.js";
import { TaskDetailPanel } from "../components/TaskDetailPanel.jsx";

const emptyForm = {
  title: "",
  description: "",
  boardId: "",
  responsibleId: "",
  startDate: "",
  endDate: "",
  status: "TODO",
  priority: "NORMAL",
  businessPoints: 0,
  protocol: "",
  expectedResult: "",
};

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content || payload?.items || [];
}

function employeeName(employee) {
  const fullName = [employee?.firstName, employee?.lastName].filter(Boolean).join(" ");
  return employee?.fullName || employee?.name || employee?.nome || fullName || employee?.email || "Servidor";
}

function employeeInitials(employee) {
  return employeeName(employee)
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

function sectorName(board) {
  return board?.sector?.name || board?.sectorId?.name || board?.name || "Setor sem nome";
}

function boardOptionLabel(board) {
  const boardTitle = board?.name || "";
  const sectorTitle = board?.sector?.name || board?.sectorId?.name || "";

  if (sectorTitle && boardTitle && sectorTitle !== boardTitle) {
    return `${sectorTitle} - ${boardTitle}`;
  }

  return sectorTitle || boardTitle || "Setor sem nome";
}

function boardName(board) {
  return board?.name || sectorName(board);
}

function taskBoardKey(task) {
  return task?.board?.id || task?.boardId || null;
}

function taskResponsibleKey(task) {
  return task?.responsible?.id || task?.responsibleId || null;
}

function sectorKey(value) {
  return value?.sector?.id || value?.sectorId?.id || value?.id || null;
}

function taskSectorKey(task) {
  return sectorKey(task?.board);
}

function taskStatus(task) {
  const persisted = {
    TODO: { key: "planejada", label: "A fazer", icon: "bi-circle" },
    IN_PROGRESS: { key: "andamento", label: "Em andamento", icon: "bi-play-circle-fill" },
    IN_REVIEW: { key: "revisao", label: "Em revisao", icon: "bi-eye-fill" },
    COMPLETED: { key: "concluida", label: "Concluida", icon: "bi-check-circle-fill" },
  }[task.status];
  if (persisted) return persisted;
  const now = new Date();
  const start = task.startDate ? new Date(task.startDate) : null;
  const end = task.endDate ? new Date(task.endDate) : null;

  if (end && end < now) {
    return { key: "atrasada", label: "Atrasada", icon: "bi-exclamation-circle-fill" };
  }

  if (start && start > now) {
    return { key: "planejada", label: "Planejada", icon: "bi-calendar-event-fill" };
  }

  return { key: "andamento", label: "Em andamento", icon: "bi-play-circle-fill" };
}

function dateLabel(value) {
  if (!value) return "Sem prazo";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Sem prazo";
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function toDatetimeLocal(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return offsetDate.toISOString().slice(0, 16);
}

function normalizeLocalDate(value) {
  return value ? `${value}:00` : null;
}

function validateForm(form) {
  if (!form.title.trim() || !form.description.trim() || !form.boardId || !form.responsibleId) {
    return "Preencha titulo, descricao, setor e responsavel para salvar a tarefa.";
  }

  if (!form.startDate || !form.endDate) {
    return "Informe data de inicio e prazo para salvar a tarefa.";
  }

  const start = new Date(normalizeLocalDate(form.startDate));
  const end = new Date(normalizeLocalDate(form.endDate));

  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return "As datas informadas para a tarefa sao invalidas.";
  }

  if (end <= start) {
    return "O prazo da tarefa precisa ser posterior ao inicio.";
  }

  return null;
}

function mergeBoards(boards, tasks) {
  const map = new Map();

  boards.forEach((board) => {
    if (board?.id) map.set(board.id, board);
  });

  tasks.forEach((task) => {
    const board = task?.board;
    if (board?.id && !map.has(board.id)) {
      map.set(board.id, board);
    }
  });

  return Array.from(map.values());
}

function hydrateTasks(rawTasks, boards, employees) {
  const boardMap = new Map(boards.map((board) => [String(board.id), board]));
  const employeeMap = new Map(employees.map((employee) => [String(employee.id), employee]));

  return rawTasks.map((task) => ({
    ...task,
    board: task?.board || boardMap.get(String(task.boardId)) || null,
    responsible: task?.responsible || employeeMap.get(String(task.responsibleId)) || null,
  }));
}

function mergeSectors(sectors, boards, tasks) {
  const map = new Map();

  sectors.forEach((sector) => {
    if (sector?.id) map.set(sector.id, sector);
  });

  boards.forEach((board) => {
    const relatedSector = board?.sector || board?.sectorId;
    if (relatedSector?.id && !map.has(relatedSector.id)) {
      map.set(relatedSector.id, relatedSector);
    }
  });

  tasks.forEach((task) => {
    const relatedSector = task?.board?.sector || task?.board?.sectorId;
    if (relatedSector?.id && !map.has(relatedSector.id)) {
      map.set(relatedSector.id, relatedSector);
    }
  });

  return Array.from(map.values());
}

function TaskModal({ open, editingTask, boards, employees, form, saving, onClose, onChange, onSubmit }) {
  useEffect(() => {
    document.body.classList.toggle("modal-open", open);
    return () => document.body.classList.remove("modal-open");
  }, [open]);

  if (!open) return null;

  const canSubmit = boards.length > 0 && employees.length > 0;

  return (
    <div className="react-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="taskModalTitle">
      <div className="react-modal-card task-modal-card">
        <div className="task-modal-header">
          <div>
            <p className="section-label mb-1">Atribuicao entre setores</p>
            <h4 id="taskModalTitle" className="task-modal-title">
              {editingTask ? "Editar tarefa" : "Nova tarefa"}
            </h4>
          </div>
          <button type="button" className="btn-acao" aria-label="Fechar" onClick={onClose}>
            <i className="bi bi-x-lg"></i>
          </button>
        </div>

        <form className="task-form" onSubmit={onSubmit}>
          <div className="row g-3">
            <div className="col-12">
              <label className="field-label" htmlFor="taskTitle">
                Titulo da tarefa *
              </label>
              <input
                id="taskTitle"
                className="field-input"
                type="text"
                required
                placeholder="Ex: Conferir documentos do processo"
                value={form.title}
                onChange={(event) => onChange("title", event.target.value)}
              />
            </div>

            <div className="col-12">
              <label className="field-label" htmlFor="taskDescription">
                Descricao *
              </label>
              <textarea
                id="taskDescription"
                className="field-input task-textarea"
                required
                rows="4"
                placeholder="Descreva o que precisa ser feito e o contexto do encaminhamento."
                value={form.description}
                onChange={(event) => onChange("description", event.target.value)}
              ></textarea>
            </div>

            <div className="col-12 col-md-6">
              <label className="field-label" htmlFor="taskBoard">
                Setor de destino *
              </label>
              <select
                id="taskBoard"
                className="field-input"
                required
                value={form.boardId}
                onChange={(event) => onChange("boardId", event.target.value)}
              >
                <option value="">Selecionar setor...</option>
                {boards.map((board) => (
                  <option value={board.id} key={board.id}>
                    {boardOptionLabel(board)}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-12 col-md-6">
              <label className="field-label" htmlFor="taskResponsible">
                Responsavel *
              </label>
              <select
                id="taskResponsible"
                className="field-input"
                required
                value={form.responsibleId}
                onChange={(event) => onChange("responsibleId", event.target.value)}
              >
                <option value="">Selecionar servidor...</option>
                {employees.map((employee) => (
                  <option value={employee.id} key={employee.id}>
                    {employeeName(employee)}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-12 col-md-6">
              <label className="field-label" htmlFor="taskStart">
                Inicio
              </label>
              <input
                id="taskStart"
                className="field-input"
                type="datetime-local"
                value={form.startDate}
                onChange={(event) => onChange("startDate", event.target.value)}
              />
            </div>

            <div className="col-12 col-md-6">
              <label className="field-label" htmlFor="taskEnd">
                Prazo
              </label>
              <input
                id="taskEnd"
                className="field-input"
                type="datetime-local"
                value={form.endDate}
                onChange={(event) => onChange("endDate", event.target.value)}
              />
            </div>
            <div className="col-12 col-md-4">
              <label className="field-label" htmlFor="taskStatus">Status</label>
              <select id="taskStatus" className="field-input" value={form.status} onChange={(event) => onChange("status", event.target.value)}>
                <option value="TODO">A fazer</option><option value="IN_PROGRESS">Em andamento</option><option value="IN_REVIEW">Em revisao</option><option value="COMPLETED">Concluida</option>
              </select>
            </div>
            <div className="col-12 col-md-4">
              <label className="field-label" htmlFor="taskPriority">Prioridade</label>
              <select id="taskPriority" className="field-input" value={form.priority} onChange={(event) => onChange("priority", event.target.value)}>
                <option value="LOW">Baixa</option><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option>
              </select>
            </div>
            <div className="col-12 col-md-4">
              <label className="field-label" htmlFor="taskPoints">Valor publico</label>
              <input id="taskPoints" className="field-input" type="number" min="0" max="100" value={form.businessPoints} onChange={(event) => onChange("businessPoints", Number(event.target.value))} />
            </div>
            <div className="col-12 col-md-5">
              <label className="field-label" htmlFor="taskProtocol">Protocolo</label>
              <input id="taskProtocol" className="field-input" maxLength="60" value={form.protocol} onChange={(event) => onChange("protocol", event.target.value)} />
            </div>
            <div className="col-12 col-md-7">
              <label className="field-label" htmlFor="taskExpected">Resultado esperado</label>
              <input id="taskExpected" className="field-input" maxLength="5000" value={form.expectedResult} onChange={(event) => onChange("expectedResult", event.target.value)} />
            </div>
          </div>

          {!canSubmit && (
            <div className="auth-message error mt-3 mb-0">
              <i className="bi bi-exclamation-circle-fill"></i>
              Cadastre ao menos um setor/quadro e um funcionario no backend para salvar atribuicoes reais.
            </div>
          )}

          <div className="task-modal-actions">
            <button type="button" className="task-btn-muted" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary d-flex align-items-center gap-2" disabled={saving || !canSubmit}>
              <i className={`bi ${saving ? "bi-arrow-repeat" : "bi-send"}`}></i>
              {saving ? "Salvando..." : "Salvar tarefa"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function TasksPage() {
  const params = new URLSearchParams(window.location.search);
  const [tasks, setTasks] = useState([]);
  const [boards, setBoards] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [query, setQuery] = useState("");
  const [sectorFilter, setSectorFilter] = useState("todos");
  const [statusFilter, setStatusFilter] = useState("todos");
  const [modalOpen, setModalOpen] = useState(() => params.get("nova") === "1");
  const [editingTask, setEditingTask] = useState(null);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [detailTask, setDetailTask] = useState(null);
  const [requestOpen, setRequestOpen] = useState(false);
  const [requestForm, setRequestForm] = useState({ destinationSectorId: "", title: "", description: "", priority: "NORMAL", deadline: "" });

  useEffect(() => {
    let mounted = true;

    async function loadTasksContext() {
      setLoading(true);
      setMessage(null);

      const [tasksResult, boardsResult, sectorsResult, employeesResult] = await Promise.allSettled([
        api.getTasks(),
        api.getBoards(),
        api.getSectors(),
        api.getEmployees(),
      ]);

      if (!mounted) return;

      const nextTasks = tasksResult.status === "fulfilled" ? pageItems(tasksResult.value) : [];
      const nextBoards = boardsResult.status === "fulfilled" ? pageItems(boardsResult.value) : [];
      const nextSectors = sectorsResult.status === "fulfilled" ? pageItems(sectorsResult.value) : [];
      const nextEmployees = employeesResult.status === "fulfilled" ? pageItems(employeesResult.value) : [];
      const failures = [];

      if (tasksResult.status === "rejected") failures.push("tarefas");
      if (boardsResult.status === "rejected") failures.push("quadros");
      if (sectorsResult.status === "rejected") failures.push("setores");
      if (employeesResult.status === "rejected") failures.push("responsaveis");

      setBoards(nextBoards);
      setSectors(nextSectors);
      setEmployees(nextEmployees);
      setTasks(hydrateTasks(nextTasks, nextBoards, nextEmployees));
      setLoading(false);

      if (failures.length > 0) {
        setMessage({
          type: "warning",
          text: `Nao foi possivel carregar ${failures.join(", ")} no backend. A tela exibira somente os dados retornados pela API.`,
        });
      }
    }

    loadTasksContext();
    return () => {
      mounted = false;
    };
  }, []);

  const allBoards = useMemo(() => mergeBoards(boards, tasks), [boards, tasks]);
  const allSectors = useMemo(() => mergeSectors(sectors, allBoards, tasks), [sectors, allBoards, tasks]);

  const visibleTasks = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return tasks.filter((task) => {
      const status = taskStatus(task).key;
      const currentSectorKey = taskSectorKey(task);
      const haystack = [
        task.title,
        task.description,
        employeeName(task.responsible),
        boardName(task.board),
        sectorName(task.board?.sector || task.board?.sectorId || task.board),
      ]
        .join(" ")
        .toLowerCase();

      return (
        (!normalizedQuery || haystack.includes(normalizedQuery)) &&
        (sectorFilter === "todos" || String(currentSectorKey) === String(sectorFilter)) &&
        (statusFilter === "todos" || status === statusFilter)
      );
    });
  }, [tasks, query, sectorFilter, statusFilter]);

  const visibleSectors = useMemo(() => {
    if (sectorFilter !== "todos") {
      return allSectors.filter((sector) => String(sector.id) === String(sectorFilter));
    }

    return allSectors;
  }, [allSectors, sectorFilter]);

  const stats = useMemo(() => {
    const byStatus = tasks.reduce(
      (acc, task) => {
        acc[taskStatus(task).key] += 1;
        return acc;
      },
      { andamento: 0, planejada: 0, atrasada: 0, revisao: 0, concluida: 0 },
    );

    return [
      ["Tarefas", tasks.length, "bi-list-check", "azul"],
      ["Em andamento", byStatus.andamento, "bi-play-circle-fill", "verde"],
      ["Atrasadas", byStatus.atrasada, "bi-exclamation-circle-fill", "vermelho"],
      ["Setores", allSectors.length, "bi-diagram-3-fill", "amarelo"],
    ];
  }, [tasks, allSectors.length]);

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function openNewTask() {
    setEditingTask(null);
    setForm({
      ...emptyForm,
      boardId: allBoards[0]?.id || "",
      responsibleId: employees[0]?.id || "",
    });
    setModalOpen(true);
  }

  function openEditTask(task) {
    setEditingTask(task);
    setForm({
      title: task.title || "",
      description: task.description || "",
      boardId: taskBoardKey(task) || "",
      responsibleId: taskResponsibleKey(task) || "",
      startDate: toDatetimeLocal(task.startDate),
      endDate: toDatetimeLocal(task.endDate),
      status: task.status || "TODO",
      priority: task.priority || "NORMAL",
      businessPoints: task.businessPoints || 0,
      protocol: task.protocol || "",
      expectedResult: task.expectedResult || "",
    });
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setEditingTask(null);
    setForm(emptyForm);
  }

  async function submitTask(event) {
    event.preventDefault();
    const validationError = validateForm(form);

    if (validationError) {
      setMessage({ type: "error", text: validationError });
      return;
    }

    setSaving(true);

    try {
      if (editingTask) {
        const updatedTask = await api.updateTask(editingTask.id, {
          title: form.title,
          description: form.description,
          responsibleId: form.responsibleId,
          boardId: form.boardId,
          startDate: normalizeLocalDate(form.startDate),
          endDate: normalizeLocalDate(form.endDate),
          status: form.status,
          priority: form.priority,
          businessPoints: form.businessPoints,
          protocol: form.protocol,
          expectedResult: form.expectedResult,
          responsibleIds: [form.responsibleId],
        });

        setTasks((current) =>
          current.map((task) =>
            task.id === editingTask.id
              ? hydrateTasks([updatedTask], allBoards, employees)[0]
              : task,
          ),
        );
      } else {
        const createdTask = await api.createTask({
          title: form.title,
          description: form.description,
          responsible: { id: form.responsibleId },
          board: { id: form.boardId },
          startDate: normalizeLocalDate(form.startDate),
          endDate: normalizeLocalDate(form.endDate),
          status: form.status,
          priority: form.priority,
          businessPoints: form.businessPoints,
          protocol: form.protocol,
          expectedResult: form.expectedResult,
          responsibleIds: [form.responsibleId],
        });

        setTasks((current) => [hydrateTasks([createdTask], allBoards, employees)[0], ...current]);
      }

      setMessage({
        type: "success",
        text: "Tarefa salva no backend com sucesso.",
      });
      closeModal();
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel salvar a tarefa." });
    } finally {
      setSaving(false);
    }
  }

  async function deleteTask(task) {
    const confirmed = window.confirm(`Remover a tarefa "${task.title}"?`);
    if (!confirmed) return;

    try {
      await api.deleteTask(task.id);
      setTasks((current) => current.filter((item) => item.id !== task.id));
      setMessage({ type: "success", text: "Tarefa removida com sucesso." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel remover a tarefa." });
    }
  }

  async function submitRequest(event) {
    event.preventDefault();
    try {
      await api.createTaskRequest({ ...requestForm, deadline: requestForm.deadline || null });
      setRequestOpen(false); setRequestForm({ destinationSectorId: "", title: "", description: "", priority: "NORMAL", deadline: "" });
      setMessage({ type: "success", text: "Demanda enviada ao setor e registrada na caixa de entrada." });
    } catch (error) { setMessage({ type: "error", text: error.message }); }
  }

  return (
    <DashboardLayout styles={["/css/tarefas.css"]}>
      <div className="dashboard tarefas-page">
        <div className="container">
          <div className="tarefas-header">
            <div>
              <p className="section-label mb-0">Setores da prefeitura</p>
              <h3 className="tarefas-title">Atribuicao e gerenciamento de tarefas</h3>
              <p className="tarefas-subtitle">
                Distribua demandas entre setores, acompanhe prazos e altere responsaveis em um unico quadro.
              </p>
            </div>
            <div className="d-flex gap-2 flex-wrap"><button type="button" className="btn btn-outline-primary" onClick={() => setRequestOpen(true)}><i className="bi bi-send"></i> Solicitar a outro setor</button><button type="button" className="btn-primary d-flex align-items-center gap-2" onClick={openNewTask}>
              <i className="bi bi-plus-circle-fill"></i>
              Nova tarefa
            </button></div>
          </div>

          {message && (
            <div className={`auth-message ${message.type} tarefas-message`}>
              <i
                className={`bi ${
                  message.type === "error"
                    ? "bi-exclamation-circle-fill"
                    : message.type === "warning"
                      ? "bi-info-circle-fill"
                      : "bi-check-circle-fill"
                }`}
              ></i>
              {message.text}
            </div>
          )}

          <div className="tarefas-stats">
            {stats.map(([label, value, icon, color]) => (
              <div className="tarefas-stat-card" key={label}>
                <div className={`tarefas-stat-icon ${color}`}>
                  <i className={`bi ${icon}`}></i>
                </div>
                <div>
                  <span>{label}</span>
                  <strong>{value}</strong>
                </div>
              </div>
            ))}
          </div>

          <div className="row g-3">
            <div className="col-12 col-xl-8">
              <div className="tarefas-main-card">
                <div className="tarefas-toolbar">
                  <div className="tarefas-search-wrap">
                    <i className="bi bi-search"></i>
                    <input
                      type="text"
                      className="tarefas-search"
                      placeholder="Buscar tarefa, setor ou responsavel..."
                      value={query}
                      onChange={(event) => setQuery(event.target.value)}
                    />
                  </div>

                  <select className="tarefas-filter" value={sectorFilter} onChange={(event) => setSectorFilter(event.target.value)}>
                    <option value="todos">Todos os setores</option>
                    {allSectors.map((sector) => (
                      <option value={sector.id} key={sector.id}>
                        {sectorName(sector)}
                      </option>
                    ))}
                  </select>

                  <select className="tarefas-filter" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                    <option value="todos">Todos os status</option>
                    <option value="andamento">Em andamento</option>
                    <option value="planejada">Planejada</option>
                    <option value="atrasada">Atrasada</option>
                    <option value="revisao">Em revisao</option>
                    <option value="concluida">Concluida</option>
                  </select>
                </div>

                {loading ? (
                  <div className="tarefas-empty-state">
                    <i className="bi bi-arrow-repeat"></i>
                    <span>Carregando tarefas...</span>
                  </div>
                ) : (
                  <div className="tarefas-board-grid">
                    {visibleSectors.length === 0 && (
                      <div className="tarefas-empty-state">
                        <i className="bi bi-kanban"></i>
                        <span>Nenhum setor encontrado para organizar tarefas.</span>
                      </div>
                    )}

                    {visibleSectors.map((sector) => {
                      const sectorTasks = visibleTasks.filter((task) => String(taskSectorKey(task)) === String(sector.id));

                      return (
                        <section className="tarefas-column" key={sector.id}>
                          <div className="tarefas-column-header">
                            <div>
                              <span>Setor</span>
                              <strong>{sectorName(sector)}</strong>
                            </div>
                            <small>{sectorTasks.length}</small>
                          </div>

                          <div className="tarefas-column-body">
                            {sectorTasks.length === 0 ? (
                              <div className="tarefas-column-empty">Sem tarefas neste filtro.</div>
                            ) : (
                              sectorTasks.map((task) => {
                                const status = taskStatus(task);

                                return (
                                  <article className="tarefa-card" key={task.id}>
                                    <div className="tarefa-card-top">
                                      <span className={`tarefa-status ${status.key}`}>
                                        <i className={`bi ${status.icon}`}></i>
                                        {status.label}
                                      </span>
                                      <div className="tarefa-card-actions">
                                        <button type="button" className="table-action-btn" title="Editar" onClick={() => openEditTask(task)}>
                                          <i className="bi bi-pencil"></i>
                                        </button>
                                        <button type="button" className="table-action-btn danger" title="Remover" onClick={() => deleteTask(task)}>
                                          <i className="bi bi-trash"></i>
                                        </button>
                                      </div>
                                    </div>

                                    <button type="button" className="tarefa-title-button" onClick={() => setDetailTask(task)}><h4>{task.title}</h4></button>
                                    <p>{task.description}</p>

                                    <div className="tarefa-badges"><span>{task.priority || "NORMAL"}</span><span>{task.businessPoints || 0} pts</span>{task.protocol && <span>{task.protocol}</span>}</div>

                                    <div className="tarefa-meta-grid">
                                      <div>
                                        <span>Responsavel</span>
                                        <strong>{employeeName(task.responsible)}</strong>
                                      </div>
                                      <div>
                                        <span>Prazo</span>
                                        <strong>{dateLabel(task.endDate)}</strong>
                                      </div>
                                    </div>

                                    <div className="tarefa-responsavel-row">
                                      <div className="tarefa-avatar">{employeeInitials(task.responsible) || "?"}</div>
                                      <div>
                                        <span>Encaminhada para</span>
                                        <strong>{boardName(task.board)}</strong>
                                      </div>
                                    </div>
                                  </article>
                                );
                              })
                            )}
                          </div>
                        </section>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>

            <div className="col-12 col-xl-4">
              <aside className="tarefas-side">
                <div className="tarefas-side-card">
                  <div className="tarefas-side-title">
                    <i className="bi bi-diagram-3-fill"></i>
                    Fluxo entre setores
                  </div>
                  <div className="tarefas-sector-list">
                    {allSectors.map((sector) => {
                      const count = tasks.filter((task) => String(taskSectorKey(task)) === String(sector.id)).length;
                      return (
                        <button
                          type="button"
                          className={`tarefas-sector-item ${sectorFilter === sector.id ? "active" : ""}`}
                          onClick={() => setSectorFilter(sector.id)}
                          key={sector.id}
                        >
                          <span>{sectorName(sector)}</span>
                          <strong>{count}</strong>
                        </button>
                      );
                    })}
                  </div>
                  {sectorFilter !== "todos" && (
                    <button type="button" className="task-btn-muted w-100 mt-2" onClick={() => setSectorFilter("todos")}>
                      Ver todos os setores
                    </button>
                  )}
                </div>

                <div className="tarefas-side-card">
                  <div className="tarefas-side-title">
                    <i className="bi bi-people-fill"></i>
                    Responsaveis
                  </div>
                  <div className="tarefas-responsaveis">
                    {employees.slice(0, 5).map((employee) => (
                      <div className="tarefas-responsavel" key={employee.id}>
                        <div className="tarefa-avatar">{employeeInitials(employee) || "?"}</div>
                        <div>
                          <strong>{employeeName(employee)}</strong>
                          <span>{employee.email || "Sem e-mail cadastrado"}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="tarefas-side-card">
                  <div className="tarefas-side-title">
                    <i className="bi bi-clock-history"></i>
                    Prazos proximos
                  </div>
                  <div className="tarefas-deadlines">
                    {tasks
                      .filter((task) => task.endDate)
                      .sort((a, b) => new Date(a.endDate) - new Date(b.endDate))
                      .slice(0, 4)
                      .map((task) => (
                        <div className="tarefas-deadline" key={`deadline-${task.id}`}>
                          <span>{task.title}</span>
                          <strong>{dateLabel(task.endDate)}</strong>
                        </div>
                      ))}
                  </div>
                </div>
              </aside>
            </div>
          </div>
        </div>
      </div>

      <TaskModal
        open={modalOpen}
        editingTask={editingTask}
        boards={allBoards}
        employees={employees}
        form={form}
        saving={saving}
        onClose={closeModal}
        onChange={updateForm}
        onSubmit={submitTask}
      />
      {requestOpen && <div className="react-modal-backdrop" role="dialog" aria-modal="true"><div className="react-modal-card task-modal-card"><div className="task-modal-header"><div><p className="section-label mb-1">Fluxo entre setores</p><h4>Solicitar demanda</h4></div><button className="btn-acao" onClick={() => setRequestOpen(false)}><i className="bi bi-x-lg"></i></button></div><form className="row g-3" onSubmit={submitRequest}><div className="col-12"><label className="field-label">Setor de destino</label><select className="field-input" required value={requestForm.destinationSectorId} onChange={(event) => setRequestForm({ ...requestForm, destinationSectorId: event.target.value })}><option value="">Selecione</option>{allSectors.map((sector) => <option key={sector.id} value={sector.id}>{sectorName(sector)}</option>)}</select></div><div className="col-12"><label className="field-label">Titulo</label><input className="field-input" required maxLength="160" value={requestForm.title} onChange={(event) => setRequestForm({ ...requestForm, title: event.target.value })} /></div><div className="col-12"><label className="field-label">Descricao</label><textarea className="field-input" rows="4" maxLength="5000" value={requestForm.description} onChange={(event) => setRequestForm({ ...requestForm, description: event.target.value })}></textarea></div><div className="col-md-6"><label className="field-label">Prioridade</label><select className="field-input" value={requestForm.priority} onChange={(event) => setRequestForm({ ...requestForm, priority: event.target.value })}><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option><option value="LOW">Baixa</option></select></div><div className="col-md-6"><label className="field-label">Prazo</label><input className="field-input" type="date" min={new Date().toISOString().slice(0,10)} value={requestForm.deadline} onChange={(event) => setRequestForm({ ...requestForm, deadline: event.target.value })} /></div><div className="col-12 text-end"><button className="btn btn-primary">Enviar demanda</button></div></form></div></div>}
      {detailTask && <TaskDetailPanel task={detailTask} onClose={() => setDetailTask(null)} onMessage={setMessage} />}
    </DashboardLayout>
  );
}

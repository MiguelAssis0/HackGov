import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api } from "../services/api.js";
import {
  mockupTaskBoards,
  mockupTaskEmployees,
  mockupTasks,
  pageItems,
} from "../services/mockupService.js";

const emptyForm = {
  title: "",
  description: "",
  boardId: "",
  responsibleId: "",
  startDate: "",
  endDate: "",
};

function isDemoId(id) {
  return typeof id === "string" && id.startsWith("demo-");
}

function randomId(prefix) {
  return `${prefix}-${globalThis.crypto?.randomUUID?.() || Date.now()}`;
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

function boardName(board) {
  return board?.name || sectorName(board);
}

function taskBoardKey(task) {
  return task?.board?.id || task?.boardId || sectorName(task?.board);
}

function taskStatus(task) {
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

function mergeBoards(boards, tasks) {
  const map = new Map();

  boards.forEach((board) => {
    if (board?.id) map.set(board.id, board);
  });

  tasks.forEach((task) => {
    const board = task?.board;
    const key = taskBoardKey(task);
    if (key && !map.has(key)) {
      map.set(key, board || { id: key, name: key, sector: { name: key } });
    }
  });

  return Array.from(map.values());
}

function buildLocalTask(form, boards, employees, currentTask) {
  const board = boards.find((item) => String(item.id) === String(form.boardId));
  const responsible = employees.find((item) => String(item.id) === String(form.responsibleId));

  return {
    ...(currentTask || {}),
    id: currentTask?.id || randomId("local-task"),
    title: form.title,
    description: form.description,
    board,
    responsible,
    startDate: normalizeLocalDate(form.startDate),
    endDate: normalizeLocalDate(form.endDate),
  };
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
                    {sectorName(board)}
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
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [apiReady, setApiReady] = useState(false);
  const [message, setMessage] = useState(null);
  const [query, setQuery] = useState("");
  const [boardFilter, setBoardFilter] = useState("todos");
  const [statusFilter, setStatusFilter] = useState("todos");
  const [modalOpen, setModalOpen] = useState(() => params.get("nova") === "1");
  const [editingTask, setEditingTask] = useState(null);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState(emptyForm);

  useEffect(() => {
    let mounted = true;

    async function loadTasksContext() {
      setLoading(true);
      const [tasksResult, boardsResult, employeesResult] = await Promise.allSettled([
        api.getTasks(),
        api.getBoards(),
        api.getEmployees(),
      ]);

      if (!mounted) return;

      const ready =
        tasksResult.status === "fulfilled" &&
        boardsResult.status === "fulfilled" &&
        employeesResult.status === "fulfilled";
      const hasSession = Boolean(localStorage.getItem("hackgov.accessToken"));

      const nextTasks = tasksResult.status === "fulfilled" ? pageItems(tasksResult.value) : hasSession ? [] : mockupTasks;
      const nextBoards = boardsResult.status === "fulfilled" ? pageItems(boardsResult.value) : hasSession ? [] : mockupTaskBoards;
      const nextEmployees =
        employeesResult.status === "fulfilled" ? pageItems(employeesResult.value) : hasSession ? [] : mockupTaskEmployees;

      setTasks(nextTasks);
      setBoards(nextBoards.length ? nextBoards : mergeBoards([], nextTasks));
      setEmployees(nextEmployees);
      setApiReady(ready);
      setLoading(false);

      if (!ready) {
        setMessage({
          type: "warning",
          text: hasSession
            ? "Nao foi possivel carregar todos os dados do backend. A tela vai mostrar somente o que a API retornar para sua permissao."
            : "Nao foi possivel carregar todos os dados do backend. A tela esta usando dados de apoio ate a API responder.",
        });
      }
    }

    loadTasksContext();
    return () => {
      mounted = false;
    };
  }, []);

  const allBoards = useMemo(() => mergeBoards(boards, tasks), [boards, tasks]);

  const visibleTasks = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return tasks.filter((task) => {
      const status = taskStatus(task).key;
      const boardKey = taskBoardKey(task);
      const haystack = [
        task.title,
        task.description,
        employeeName(task.responsible),
        boardName(task.board),
        sectorName(task.board),
      ]
        .join(" ")
        .toLowerCase();

      return (
        (!normalizedQuery || haystack.includes(normalizedQuery)) &&
        (boardFilter === "todos" || String(boardKey) === String(boardFilter)) &&
        (statusFilter === "todos" || status === statusFilter)
      );
    });
  }, [tasks, query, boardFilter, statusFilter]);

  const visibleBoards = useMemo(() => {
    if (boardFilter !== "todos") {
      return allBoards.filter((board) => String(board.id) === String(boardFilter));
    }

    return allBoards;
  }, [allBoards, boardFilter]);

  const stats = useMemo(() => {
    const byStatus = tasks.reduce(
      (acc, task) => {
        acc[taskStatus(task).key] += 1;
        return acc;
      },
      { andamento: 0, planejada: 0, atrasada: 0 },
    );

    return [
      ["Tarefas", tasks.length, "bi-list-check", "azul"],
      ["Em andamento", byStatus.andamento, "bi-play-circle-fill", "verde"],
      ["Atrasadas", byStatus.atrasada, "bi-exclamation-circle-fill", "vermelho"],
      ["Setores", allBoards.length, "bi-diagram-3-fill", "amarelo"],
    ];
  }, [tasks, allBoards.length]);

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
      responsibleId: task.responsible?.id || "",
      startDate: toDatetimeLocal(task.startDate),
      endDate: toDatetimeLocal(task.endDate),
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
    const localTask = buildLocalTask(form, allBoards, employees, editingTask);
    const canPersist =
      apiReady &&
      !isDemoId(localTask.id) &&
      !isDemoId(form.boardId) &&
      !isDemoId(form.responsibleId);

    setSaving(true);

    try {
      if (canPersist && editingTask) {
        const updatedTask = await api.updateTask(editingTask.id, {
          title: form.title,
          description: form.description,
          responsibleId: form.responsibleId,
          boardId: form.boardId,
          startDate: normalizeLocalDate(form.startDate),
          endDate: normalizeLocalDate(form.endDate),
        });

        setTasks((current) =>
          current.map((task) =>
            task.id === editingTask.id
              ? {
                  ...localTask,
                  ...updatedTask,
                  board: updatedTask.board || localTask.board,
                  responsible: updatedTask.responsible || localTask.responsible,
                }
              : task,
          ),
        );
      } else if (canPersist) {
        const createdTask = await api.createTask({
          title: form.title,
          description: form.description,
          responsible: { id: form.responsibleId },
          board: { id: form.boardId },
          startDate: normalizeLocalDate(form.startDate),
          endDate: normalizeLocalDate(form.endDate),
        });

        setTasks((current) => [
          {
            ...localTask,
            ...createdTask,
            board: createdTask.board || localTask.board,
            responsible: createdTask.responsible || localTask.responsible,
          },
          ...current,
        ]);
      } else if (editingTask) {
        setTasks((current) => current.map((task) => (task.id === editingTask.id ? localTask : task)));
      } else {
        setTasks((current) => [localTask, ...current]);
      }

      setMessage({
        type: "success",
        text: canPersist ? "Tarefa salva no backend com sucesso." : "Tarefa registrada localmente enquanto a API de tarefas nao responde.",
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
      if (apiReady && !isDemoId(task.id)) {
        await api.deleteTask(task.id);
      }

      setTasks((current) => current.filter((item) => item.id !== task.id));
      setMessage({ type: "success", text: "Tarefa removida com sucesso." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel remover a tarefa." });
    }
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
            <button type="button" className="btn-primary d-flex align-items-center gap-2" onClick={openNewTask}>
              <i className="bi bi-plus-circle-fill"></i>
              Nova tarefa
            </button>
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

                  <select className="tarefas-filter" value={boardFilter} onChange={(event) => setBoardFilter(event.target.value)}>
                    <option value="todos">Todos os setores</option>
                    {allBoards.map((board) => (
                      <option value={board.id} key={board.id}>
                        {sectorName(board)}
                      </option>
                    ))}
                  </select>

                  <select className="tarefas-filter" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                    <option value="todos">Todos os status</option>
                    <option value="andamento">Em andamento</option>
                    <option value="planejada">Planejada</option>
                    <option value="atrasada">Atrasada</option>
                  </select>
                </div>

                {loading ? (
                  <div className="tarefas-empty-state">
                    <i className="bi bi-arrow-repeat"></i>
                    <span>Carregando tarefas...</span>
                  </div>
                ) : (
                  <div className="tarefas-board-grid">
                    {visibleBoards.length === 0 && (
                      <div className="tarefas-empty-state">
                        <i className="bi bi-kanban"></i>
                        <span>Nenhum setor encontrado para organizar tarefas.</span>
                      </div>
                    )}

                    {visibleBoards.map((board) => {
                      const boardTasks = visibleTasks.filter((task) => String(taskBoardKey(task)) === String(board.id));

                      return (
                        <section className="tarefas-column" key={board.id}>
                          <div className="tarefas-column-header">
                            <div>
                              <span>Setor</span>
                              <strong>{sectorName(board)}</strong>
                            </div>
                            <small>{boardTasks.length}</small>
                          </div>

                          <div className="tarefas-column-body">
                            {boardTasks.length === 0 ? (
                              <div className="tarefas-column-empty">Sem tarefas neste filtro.</div>
                            ) : (
                              boardTasks.map((task) => {
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

                                    <h4>{task.title}</h4>
                                    <p>{task.description}</p>

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
                    {allBoards.map((board) => {
                      const count = tasks.filter((task) => String(taskBoardKey(task)) === String(board.id)).length;
                      return (
                        <button
                          type="button"
                          className={`tarefas-sector-item ${boardFilter === board.id ? "active" : ""}`}
                          onClick={() => setBoardFilter(board.id)}
                          key={board.id}
                        >
                          <span>{sectorName(board)}</span>
                          <strong>{count}</strong>
                        </button>
                      );
                    })}
                  </div>
                  {boardFilter !== "todos" && (
                    <button type="button" className="task-btn-muted w-100 mt-2" onClick={() => setBoardFilter("todos")}>
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
    </DashboardLayout>
  );
}

import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserDisplayName } from "../services/api.js";
import { toolColors, useAvailableTools } from "../services/mockupService.js";

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content || payload?.items || [];
}

function resolveCityHallName(user) {
  const selectedCityHall = getSelectedCityHall();
  if (selectedCityHall?.name) return selectedCityHall.name;

  const cityHall = user?.cityHall;
  if (cityHall?.name) return cityHall.name;
  if (typeof cityHall === "string" && cityHall.trim()) return cityHall;
  if (typeof user?.prefeitura === "string" && user.prefeitura.trim()) return user.prefeitura;
  if (user?.prefeitura?.name) return user.prefeitura.name;

  return "Prefeitura vinculada";
}

function taskStatus(task) {
  const now = new Date();
  const start = task.startDate ? new Date(task.startDate) : null;
  const end = task.endDate ? new Date(task.endDate) : null;

  if (end && end < now) return "vermelho";
  if (start && start > now) return "amarelo";
  return "primary";
}

function taskMeta(task) {
  if (!task?.endDate) return "Sem prazo definido";

  const date = new Date(task.endDate);
  if (Number.isNaN(date.getTime())) return "Prazo indisponivel";

  const formatted = new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);

  return `Ate ${formatted}`;
}

function buildCalendarDays(viewDate, tasks) {
  const year = viewDate.getFullYear();
  const month = viewDate.getMonth();
  const firstDay = new Date(year, month, 1);
  const start = new Date(firstDay);
  start.setDate(firstDay.getDate() - firstDay.getDay());

  const today = new Date();
  const deadlineKeys = new Set(
    tasks
      .map((task) => {
        if (!task?.endDate) return null;
        const date = new Date(task.endDate);
        if (Number.isNaN(date.getTime())) return null;
        return `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`;
      })
      .filter(Boolean),
  );

  return Array.from({ length: 35 }, (_, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);

    const classes = [];
    if (date.getMonth() !== month) classes.push("outro-mes");
    if (
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()
    ) {
      classes.push("hoje");
    }
    if (deadlineKeys.has(`${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`)) {
      classes.push("has-event");
    }

    return [String(date.getDate()), classes.join(" ").trim()];
  });
}

function StatCard({ icon, label, value }) {
  return (
    <article className="dashboard-stat-card">
      <i className={`bi ${icon}`}></i>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function ToolCard({ tool, color }) {
  const content = (
    <>
      <div className="dashboard-tool-icon" style={{ color: color.fg }}>
        <i className={`bi ${tool.icon}`}></i>
      </div>
      <div>
        <h4>{tool.name}</h4>
        <p>{tool.description}</p>
      </div>
    </>
  );

  return tool.route ? (
    <Link to={tool.route} className="dashboard-tool-card">
      {content}
    </Link>
  ) : (
    <div className="dashboard-tool-card">{content}</div>
  );
}

export default function DashboardPage() {
  const user = getStoredUser() || { nome: "Usuario" };
  const displayName = getUserDisplayName(user);
  const cityHallName = resolveCityHallName(user);
  const dashboardTools = useAvailableTools();
  const [employees, setEmployees] = useState([]);
  const [occupations, setOccupations] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [monthDate, setMonthDate] = useState(() => new Date());

  useEffect(() => {
    let mounted = true;

    async function loadDashboard() {
      setLoading(true);
      setMessage(null);

      const [employeesResult, occupationsResult, sectorsResult, tasksResult] = await Promise.allSettled([
        api.getEmployees(),
        api.getOccupations(),
        api.getSectors(),
        api.getTasks(),
      ]);

      if (!mounted) return;

      const nextEmployees = employeesResult.status === "fulfilled" ? pageItems(employeesResult.value) : [];
      const nextOccupations = occupationsResult.status === "fulfilled" ? pageItems(occupationsResult.value) : [];
      const nextSectors = sectorsResult.status === "fulfilled" ? pageItems(sectorsResult.value) : [];
      const nextTasks = tasksResult.status === "fulfilled" ? pageItems(tasksResult.value) : [];
      const failures = [];

      if (employeesResult.status === "rejected") failures.push("funcionarios");
      if (occupationsResult.status === "rejected") failures.push("cargos");
      if (sectorsResult.status === "rejected") failures.push("setores");
      if (tasksResult.status === "rejected") failures.push("tarefas");

      setEmployees(nextEmployees);
      setOccupations(nextOccupations);
      setSectors(nextSectors);
      setTasks(nextTasks);
      setLoading(false);

      if (failures.length > 0) {
        setMessage({
          type: "warning",
          text: `Nao foi possivel carregar ${failures.join(", ")} no backend. O dashboard exibira apenas os dados disponiveis.`,
        });
      }
    }

    loadDashboard();
    return () => {
      mounted = false;
    };
  }, []);

  const monthLabel = useMemo(
    () =>
      new Intl.DateTimeFormat("pt-BR", {
        month: "long",
        year: "numeric",
      }).format(monthDate),
    [monthDate],
  );

  const stats = useMemo(
    () => [
      { icon: "bi-people-fill", label: "Funcionarios", value: employees.length },
      { icon: "bi-diagram-3-fill", label: "Setores", value: sectors.length },
      { icon: "bi-person-badge-fill", label: "Cargos", value: occupations.length },
      { icon: "bi-grid-1x2-fill", label: "Ferramentas disponiveis", value: dashboardTools.length },
    ],
    [employees.length, sectors.length, occupations.length, dashboardTools.length],
  );

  const calendarDays = useMemo(() => buildCalendarDays(monthDate, tasks), [monthDate, tasks]);

  const taskPreview = useMemo(
    () =>
      tasks
        .filter((task) => task?.title)
        .sort((a, b) => {
          const aTime = a?.endDate ? new Date(a.endDate).getTime() : Number.MAX_SAFE_INTEGER;
          const bTime = b?.endDate ? new Date(b.endDate).getTime() : Number.MAX_SAFE_INTEGER;
          return aTime - bTime;
        })
        .slice(0, 3)
        .map((task) => ({
          id: task.id,
          name: task.title,
          color: taskStatus(task),
          meta: taskMeta(task),
        })),
    [tasks],
  );

  return (
    <DashboardLayout styles={["/css/dashboard.css"]}>
      <div className="dashboard">
        <div className="container">
          {message && (
            <div className={`auth-message ${message.type} mb-3`}>
              <i className="bi bi-info-circle-fill"></i>
              {message.text}
            </div>
          )}

          <div className="row">
            <div className="col-12 col-lg-8 d-flex flex-column gap-3">
              <section className="dash-hero">
                <div className="row">
                  <div className="col-12 col-md-8">
                    <h2 className="hero-h2">Ola, {displayName}</h2>
                    <p className="hero-p">Bem-vindo/a a prefeitura digital de {cityHallName}</p>

                    <div className="dash-hero-botoes">
                      <Link to="/processos" className="btn btn-primary">
                        <i className="bi bi-diagram-3"></i> Ver processos
                      </Link>
                      <Link to="/tarefas" className="btn btn-outline-primary">
                        <i className="bi bi-plus-lg"></i> Gerenciar Tarefas
                      </Link>
                    </div>
                  </div>

                  <div className="col-4 d-none d-md-flex align-items-center justify-content-end">
                    <div className="dash-hero-img">
                      <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <polygon points="50,10 90,80 10,80" fill="white" />
                      </svg>
                    </div>
                  </div>
                </div>
              </section>

              <section className="dashboard-stats-grid" aria-label="Resumo da prefeitura">
                {stats.map((item) => (
                  <StatCard {...item} key={item.label} />
                ))}
              </section>

              <section className="dashboard-tools-panel">
                <div className="dashboard-tools-title">
                  <i className="bi bi-grid-1x2-fill"></i>
                  <h4 className="mb-0">Ferramentas disponiveis</h4>
                </div>

                <div className="dashboard-tools-list">
                  {dashboardTools.map((tool, index) => (
                    <ToolCard tool={tool} color={toolColors[index % toolColors.length]} key={tool.id} />
                  ))}
                </div>
              </section>
            </div>

            <div className="col-12 col-lg-4 mb-5 pb-5 mb-lg-0 pb-lg-0">
              <aside className="d-flex flex-column gap-3 coluna-direita">
                <div className="card-2">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <h4>
                      <i className="bi bi-calendar-date-fill primary me-1"></i>
                      {monthLabel}
                    </h4>
                    <div className="cal-nav d-flex">
                      <button
                        className="cal-nav-btn"
                        type="button"
                        aria-label="Mes anterior"
                        onClick={() => setMonthDate((current) => new Date(current.getFullYear(), current.getMonth() - 1, 1))}
                      >
                        <i className="bi bi-chevron-left"></i>
                      </button>
                      <button
                        className="cal-nav-btn"
                        type="button"
                        aria-label="Proximo mes"
                        onClick={() => setMonthDate((current) => new Date(current.getFullYear(), current.getMonth() + 1, 1))}
                      >
                        <i className="bi bi-chevron-right"></i>
                      </button>
                    </div>
                  </div>

                  <div className="cal-grid">
                    {["D", "S", "T", "Q", "Q", "S", "S"].map((label, index) => (
                      <div className="cal-dia-label" key={`${label}-${index}`}>
                        {label}
                      </div>
                    ))}
                    {calendarDays.map(([day, className], index) => (
                      <div className={`cal-dia ${className}`.trim()} key={`${day}-${index}`}>
                        {day}
                      </div>
                    ))}
                  </div>
                </div>

                <div className="card-2">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <h4>
                      <i className="bi bi-check-square-fill primary me-1"></i> Suas tarefas
                    </h4>
                    <Link to="/tarefas">
                      <h4 className="primary">Ver todas</h4>
                    </Link>
                  </div>

                  {loading ? (
                    <div className="empty-state">
                      <i className="bi bi-arrow-repeat"></i>
                      <span>Carregando dashboard...</span>
                    </div>
                  ) : taskPreview.length === 0 ? (
                    <div className="empty-state">
                      <i className="bi bi-check2-circle"></i>
                      <span>Nenhuma tarefa encontrada para exibicao.</span>
                    </div>
                  ) : (
                    taskPreview.map(({ id, name, color, meta }) => (
                      <div className="item-tarefa" key={id || name}>
                        <div className="nome-tarefa">{name}</div>
                        <div className="tarefa-meta">
                          <span className={color}>
                            <i className="bi bi-clock-fill"></i>
                          </span>
                          {meta}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </aside>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { getStoredUser, getUserDisplayName } from "../services/api.js";
import {
  mockupDashboardCalendarDays,
  mockupDashboardTaskPreview,
  toolColors,
  useAvailableTools,
  useCityHallName,
  useEmployees,
  useJobs,
  useSectors,
} from "../services/mockupService.js";

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
        <p>{tool.description || "Ferramenta disponivel para a prefeitura."}</p>
      </div>
    </>
  );

  if (tool.route) {
    return (
      <Link to={tool.route} className="dashboard-tool-card">
        {content}
      </Link>
    );
  }

  return <div className="dashboard-tool-card">{content}</div>;
}

export default function DashboardPage() {
  const user = getStoredUser() || { nome: "Usuario", setor: "" };
  const displayName = getUserDisplayName(user);
  const cityHallName = useCityHallName();
  const [employees] = useEmployees();
  const [sectors] = useSectors();
  const [jobs] = useJobs();
  const availableTools = useAvailableTools();

  const monthLabel = new Intl.DateTimeFormat("pt-BR", {
    month: "long",
    year: "numeric",
  }).format(new Date());

  const stats = [
    { icon: "bi-people-fill", label: "Funcion\u00e1rios", value: employees.length },
    { icon: "bi-diagram-3-fill", label: "Setores", value: sectors.length },
    { icon: "bi-person-badge-fill", label: "Cargos", value: jobs.length },
    { icon: "bi-grid-1x2-fill", label: "Ferramentas dispon\u00edveis", value: availableTools.length },
  ];

  return (
    <DashboardLayout styles={["/css/dashboard.css"]}>
      <div className="dashboard">
        <div className="container">
          <div className="row">
            <div className="col-12 col-lg-8 d-flex flex-column gap-3">
              <section className="dash-hero">
                <div className="row">
                  <div className="col-12 col-md-8">
                    <h2 className="hero-h2">Ol&aacute;, {displayName}</h2>
                    <p className="hero-p">Bem-vindo/a &agrave; prefeitura digital de {cityHallName}</p>

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
                  <h4 className="mb-0">Ferramentas disponíveis</h4>
                </div>

                <div className="dashboard-tools-list">
                  {availableTools.map((tool, index) => (
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
                      <button className="cal-nav-btn" type="button" aria-label="Mes anterior">
                        <i className="bi bi-chevron-left"></i>
                      </button>
                      <button className="cal-nav-btn" type="button" aria-label="Proximo mes">
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
                    {mockupDashboardCalendarDays.map(([day, className], index) => (
                      <div className={`cal-dia ${className}`} key={`${day}-${index}`}>
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

                  {mockupDashboardTaskPreview.map(({ name, color, meta }) => (
                    <div className="item-tarefa" key={name}>
                      <div className="nome-tarefa">{name}</div>
                      <div className="tarefa-meta">
                        <span className={color}>
                          <i className="bi bi-clock-fill"></i>
                        </span>
                        {meta}
                      </div>
                    </div>
                  ))}
                </div>
              </aside>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

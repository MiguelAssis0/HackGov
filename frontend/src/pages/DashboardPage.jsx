import Chatbot from "../components/Chatbot.jsx";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { getStoredUser } from "../services/api.js";

const calendarDays = [
  ["30", "outro-mes"],
  ["31", "outro-mes"],
  ["1", ""],
  ["2", ""],
  ["3", ""],
  ["4", ""],
  ["5", ""],
  ["6", ""],
  ["7", ""],
  ["8", ""],
  ["9", ""],
  ["10", ""],
  ["11", ""],
  ["12", ""],
  ["13", ""],
  ["14", ""],
  ["15", ""],
  ["16", ""],
  ["17", ""],
  ["18", "has-event"],
  ["19", ""],
  ["20", "has-event"],
  ["21", "has-event hoje"],
  ["22", ""],
  ["23", ""],
  ["24", ""],
  ["25", ""],
  ["26", ""],
  ["27", ""],
  ["28", ""],
  ["29", ""],
  ["30", ""],
  ["31", ""],
  ["1", "outro-mes"],
  ["2", "outro-mes"],
];

function ProgressCard({ title, progress, status }) {
  const circumference = 226.2;
  const offset = circumference - (circumference * progress) / 100;

  return (
    <div className="col">
      <a href="#" className="card">
        <div className="d-flex justify-content-between align-items-center mb-1">
          <h4>{title}</h4>
          <button className="card-menu-btn" type="button" aria-label={`Opções de ${title}`}>
            <i className="bi bi-three-dots"></i>
          </button>
        </div>
        <div className="mx-auto text-center">
          <div className="anel-container">
            <svg className="anel-svg" width="90" height="90" viewBox="0 0 90 90">
              <circle className="anel-bg" cx="45" cy="45" r="36" />
              <circle
                className="anel-fill"
                cx="45"
                cy="45"
                r="36"
                stroke="var(--primary)"
                strokeDasharray={circumference}
                strokeDashoffset={offset}
              />
            </svg>
            <div className="anel-progresso">{progress}%</div>
          </div>
          <span className="anel-status">{status}</span>
        </div>
      </a>
    </div>
  );
}

export default function DashboardPage() {
  const user = getStoredUser() || { nome: "Usuário", setor: "" };
  const monthLabel = new Intl.DateTimeFormat("pt-BR", {
    month: "long",
    year: "numeric",
  }).format(new Date());

  return (
    <DashboardLayout styles={["/css/dashboard.css"]}>
      <div className="dashboard">
        <div className="container">
          <div className="row">
            <div className="col-12 col-lg-8 d-flex flex-column gap-3">
              <div className="dash-hero">
                <div className="row">
                  <div className="col-12 col-md-8">
                    <h2 className="hero-h2">
                      Olá, {user.nome}
                      {user.setor && (
                        <small style={{ fontSize: "0.9rem", opacity: 0.7, fontWeight: 400 }}>
                          {" "}— {user.setor}
                        </small>
                      )}
                    </h2>
                    <p className="hero-p">Bem-vindo ao ERP Municipal. Acompanhe seus processos e tarefas.</p>

                    <div className="dash-hero-botoes">
                      <Link to="/processos" className="btn btn-primary">
                        Ver Processos <i className="bi bi-diagram-3"></i>
                      </Link>
                      <a href="#" className="btn btn-outline-primary">
                        Nova Tarefa <i className="bi bi-plus"></i>
                      </a>
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
              </div>

              <div className="row g-2">
                <ProgressCard title="Entrega 2" progress={60} status="Em progresso" />
                <ProgressCard title="Entrega 1" progress={90} status="Quase pronto" />
              </div>

              <div className="card-2">
                <div className="d-flex justify-content-between align-items-center mb-4">
                  <h4>
                    <i className="bi bi-wrench-adjustable primary me-1"></i> Ferramentas
                  </h4>
                  <Link to="/ferramentas">
                    <h4 className="primary">Ver todas</h4>
                  </Link>
                </div>

                <div className="row g-2">
                  <div className="col-12">
                    <Link to="/ferramentas" className="card card-2">
                      <h3 className="azul-escuro d-flex align-items-center mb-0">
                        <span className="material-symbols-outlined me-2">drag_indicator</span>
                        Ferramentas
                      </h3>
                    </Link>
                  </div>
                </div>
              </div>
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
                      <button className="cal-nav-btn" type="button" aria-label="Mês anterior">
                        <i className="bi bi-chevron-left"></i>
                      </button>
                      <button className="cal-nav-btn" type="button" aria-label="Próximo mês">
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
                    <a href="#">
                      <h4 className="primary">Ver todas</h4>
                    </a>
                  </div>

                  {[
                    ["Entrega 1", "amarelo", "Até dia 14 de Abril, 13:00h"],
                    ["Reunião", "primary", "Dia 17 de Abril, 13:30h"],
                    ["Entrega 2", "vermelho", "Até dia 31 de Abril, 14:00h"],
                  ].map(([name, color, meta]) => (
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

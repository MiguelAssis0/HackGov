import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api } from "../services/api.js";

const WEEK_DAYS = ["D", "S", "T", "Q", "Q", "S", "S"];

function localDate(value, options) {
  if (!value) return "";
  const [year, month, day] = value.split("-").map(Number);
  return new Intl.DateTimeFormat("pt-BR", options).format(new Date(year, month - 1, day));
}

function StatCard({ icon, label, value }) {
  return <article className="stat-card"><i className={`bi ${icon}`}></i><span>{label}</span><strong>{value}</strong></article>;
}

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState(null);
  const [message, setMessage] = useState(null);
  const [removingFavorite, setRemovingFavorite] = useState("");

  useEffect(() => {
    let active = true;
    api.getDashboard().then((response) => { if (active) setDashboard(response); })
      .catch((error) => { if (active) setMessage({ type: "error", text: error.message || "Não foi possível carregar o dashboard." }); });
    return () => { active = false; };
  }, []);

  async function removeFavorite(slug) {
    setRemovingFavorite(slug);
    setMessage(null);
    try {
      const result = await api.toggleToolFavorite(slug);
      if (!result?.favorite) {
        setDashboard((current) => ({
          ...current,
          favorites: current.favorites.filter((favorite) => favorite.slug !== slug),
          stats: { ...current.stats, favorites: Math.max(0, current.stats.favorites - 1) },
        }));
        setMessage({ type: "success", text: "Favorito removido." });
      }
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Não foi possível remover o favorito." });
    } finally {
      setRemovingFavorite("");
    }
  }

  const stats = dashboard ? [
    ["bi-people-fill", "Funcionários", dashboard.stats.employees],
    ["bi-diagram-3-fill", "Setores", dashboard.stats.sectors],
    ["bi-person-badge-fill", "Cargos", dashboard.stats.occupations],
    ["bi-star-fill", "Favoritos", dashboard.stats.favorites],
  ] : [];

  return (
    <DashboardLayout styles={["/css/dashboard.css"]}>
      <div className="dashboard"><div className="container">
        {message && <div className={`auth-message ${message.type} mb-3`} role="status"><i className={`bi ${message.type === "success" ? "bi-check-circle-fill" : "bi-exclamation-circle-fill"}`}></i>{message.text}</div>}

        {!dashboard ? (
          <div className="dashboard-loading panel"><i className="bi bi-arrow-repeat"></i><span>Carregando dashboard...</span></div>
        ) : (
          <div className="row">
            <div className="col-12 col-lg-8 d-flex flex-column gap-3">
              <section className="dash-hero"><div className="row">
                <div className="col-12 col-md-8">
                  <h2 className="hero-h2">Olá, {dashboard.userShortName}</h2>
                  <p className="hero-p">Bem-vindo/a a prefeitura digital de {dashboard.cityHallName}{dashboard.stateCode ? ` - ${dashboard.stateCode}` : ""}</p>
                  <div className="dash-hero-botoes">
                    <Link to="/processos" className="btn btn-primary"><i className="bi bi-diagram-3"></i> Ver processos</Link>
                    <Link to="/tarefas" className="btn btn-outline-primary"><i className="bi bi-plus"></i> Gerenciar Tarefas</Link>
                  </div>
                </div>
                <div className="col-4 d-none d-md-flex align-items-center justify-content-end"><div className="dash-hero-img">
                  <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true"><polygon points="50,10 90,80 10,80" fill="white" /></svg>
                </div></div>
              </div></section>

              <section className="stats-grid mb-0" aria-label="Resumo da prefeitura">
                {stats.map(([icon, label, value]) => <StatCard icon={icon} label={label} value={value} key={label} />)}
              </section>

              <section className="panel">
                <div className="panel-heading">
                  <h4 className="mb-0"><i className="bi bi-star-fill me-2 text-primary"></i>Favoritos</h4>
                  <Link to="/ferramentas"><h4 className="primary">Ver todos</h4></Link>
                </div>
                <div className="favorite-grid">
                  {dashboard.favorites.length ? dashboard.favorites.map((favorite) => (
                    <article className="favorite-card" key={favorite.slug}>
                      <Link to={favorite.route} className="favorite-link">
                        <span className="favorite-icon"><i className={`bi ${favorite.icon}`}></i></span>
                        <span className="favorite-body"><strong>{favorite.title}</strong><small>{favorite.typeLabel}</small></span>
                      </Link>
                      <button type="button" className="favorite-remove" title="Remover favorito" aria-label={`Remover ${favorite.title} dos favoritos`} disabled={removingFavorite === favorite.slug} onClick={() => removeFavorite(favorite.slug)}>
                        <i className={`bi ${removingFavorite === favorite.slug ? "bi-arrow-repeat" : "bi-star-fill"}`}></i>
                      </button>
                    </article>
                  )) : <div className="empty-state">Você ainda não possui nenhum favorito.</div>}
                </div>
              </section>
            </div>

            <div className="col-12 col-lg-4 mb-5 pb-5 mb-lg-0 pb-lg-0">
              <aside className="d-flex flex-column gap-3 coluna-direita">
                {dashboard.agendaVisible && <section className="card-2 dashboard-agenda">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <h4><i className="bi bi-calendar-date-fill primary me-1"></i>{dashboard.calendar.label}</h4>
                    <Link className="dashboard-agenda-link" to="/agenda">Abrir agenda</Link>
                  </div>
                  <div className="cal-grid">
                    {WEEK_DAYS.map((label, index) => <div className="cal-dia-label" key={`${label}-${index}`}>{label}</div>)}
                    {dashboard.calendar.days.map((day) => <Link
                      to={`/agenda?mes=${dashboard.calendar.year}-${String(dashboard.calendar.month).padStart(2, "0")}`}
                      className={`cal-dia ${day.inMonth ? "" : "outro-mes"} ${day.itemCount ? "has-event" : ""} ${day.today ? "hoje" : ""}`.trim()}
                      title={day.itemCount ? `${day.itemCount} item(ns) neste dia` : "Sem eventos"}
                      key={day.date}
                    >{Number(day.date.slice(-2))}</Link>)}
                  </div>
                  <div className="dashboard-upcoming-events">
                    {dashboard.upcomingEvents.length ? dashboard.upcomingEvents.map((event) => <Link to={`/agenda?mes=${event.startDate.slice(0, 7)}`} className="dashboard-event-row" key={event.id}>
                      <span className="dashboard-event-icon"><i className={`bi ${event.icon}`}></i></span>
                      <span><strong>{event.title}</strong><small>
                        {localDate(event.startDate, { day: "2-digit", month: "2-digit" })}
                        {event.startTime ? ` · ${event.startTime.slice(0, 5)}` : ""}
                        {event.linkedTask ? " · Tarefa vinculada" : ""}
                      </small></span>
                    </Link>) : <div className="dashboard-agenda-empty">Nenhum próximo evento.</div>}
                  </div>
                </section>}

                <section className="card-2">
                  <div className="d-flex justify-content-between align-items-center mb-1">
                    <h4><i className="bi bi-check-square-fill primary me-1"></i> Suas tarefas</h4>
                    <Link to="/tarefas?responsavel=minhas"><h4 className="primary">Ver todas</h4></Link>
                  </div>
                  {dashboard.tasks.length ? dashboard.tasks.map((task) => <Link className={`item-tarefa prazo-${task.deadlineState}`} to={`/tarefas?task=${task.id}`} key={task.id}>
                    <div className="nome-tarefa">{task.title}</div>
                    <div className="tarefa-meta">
                      {task.deadline ? <>
                        <span className="task-deadline-label"><i className="bi bi-calendar2-event-fill"></i>Prazo de entrega: <strong>{localDate(task.deadline)}</strong></span>
                        <span className={`task-due-badge badge-${task.deadlineState === "atrasada" ? "overdue" : task.deadlineState === "hoje" ? "today" : task.deadlineState === "proxima" ? "soon" : "on-time"}`}>
                          <i className={`bi ${task.deadlineState === "atrasada" ? "bi-exclamation-triangle-fill" : task.deadlineState === "hoje" ? "bi-alarm-fill" : task.deadlineState === "proxima" ? "bi-hourglass-split" : "bi-check-circle-fill"}`}></i>{task.deadlineLabel}
                        </span>
                      </> : <span className="task-deadline-label"><i className="bi bi-calendar2-minus"></i>{task.deadlineLabel}</span>}
                    </div>
                    <div className="task-context-meta">
                      <span className="task-state-label"><i className="bi bi-kanban"></i> <strong>{task.statusLabel}</strong></span>
                      <span>Prioridade: <strong>{task.priorityLabel}</strong></span>
                    </div>
                  </Link>) : <div className="task-empty-dashboard"><i className="bi bi-check2-circle"></i><span>Nenhuma tarefa atribuída a você.</span></div>}
                </section>
              </aside>
            </div>
          </div>
        )}
      </div></div>
    </DashboardLayout>
  );
}

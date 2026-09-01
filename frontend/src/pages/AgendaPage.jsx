import { useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api, getSelectedCityHall, getStoredUser } from "../services/api.js";

const emptyForm = { title: "", type: "MEETING", startDate: "", endDate: "", startTime: "", endTime: "", location: "", taskId: "", description: "" };
const typeLabels = { MEETING: "Reunião", DEADLINE: "Prazo", SERVICE: "Atendimento", CEREMONY: "Cerimônia", OTHER: "Outro" };
const typeIcons = { MEETING: "bi-people-fill", DEADLINE: "bi-hourglass-split", SERVICE: "bi-person-workspace", CEREMONY: "bi-bank2", OTHER: "bi-calendar-event-fill" };

function parseMonth(value) {
  const [year, month] = (value || "").split("-").map(Number);
  if (year && month >= 1 && month <= 12) return new Date(year, month - 1, 1);
  const today = new Date();
  return new Date(today.getFullYear(), today.getMonth(), 1);
}

function monthValue(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function dateKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function buildDays(monthDate, events, tasks) {
  const first = new Date(monthDate.getFullYear(), monthDate.getMonth(), 1);
  const last = new Date(monthDate.getFullYear(), monthDate.getMonth() + 1, 0);
  const gridStart = new Date(first);
  gridStart.setDate(first.getDate() - first.getDay());
  const gridEnd = new Date(last);
  gridEnd.setDate(last.getDate() + (6 - last.getDay()));
  const days = [];
  for (const date = new Date(gridStart); date <= gridEnd; date.setDate(date.getDate() + 1)) {
    const key = dateKey(date);
    const items = events.filter((event) => event.startDate <= key && (event.endDate || event.startDate) >= key).map((event) => ({ kind: "event", ...event }));
    tasks.filter((task) => task.deadline === key).forEach((task) => items.push({ kind: "task", ...task }));
    days.push({ date: new Date(date), key, inMonth: date.getMonth() === monthDate.getMonth(), isToday: key === dateKey(new Date()), items });
  }
  return days;
}

function formatDate(value) {
  if (!value) return "";
  const [year, month, day] = value.split("-").map(Number);
  return new Intl.DateTimeFormat("pt-BR").format(new Date(year, month - 1, day));
}

export default function AgendaPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const params = useMemo(() => new URLSearchParams(location.search), [location.search]);
  const monthDate = useMemo(() => parseMonth(params.get("mes")), [params]);
  const selectedTaskId = params.get("tarefa") || "";
  const cityHall = getSelectedCityHall() || { name: getStoredUser()?.prefeitura || "Prefeitura" };
  const [events, setEvents] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [taskOptions, setTaskOptions] = useState([]);
  const [access, setAccess] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const savingRef = useRef(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    Promise.all([
      api.getAgendaEvents(monthValue(monthDate), selectedTaskId),
      api.getAgendaTasks(monthValue(monthDate), selectedTaskId),
      api.getAgendaTaskOptions(),
      api.getAgendaAccess(),
    ])
      .then(([eventItems, deadlineItems, options, agendaAccess]) => {
        if (!active) return;
        setEvents(eventItems || []);
        setTasks(deadlineItems || []);
        setTaskOptions(options || []);
        setAccess(agendaAccess);
        setMessage(null);
      })
      .catch((error) => active && setMessage({ type: "error", text: error.message || "Não foi possível carregar a agenda." }))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [monthDate, selectedTaskId]);

  const days = useMemo(() => buildDays(monthDate, events, tasks), [monthDate, events, tasks]);
  const monthLabel = new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" }).format(monthDate);
  const calendarMonthLabel = `${monthLabel.charAt(0).toUpperCase()}${monthLabel.slice(1)}`;

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function moveMonth(delta) {
    const next = new Date(monthDate.getFullYear(), monthDate.getMonth() + delta, 1);
    const filter = selectedTaskId ? `&tarefa=${encodeURIComponent(selectedTaskId)}` : "";
    navigate(`/agenda?mes=${monthValue(next)}${filter}`);
  }

  function openToday() {
    navigate(selectedTaskId ? `/agenda?tarefa=${encodeURIComponent(selectedTaskId)}` : "/agenda");
  }

  function closeModal() {
    setOpen(false);
  }

  async function submit(event) {
    event.preventDefault();
    if (savingRef.current) return;
    if (form.endDate && form.endDate < form.startDate) {
      setMessage({ type: "error", text: "A data final não pode ser anterior à data inicial." });
      return;
    }
    if (form.startDate === (form.endDate || form.startDate) && form.startTime && form.endTime && form.endTime <= form.startTime) {
      setMessage({ type: "error", text: "O horário final precisa ser posterior ao horário inicial." });
      return;
    }
    savingRef.current = true;
    setSaving(true);
    try {
      const created = await api.createAgendaEvent({ ...form, description: form.description || null, endDate: form.endDate || null, startTime: form.startTime || null, endTime: form.endTime || null, location: form.location || null, taskId: form.taskId || null });
      setOpen(false);
      setMessage({ type: "success", text: `Evento ${created.title} adicionado à agenda.` });
      navigate(`/agenda?mes=${monthValue(parseMonth(created.startDate))}`);
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Não foi possível salvar o evento." });
    } finally {
      savingRef.current = false;
      setSaving(false);
    }
  }

  return (
    <DashboardLayout styles={["/css/agenda.css"]}>
      <main className="dashboard"><div className="container agenda-page">
        <header className="agenda-header">
          <div><p className="eyebrow dark mb-0">{cityHall.name}</p><h1>Agenda Municipal</h1><span>Eventos, compromissos e prazos das tarefas em um só lugar.</span></div>
          {access?.canCreate && <button className="btn btn-primary" type="button" onClick={() => { setForm(emptyForm); setOpen(true); }}><i className="bi bi-calendar-plus"></i> Novo evento</button>}
        </header>

        {message && <div className={`auth-message ${message.type} mb-3`} role="status"><i className={`bi ${message.type === "success" ? "bi-check-circle-fill" : "bi-exclamation-circle-fill"}`}></i>{message.text}</div>}
        {selectedTaskId && <div className="agenda-filter-notice"><i className="bi bi-link-45deg"></i> Exibindo a agenda da tarefa selecionada. <Link to={`/agenda?mes=${monthValue(monthDate)}`}>Remover filtro</Link></div>}

        <div className="agenda-layout">
          <section className="agenda-calendar" aria-label={`Calendário de ${calendarMonthLabel}`}>
            <div className="agenda-toolbar"><div><p className="eyebrow dark mb-0">Calendário mensal</p><h2>{calendarMonthLabel}</h2></div><nav className="agenda-month-nav" aria-label="Navegação entre meses">
              <button type="button" onClick={() => moveMonth(-1)} title="Mês anterior" aria-label="Mês anterior"><i className="bi bi-chevron-left"></i></button><button type="button" className="agenda-today-link" onClick={openToday}>Hoje</button><button type="button" onClick={() => moveMonth(1)} title="Próximo mês" aria-label="Próximo mês"><i className="bi bi-chevron-right"></i></button>
            </nav></div>
            <div className="agenda-calendar-scroll"><div className="agenda-weekdays" aria-hidden="true"><span>Dom</span><span>Seg</span><span>Ter</span><span>Qua</span><span>Qui</span><span>Sex</span><span>Sáb</span></div><div className="agenda-month-grid" aria-busy={loading}>
              {days.map((day) => <div className={`agenda-day ${day.inMonth ? "" : "is-outside"} ${day.isToday ? "is-today" : ""}`} key={day.key}><time dateTime={day.key}>{day.date.getDate()}</time><div className="agenda-day-items">
                {day.items.slice(0, 3).map((item) => <div className={`agenda-day-item item-${item.kind}`} title={item.title} key={`${item.kind}-${item.id}`}><i className={`bi ${item.kind === "task" ? "bi-check2-square" : typeIcons[item.type] || typeIcons.OTHER}`}></i><span>{item.title}</span></div>)}
                {day.items.length > 3 && <small>Mais eventos</small>}
              </div></div>)}
            </div></div>
            <div className="agenda-legend"><span><i className="bi bi-calendar-event-fill"></i> Eventos</span><span><i className="bi bi-check2-square"></i> Prazos de tarefas</span></div>
          </section>

          <aside className="agenda-events-panel"><div className="agenda-panel-heading"><div><p className="eyebrow dark mb-0">Programação</p><h2>Eventos do mês</h2></div><span>{events.length}</span></div><div className="agenda-event-list">
            {events.map((event) => <article className={`agenda-event-card tipo-${String(event.type || "OTHER").toLowerCase()}`} key={event.id}><div className="agenda-event-icon"><i className={`bi ${typeIcons[event.type] || typeIcons.OTHER}`}></i></div><div className="agenda-event-body">
              <span className="agenda-event-type">{typeLabels[event.type] || "Outro"}</span><h3>{event.title}</h3><div className="agenda-event-meta"><span><i className="bi bi-calendar3"></i> {formatDate(event.startDate)}{event.endDate && event.endDate !== event.startDate ? ` a ${formatDate(event.endDate)}` : ""}</span>{event.startTime && <span><i className="bi bi-clock"></i> {event.startTime.slice(0, 5)}{event.endTime ? ` às ${event.endTime.slice(0, 5)}` : ""}</span>}{event.location && <span><i className="bi bi-geo-alt"></i> {event.location}</span>}</div>
              {event.taskId && (access?.canViewAllTasks || event.taskSectorName === access?.sectorName) ? <Link className="agenda-task-link" to={`/tarefas?setor=${encodeURIComponent(event.taskSectorId || "")}`}><i className="bi bi-link-45deg"></i> Tarefa: {event.taskTitle}</Link> : event.taskId && <span className="agenda-task-link"><i className="bi bi-link-45deg"></i> Tarefa vinculada</span>}
            </div></article>)}
            {!events.length && <div className="agenda-empty"><i className="bi bi-calendar2-x"></i><span>Nenhum evento cadastrado neste mês.</span></div>}
          </div></aside>
        </div>
      </div></main>

      {open && access?.canCreate && <div className="react-modal-backdrop" role="dialog" aria-modal="true" onMouseDown={(event) => event.target === event.currentTarget && closeModal()}><div className="react-modal-card agenda-modal" onMouseDown={(event) => event.stopPropagation()}><div className="task-modal-header"><div><h2 className="modal-title fs-5">Novo evento</h2><small>Agenda de {cityHall.name}</small></div><button type="button" className="agenda-modal-close" onClick={closeModal} aria-label="Fechar"><i className="bi bi-x-lg"></i></button></div><form onSubmit={submit}>
        <div className="modal-body agenda-form-grid">
          <div className="agenda-field agenda-field-wide"><label htmlFor="agenda-title">Título</label><input id="agenda-title" required maxLength="160" value={form.title} onChange={(event) => updateForm("title", event.target.value)} /></div>
          <div className="agenda-field"><label htmlFor="agenda-type">Tipo</label><select id="agenda-type" value={form.type} onChange={(event) => updateForm("type", event.target.value)}>{Object.entries(typeLabels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></div>
          <div className="agenda-field"><label htmlFor="agenda-location">Local</label><input id="agenda-location" maxLength="180" value={form.location} onChange={(event) => updateForm("location", event.target.value)} /></div>
          <div className="agenda-field"><label htmlFor="agenda-start-date">Data inicial</label><input id="agenda-start-date" type="date" required value={form.startDate} onChange={(event) => updateForm("startDate", event.target.value)} /></div>
          <div className="agenda-field"><label htmlFor="agenda-end-date">Data final</label><input id="agenda-end-date" type="date" value={form.endDate} onChange={(event) => updateForm("endDate", event.target.value)} /></div>
          <div className="agenda-field"><label htmlFor="agenda-start-time">Horário inicial</label><input id="agenda-start-time" type="time" value={form.startTime} onChange={(event) => updateForm("startTime", event.target.value)} /></div>
          <div className="agenda-field"><label htmlFor="agenda-end-time">Horário final</label><input id="agenda-end-time" type="time" value={form.endTime} onChange={(event) => updateForm("endTime", event.target.value)} /></div>
          <div className="agenda-field agenda-field-wide"><label htmlFor="agenda-task">Tarefa relacionada</label><select id="agenda-task" value={form.taskId} onChange={(event) => updateForm("taskId", event.target.value)}><option value="">Nenhuma tarefa vinculada</option>{taskOptions.map((task) => <option value={task.id} key={task.id}>{task.title}</option>)}</select><small>Opcional. Apenas tarefas acessíveis da prefeitura são exibidas.</small></div>
          <div className="agenda-field agenda-field-wide"><label htmlFor="agenda-description">Descrição</label><textarea id="agenda-description" rows="4" value={form.description} onChange={(event) => updateForm("description", event.target.value)}></textarea></div>
        </div>
        <div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={closeModal} disabled={saving}>Cancelar</button><button type="submit" className="btn btn-primary" disabled={saving}><i className="bi bi-calendar-check"></i> {saving ? "Adicionando..." : "Adicionar evento"}</button></div>
      </form></div></div>}
    </DashboardLayout>
  );
}

import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";

const emptyForm = {
  title: "",
  description: "",
  type: "MEETING",
  startDate: "",
  endDate: "",
  startTime: "",
  endTime: "",
  location: "",
  taskId: "",
};

const typeLabels = {
  MEETING: "Reuniao",
  DEADLINE: "Prazo",
  SERVICE: "Atendimento",
  CEREMONY: "Cerimonia",
  OTHER: "Outro",
};

function pageItems(payload) {
  return Array.isArray(payload) ? payload : payload?.content || [];
}

function monthValue(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function dateKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function buildDays(monthDate, events, tasks) {
  const first = new Date(monthDate.getFullYear(), monthDate.getMonth(), 1);
  const start = new Date(first);
  start.setDate(start.getDate() - start.getDay());
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start);
    date.setDate(start.getDate() + index);
    const key = dateKey(date);
    const items = events.filter((event) => {
      const end = event.endDate || event.startDate;
      return event.startDate <= key && end >= key;
    });
    const deadlines = tasks.filter((task) => task.endDate?.slice(0, 10) === key);
    return { date, key, items, deadlines, inMonth: date.getMonth() === monthDate.getMonth() };
  });
}

export default function AgendaPage() {
  const [monthDate, setMonthDate] = useState(() => new Date());
  const [events, setEvents] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const [eventPayload, taskPayload] = await Promise.all([
        api.getAgendaEvents(monthValue(monthDate)),
        api.getTasks(),
      ]);
      setEvents(pageItems(eventPayload));
      setTasks(pageItems(taskPayload));
      setMessage(null);
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [monthDate]);

  const days = useMemo(() => buildDays(monthDate, events, tasks), [monthDate, events, tasks]);
  const label = new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" }).format(monthDate);

  function showCreate(date = null) {
    setEditingId(null);
    setForm({ ...emptyForm, startDate: date || "" });
    setOpen(true);
  }

  function showEdit(event) {
    setEditingId(event.id);
    setForm({
      title: event.title || "",
      description: event.description || "",
      type: event.type || "MEETING",
      startDate: event.startDate || "",
      endDate: event.endDate || "",
      startTime: event.startTime?.slice(0, 5) || "",
      endTime: event.endTime?.slice(0, 5) || "",
      location: event.location || "",
      taskId: event.taskId || "",
    });
    setOpen(true);
  }

  async function submit(event) {
    event.preventDefault();
    if (form.endDate && form.endDate < form.startDate) {
      setMessage({ type: "danger", text: "A data final nao pode ser anterior a inicial." });
      return;
    }
    const payload = {
      ...form,
      description: form.description || null,
      endDate: form.endDate || null,
      startTime: form.startTime || null,
      endTime: form.endTime || null,
      location: form.location || null,
      taskId: form.taskId || null,
    };
    try {
      if (editingId) await api.updateAgendaEvent(editingId, payload);
      else await api.createAgendaEvent(payload);
      setOpen(false);
      setMessage({ type: "success", text: editingId ? "Evento atualizado." : "Evento adicionado a agenda." });
      await load();
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    }
  }

  async function remove() {
    if (!editingId || !window.confirm("Excluir este evento?")) return;
    try {
      await api.deleteAgendaEvent(editingId);
      setOpen(false);
      await load();
    } catch (error) {
      setMessage({ type: "danger", text: error.message });
    }
  }

  function moveMonth(delta) {
    setMonthDate((current) => new Date(current.getFullYear(), current.getMonth() + delta, 1));
  }

  return (
    <DashboardLayout styles={["/css/agenda.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader
            eyebrow="Compromissos municipais"
            title="Agenda"
            action={<button className="btn btn-primary" onClick={() => showCreate()}><i className="bi bi-plus-lg"></i> Novo evento</button>}
          />
          {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}

          <section className="agenda-panel">
            <header className="agenda-toolbar">
              <button className="btn-acao" onClick={() => moveMonth(-1)} aria-label="Mes anterior"><i className="bi bi-chevron-left"></i></button>
              <h2>{label}</h2>
              <button className="btn-acao" onClick={() => moveMonth(1)} aria-label="Proximo mes"><i className="bi bi-chevron-right"></i></button>
            </header>
            <div className="agenda-weekdays">{["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab"].map((day) => <span key={day}>{day}</span>)}</div>
            <div className="agenda-grid" aria-busy={loading}>
              {days.map((day) => (
                <article className={`agenda-day ${day.inMonth ? "" : "outside"}`} key={day.key} onDoubleClick={() => showCreate(day.key)}>
                  <span className="agenda-day-number">{day.date.getDate()}</span>
                  {day.items.map((item) => (
                    <button className={`agenda-item type-${item.type.toLowerCase()}`} key={item.id} onClick={() => showEdit(item)} title={item.title}>
                      {item.startTime && <small>{item.startTime.slice(0, 5)}</small>} {item.title}
                    </button>
                  ))}
                  {day.deadlines.map((task) => <div className="agenda-item task-deadline" key={`task-${task.id}`}><i className="bi bi-check2-square"></i> {task.title}</div>)}
                </article>
              ))}
            </div>
          </section>
        </div>
      </main>

      {open && (
        <div className="react-modal-backdrop" role="dialog" aria-modal="true">
          <div className="react-modal-card agenda-modal">
            <div className="task-modal-header">
              <h3>{editingId ? "Editar evento" : "Novo evento"}</h3>
              <button className="btn-acao" onClick={() => setOpen(false)} aria-label="Fechar"><i className="bi bi-x-lg"></i></button>
            </div>
            <form onSubmit={submit} className="row g-3">
              <div className="col-12"><label className="field-label">Titulo *</label><input className="field-input" required maxLength="160" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></div>
              <div className="col-md-6"><label className="field-label">Tipo *</label><select className="field-input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>{Object.entries(typeLabels).map(([value, text]) => <option value={value} key={value}>{text}</option>)}</select></div>
              <div className="col-md-6"><label className="field-label">Local</label><input className="field-input" maxLength="180" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} /></div>
              <div className="col-md-6"><label className="field-label">Data inicial *</label><input className="field-input" type="date" required value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} /></div>
              <div className="col-md-6"><label className="field-label">Data final</label><input className="field-input" type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} /></div>
              <div className="col-md-6"><label className="field-label">Horario inicial</label><input className="field-input" type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} /></div>
              <div className="col-md-6"><label className="field-label">Horario final</label><input className="field-input" type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} /></div>
              <div className="col-12"><label className="field-label">Tarefa relacionada</label><select className="field-input" value={form.taskId} onChange={(e) => setForm({ ...form, taskId: e.target.value })}><option value="">Nenhuma</option>{tasks.map((task) => <option key={task.id} value={task.id}>{task.title}</option>)}</select></div>
              <div className="col-12"><label className="field-label">Descricao</label><textarea className="field-input" rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}></textarea></div>
              <div className="col-12 d-flex justify-content-between gap-2">
                <div>{editingId && <button type="button" className="btn btn-outline-danger" onClick={remove}>Excluir</button>}</div>
                <div className="d-flex gap-2"><button type="button" className="btn btn-outline-secondary" onClick={() => setOpen(false)}>Cancelar</button><button className="btn btn-primary" type="submit">Salvar</button></div>
              </div>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  );
}

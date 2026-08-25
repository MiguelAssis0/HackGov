import { useEffect, useMemo, useState } from "react";
import { api } from "../services/api.js";

function durationLabel(seconds = 0) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (hours && minutes) return `${hours}h ${minutes}min`;
  if (hours) return `${hours}h`;
  return minutes ? `${minutes}min` : "<1min";
}

export function TaskDetailPanel({ task, onClose, onMessage }) {
  const [details, setDetails] = useState(null);
  const [comment, setComment] = useState("");
  const [checklistTitle, setChecklistTitle] = useState("");
  const [manual, setManual] = useState({ referenceDate: new Date().toISOString().slice(0, 10), hours: "", observation: "" });
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setDetails(await api.getTaskDetails(task.id));
    } catch (error) {
      onMessage({ type: "error", text: error.message });
    }
  }

  useEffect(() => { load(); }, [task.id]);

  const activeTimer = useMemo(() => details?.timeEntries?.find((entry) => entry.active), [details]);
  const totalSeconds = useMemo(() => details?.timeEntries?.reduce((total, entry) => total + (entry.durationSeconds || 0), 0) || 0, [details]);

  async function run(action) {
    setBusy(true);
    try {
      await action();
      await load();
    } catch (error) {
      onMessage({ type: "error", text: error.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="react-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="taskDetailTitle">
      <div className="react-modal-card task-detail-modal">
        <div className="task-modal-header">
          <div><p className="section-label mb-1">{task.sectorName || "Tarefa"}</p><h3 id="taskDetailTitle">{task.title}</h3></div>
          <button className="btn-acao" onClick={onClose} aria-label="Fechar"><i className="bi bi-x-lg"></i></button>
        </div>
        {!details ? <div className="empty-state">Carregando detalhes...</div> : <div className="task-detail-grid">
          <section>
            <h4>Checklist</h4>
            <form className="d-flex gap-2 mb-3" onSubmit={(event) => { event.preventDefault(); if (checklistTitle.trim()) run(() => api.addTaskChecklist(task.id, checklistTitle).then(() => setChecklistTitle(""))); }}>
              <input className="field-input" placeholder="Novo item" value={checklistTitle} onChange={(e) => setChecklistTitle(e.target.value)} /><button className="btn btn-primary" disabled={busy}>Adicionar</button>
            </form>
            <div className="task-detail-list">
              {details.checklist.map((item) => <label className="task-check-item" key={item.id}><input type="checkbox" checked={item.completed} onChange={() => run(() => api.toggleTaskChecklist(task.id, item.id))} /><span>{item.title}</span><button type="button" className="table-action-btn danger" onClick={() => run(() => api.deleteTaskChecklist(task.id, item.id))}><i className="bi bi-trash"></i></button></label>)}
              {details.checklist.length === 0 && <small className="text-muted">Nenhum item cadastrado.</small>}
            </div>

            <h4 className="mt-4">Comentarios</h4>
            <form className="d-flex gap-2 mb-3" onSubmit={(event) => { event.preventDefault(); if (comment.trim()) run(() => api.addTaskComment(task.id, comment).then(() => setComment(""))); }}>
              <input className="field-input" maxLength="2000" placeholder="Escreva um comentario" value={comment} onChange={(e) => setComment(e.target.value)} /><button className="btn btn-primary" disabled={busy}>Enviar</button>
            </form>
            <div className="task-detail-list">{details.comments.map((item) => <article className="task-comment" key={item.id}><strong>{item.authorName || "Usuario"}</strong><p>{item.text}</p><small>{new Date(item.createdAt).toLocaleString("pt-BR")}</small></article>)}</div>
          </section>

          <aside>
            <h4>Tempo trabalhado</h4>
            <div className="task-time-summary"><strong>{durationLabel(totalSeconds)}</strong><span>Total registrado</span></div>
            <button className={`btn ${activeTimer ? "btn-warning" : "btn-primary"} w-100 mb-3`} disabled={busy} onClick={() => run(() => activeTimer ? api.pauseTaskTimer(task.id) : api.startTaskTimer(task.id))}>
              <i className={`bi ${activeTimer ? "bi-pause-fill" : "bi-play-fill"}`}></i> {activeTimer ? "Pausar cronometro" : "Iniciar cronometro"}
            </button>
            <form className="task-manual-time" onSubmit={(event) => { event.preventDefault(); run(() => api.addTaskManualTime(task.id, { ...manual, hours: Number(manual.hours) }).then(() => setManual({ ...manual, hours: "", observation: "" }))); }}>
              <label className="field-label">Data</label><input className="field-input" type="date" max={new Date().toISOString().slice(0, 10)} required value={manual.referenceDate} onChange={(e) => setManual({ ...manual, referenceDate: e.target.value })} />
              <label className="field-label">Horas</label><input className="field-input" type="number" min="0.01" max="24" step="0.01" required value={manual.hours} onChange={(e) => setManual({ ...manual, hours: e.target.value })} />
              <label className="field-label">Justificativa</label><input className="field-input" required maxLength="500" value={manual.observation} onChange={(e) => setManual({ ...manual, observation: e.target.value })} />
              <button className="btn btn-outline-primary w-100 mt-2" disabled={busy}>Registrar horas</button>
            </form>
            <div className="task-detail-list mt-3">{details.timeEntries.slice(0, 5).map((entry) => <div className="task-time-row" key={entry.id}><span>{entry.employeeName}</span><strong>{durationLabel(entry.durationSeconds)}</strong></div>)}</div>

            <h4 className="mt-4">Anexos</h4>
            <label className="btn btn-outline-primary w-100 mb-2">Enviar arquivo<input type="file" hidden onChange={(event) => { const file = event.target.files?.[0]; if (file) run(() => api.addTaskAttachment(task.id, file)); event.target.value = ""; }} /></label>
            <div className="task-detail-list">{details.attachments.map((attachment) => <button className="task-attachment" key={attachment.id} onClick={() => api.downloadTaskAttachment(task.id, attachment)}><i className="bi bi-paperclip"></i><span>{attachment.originalName}</span><small>{Math.ceil(attachment.size / 1024)} KB</small></button>)}</div>
          </aside>
        </div>}
      </div>
    </div>
  );
}

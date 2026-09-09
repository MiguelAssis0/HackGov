import { useEffect, useMemo, useState } from "react";
import { api } from "../services/api.js";

const statusLabels = { TODO: "A fazer", IN_PROGRESS: "Em andamento", IN_REVIEW: "Em revisão", COMPLETED: "Concluída" };
const priorityLabels = { LOW: "Baixa", NORMAL: "Normal", HIGH: "Alta", URGENT: "Urgente" };

function dateLabel(value, withTime = false) {
  if (!value) return "Não definido";
  const date = new Date(typeof value === "string" && value.length === 10 ? `${value}T12:00:00` : value);
  if (Number.isNaN(date.getTime())) return "Não definido";
  return withTime
    ? date.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" })
    : date.toLocaleDateString("pt-BR");
}

function durationLabel(seconds = 0) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (hours && minutes) return `${hours}h ${minutes}min`;
  if (hours) return `${hours}h`;
  return minutes ? `${minutes}min` : "<1min";
}

function initials(name = "") {
  return name.split(" ").filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase() || "?";
}

function personName(person) {
  return person?.fullName || person?.name || [person?.firstName, person?.lastName].filter(Boolean).join(" ") || person?.email || "Servidor";
}

function dueState(task) {
  if (task.status === "COMPLETED") return "concluida";
  if (!task.endDate) return "sem_prazo";
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const deadline = new Date(task.endDate);
  deadline.setHours(0, 0, 0, 0);
  if (deadline < today) return "atrasada";
  if (deadline.getTime() === today.getTime()) return "hoje";
  return (deadline - today) / 86400000 <= 3 ? "proxima" : "futura";
}

function dueContent(state) {
  const values = {
    atrasada: ["bi-exclamation-triangle-fill", "Atrasada"],
    hoje: ["bi-alarm-fill", "Vence hoje"],
    proxima: ["bi-hourglass-split", "Próxima do vencimento"],
    futura: ["bi-check-circle-fill", "No prazo"],
    concluida: ["bi-check2-circle", "Concluída"],
    sem_prazo: ["bi-calendar2-minus", "Sem prazo definido"],
  };
  return values[state] || values.sem_prazo;
}

function historyItems(task, details) {
  const items = [];
  const add = (at, icon, title, description, id) => {
    if (at) items.push({ at, icon, title, description, id });
  };

  add(task.createdAt, "bi-plus-circle", "Tarefa criada", "Registro inicial", "created");
  if (task.updatedAt && String(task.updatedAt) !== String(task.createdAt)) {
    add(task.updatedAt, "bi-pencil-square", "Tarefa atualizada", "Dados da tarefa alterados", "updated");
  }
  add(task.completedAt, "bi-check2-circle", "Tarefa concluída", "Status alterado para concluída", "completed");
  (details?.comments || []).forEach((item) => add(item.createdAt, "bi-chat-square-text", "Comentário adicionado", item.authorName || "Usuário", `comment-${item.id}`));
  (details?.checklist || []).filter((item) => item.completedAt).forEach((item) => add(item.completedAt, "bi-check-square", "Etapa concluída", item.title, `check-${item.id}`));
  (details?.timeEntries || []).forEach((item) => add(item.startedAt || item.referenceDate, "bi-stopwatch", item.manual ? "Horas registradas manualmente" : "Cronômetro registrado", item.employeeName || "Usuário", `time-${item.id}`));
  (details?.attachments || []).forEach((item) => add(item.createdAt, "bi-paperclip", "Anexo adicionado", item.originalName, `attachment-${item.id}`));

  return items.sort((left, right) => new Date(right.at) - new Date(left.at));
}

export function TaskDetailPanel({ task, onClose, onMessage, canManage = false, canDelete = false, onEdit, onDelete }) {
  const [details, setDetails] = useState(null);
  const [comment, setComment] = useState("");
  const [editingComment, setEditingComment] = useState(null);
  const [checklistTitle, setChecklistTitle] = useState("");
  const [manual, setManual] = useState({ referenceDate: new Date().toISOString().slice(0, 10), hours: "", observation: "" });
  const [attachmentFile, setAttachmentFile] = useState(null);
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      setDetails(await api.getTaskDetails(task.id));
    } catch (error) {
      onMessage({ type: "error", text: error.message });
    }
  }

  useEffect(() => { load(); }, [task.id]);

  const checklist = details?.checklist || [];
  const comments = details?.comments || [];
  const timeEntries = details?.timeEntries || [];
  const attachments = details?.attachments || [];
  const activeTimer = timeEntries.find((entry) => entry.active);
  const totalSeconds = timeEntries.reduce((total, entry) => total + (entry.durationSeconds || 0), 0);
  const checklistProgress = checklist.length ? Math.round((checklist.filter((item) => item.completed).length / checklist.length) * 100) : 0;
  const peopleTime = useMemo(() => {
    const grouped = new Map();
    timeEntries.forEach((entry) => {
      const key = entry.employeeId || entry.employeeName || "unknown";
      const current = grouped.get(key) || { name: entry.employeeName || "Usuário", seconds: 0 };
      current.seconds += entry.durationSeconds || 0;
      grouped.set(key, current);
    });
    return [...grouped.values()];
  }, [timeEntries]);
  const history = historyItems(task, details);
  const due = dueContent(dueState(task));

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

  function submitComment(event) {
    event.preventDefault();
    if (!comment.trim()) return;
    run(() => api.addTaskComment(task.id, comment).then(() => setComment("")));
  }

  function submitChecklist(event) {
    event.preventDefault();
    if (!checklistTitle.trim()) return;
    run(() => api.addTaskChecklist(task.id, checklistTitle).then(() => setChecklistTitle("")));
  }

  function submitManualTime(event) {
    event.preventDefault();
    run(() => api.addTaskManualTime(task.id, { ...manual, hours: Number(manual.hours) }).then(() => setManual({ ...manual, hours: "", observation: "" })));
  }

  return (
    <div className="react-modal-backdrop task-detail-backdrop" role="dialog" aria-modal="true" aria-labelledby="taskDetailTitle" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="react-modal-card task-detail-modal" onMouseDown={(event) => event.stopPropagation()}>
        <header className="task-detail-header">
          <div className="task-detail-heading">
            <div className="task-detail-labels">
              <span className={`task-detail-status status-${String(task.status || "TODO").toLowerCase()}`}>{statusLabels[task.status] || task.status}</span>
              <span className={`task-detail-priority prioridade-${String(task.priority || "NORMAL").toLowerCase()}`}>{priorityLabels[task.priority] || task.priority}</span>
              <span className={`task-detail-due due-${dueState(task)}`}><i className={`bi ${due[0]}`}></i> {due[1]}</span>
            </div>
            <h1 id="taskDetailTitle">{task.title}</h1>
            <p>{task.description || "Sem descrição cadastrada."}</p>
          </div>
          <div className="task-detail-header-actions">
            {canDelete && onDelete && <button className="btn btn-outline-danger" type="button" onClick={onDelete}><i className="bi bi-trash3"></i> Excluir</button>}
            {canManage && onEdit && <button className="btn btn-primary" type="button" onClick={onEdit}><i className="bi bi-pencil-square"></i> Editar tarefa</button>}
            <button className="btn btn-outline-secondary task-detail-close" type="button" onClick={onClose} aria-label="Fechar"><i className="bi bi-x-lg"></i></button>
          </div>
        </header>

        {!details ? <div className="task-detail-loading">Carregando detalhes...</div> : <>
          <section className="task-detail-metrics" aria-label="Indicadores da tarefa">
            <div><i className="bi bi-gem"></i><span>Valor público</span><strong>{task.businessPoints || 0} pontos</strong></div>
            <div><i className="bi bi-play-circle"></i><span>Data de início</span><strong>{dateLabel(task.startDate)}</strong></div>
            <div><i className="bi bi-flag-fill"></i><span>Prazo de entrega</span><strong>{dateLabel(task.endDate)}</strong></div>
            <div><i className="bi bi-list-check"></i><span>Checklist</span><strong>{checklist.filter((item) => item.completed).length}/{checklist.length} concluídos</strong></div>
          </section>

          <div className="task-detail-layout">
            <main className="task-detail-main">
              <section className="task-detail-panel"><div className="task-detail-panel-head"><div><p className="eyebrow dark mb-0">Entrega pública</p><h2>Resultado esperado</h2></div><i className="bi bi-bullseye"></i></div><div className="task-expected-result">{task.expectedResult || "Nenhum resultado esperado foi informado."}</div></section>

              <section className="task-detail-panel"><div className="task-detail-panel-head"><div><p className="eyebrow dark mb-0">Esforço da equipe</p><h2>Tempo trabalhado</h2></div><strong className="task-total-time">{durationLabel(totalSeconds)}</strong></div>
                {canManage && task.status !== "COMPLETED" && <div className="task-time-controls"><button className={`btn ${activeTimer ? "btn-outline-secondary task-timer-btn is-active" : "btn-primary"}`} disabled={busy} onClick={() => run(() => activeTimer ? api.pauseTaskTimer(task.id) : api.startTaskTimer(task.id))}><i className={`bi ${activeTimer ? "bi-pause-fill" : "bi-play-fill"}`}></i> {activeTimer ? "Pausar" : "Iniciar cronômetro"}</button></div>}
                <div className="task-time-people">{peopleTime.length ? peopleTime.map((person) => <div key={person.name}><span>{initials(person.name)}</span><strong>{person.name}</strong><time>{durationLabel(person.seconds)}</time></div>) : <div className="task-detail-empty"><i className="bi bi-stopwatch"></i> Nenhuma hora registrada.</div>}</div>
                {canManage && <details className="task-manual-time"><summary><i className="bi bi-clock-history"></i> Adicionar horas manualmente</summary><form onSubmit={submitManualTime}><div><label htmlFor="task-time-date">Data trabalhada</label><input id="task-time-date" className="field-input" type="date" max={new Date().toISOString().slice(0, 10)} required value={manual.referenceDate} onChange={(event) => setManual({ ...manual, referenceDate: event.target.value })} /></div><div><label htmlFor="task-time-hours">Tempo trabalhado</label><input id="task-time-hours" className="field-input" type="number" min="0.01" max="24" step="0.01" required value={manual.hours} onChange={(event) => setManual({ ...manual, hours: event.target.value })} /></div><div className="task-manual-time-wide"><label htmlFor="task-time-observation">Justificativa</label><input id="task-time-observation" className="field-input" maxLength="500" required value={manual.observation} onChange={(event) => setManual({ ...manual, observation: event.target.value })} /></div><button className="btn btn-outline-secondary" disabled={busy}><i className="bi bi-plus-circle"></i> Registrar horas</button></form></details>}
                {!!timeEntries.length && <div className="task-time-entries">{timeEntries.map((entry) => <div key={entry.id}><i className={`bi ${entry.manual ? "bi-pencil-square" : entry.active ? "bi-play-circle-fill" : "bi-stopwatch-fill"}`}></i><span><strong>{entry.employeeName || "Usuário"}</strong><small>{dateLabel(entry.referenceDate)} · {entry.manual ? "Manual" : entry.active ? "Em andamento" : "Cronômetro"}</small>{entry.observation && <small>{entry.observation}</small>}</span><time>{entry.active ? "Sessão atual" : durationLabel(entry.durationSeconds)}</time>{canManage && <button type="button" className="task-icon-button danger" onClick={() => run(() => api.deleteTaskTime(task.id, entry.id))} aria-label="Remover apontamento"><i className="bi bi-trash3"></i></button>}</div>)}</div>}
              </section>

              <section className="task-detail-panel"><div className="task-detail-panel-head"><div><p className="eyebrow dark mb-0">Execução</p><h2>Checklist de entregas</h2></div><strong>{checklistProgress}%</strong></div><progress className="task-checklist-progress" value={checklistProgress} max="100">{checklistProgress}%</progress><div className="task-checklist-list">{checklist.map((item) => <div className={`task-checklist-item ${item.completed ? "is-done" : ""}`} key={item.id}><button type="button" className="task-check-button" disabled={!canManage || busy} onClick={() => run(() => api.toggleTaskChecklist(task.id, item.id))} aria-label={item.completed ? "Reabrir item" : "Concluir item"}><i className={`bi ${item.completed ? "bi-check-square-fill" : "bi-square"}`}></i></button><span>{item.title}</span>{canManage && <button type="button" className="task-icon-button danger" onClick={() => run(() => api.deleteTaskChecklist(task.id, item.id))} aria-label="Excluir item"><i className="bi bi-trash3"></i></button>}</div>)}{!checklist.length && <div className="task-detail-empty"><i className="bi bi-list-check"></i> Nenhuma etapa cadastrada.</div>}</div>{canManage && <form className="task-inline-form" onSubmit={submitChecklist}><input className="field-input" maxLength="180" placeholder="Nova etapa" required value={checklistTitle} onChange={(event) => setChecklistTitle(event.target.value)} /><button className="btn btn-outline-secondary" disabled={busy}><i className="bi bi-plus-lg"></i> Adicionar</button></form>}</section>

              <section className="task-detail-panel"><div className="task-detail-panel-head"><div><p className="eyebrow dark mb-0">Colaboração</p><h2>Comentários</h2></div><span>{comments.length}</span></div><div className="task-comments-list">{comments.map((item) => <article className="task-comment" key={item.id}><div className="task-comment-avatar"><i className="bi bi-person-fill"></i></div><div><header><div><strong>{item.authorName || "Usuário removido"}</strong><div className="task-comment-meta"><time>{dateLabel(item.createdAt, true)}</time>{item.editedAt && <span className="task-comment-edited"><i className="bi bi-pencil-square"></i> Editado</span>}</div></div>{canManage && <div className="task-comment-actions"><button type="button" onClick={() => setEditingComment({ id: item.id, text: item.text })} aria-label="Editar comentário"><i className="bi bi-pencil"></i></button><button type="button" className="task-comment-delete" onClick={() => run(() => api.deleteTaskComment(task.id, item.id))} aria-label="Excluir comentário"><i className="bi bi-trash3"></i></button></div>}</header><p>{item.text}</p></div></article>)}</div>{editingComment && <form className="task-comment-edit-form" onSubmit={(event) => { event.preventDefault(); run(() => api.updateTaskComment(task.id, editingComment.id, editingComment.text).then(() => setEditingComment(null))); }}><label htmlFor="task-comment-edit">Comentário</label><textarea id="task-comment-edit" maxLength="2000" required value={editingComment.text} onChange={(event) => setEditingComment({ ...editingComment, text: event.target.value })}></textarea><div><button type="button" className="btn btn-outline-secondary" onClick={() => setEditingComment(null)}>Cancelar</button><button className="btn btn-primary" disabled={busy}>Salvar</button></div></form>}{canManage && <form className="task-comment-form" onSubmit={submitComment}><textarea maxLength="2000" rows="3" placeholder="Escreva um comentário" required value={comment} onChange={(event) => setComment(event.target.value)}></textarea><button className="btn btn-primary" disabled={busy}><i className="bi bi-send"></i> Comentar</button></form>}</section>

              <section className="task-detail-panel"><div className="task-detail-panel-head"><div><p className="eyebrow dark mb-0">Auditoria operacional</p><h2>Histórico da tarefa</h2></div><i className="bi bi-clock-history"></i></div><div className="task-history-list">{history.map((item) => <article className="task-history-item" key={item.id}><span className="task-history-icon"><i className={`bi ${item.icon}`}></i></span><div><strong>{item.title}</strong><p>{item.description}</p><time>{dateLabel(item.at, true)}</time></div></article>)}{!history.length && <div className="task-detail-empty"><i className="bi bi-clock-history"></i> Nenhuma movimentação registrada.</div>}</div></section>
            </main>

            <aside className="task-detail-sidebar">
              <section className="task-detail-panel"><div className="task-detail-panel-head"><h2>Gestão</h2><i className="bi bi-people-fill"></i></div><dl className="task-management-list"><div><dt>Setor responsável</dt><dd>{task.sectorName || "Não informado"}</dd></div><div><dt>Setor solicitante</dt><dd>Demanda interna</dd></div><div><dt>Protocolo/processo</dt><dd>{task.protocol || "Não informado"}</dd></div><div><dt>Criada em</dt><dd>{dateLabel(task.createdAt, true)}</dd></div></dl><div className="task-detail-responsibles"><span>Responsáveis</span>{(task.responsibles || []).map((person) => <div key={person.id}><span>{initials(personName(person))}</span><strong>{personName(person)}</strong></div>)}</div></section>

              <section className="task-detail-panel"><div className="task-detail-panel-head"><h2>Anexos</h2><span>{attachments.length}</span></div><div className="task-attachments-list">{attachments.map((attachment) => <div className="task-attachment-item" key={attachment.id}><button type="button" onClick={() => api.downloadTaskAttachment(task.id, attachment)}><i className="bi bi-paperclip"></i><span><strong>{attachment.originalName}</strong><small>{attachment.uploadedByName || "Sistema"} · {dateLabel(attachment.createdAt, true)}</small></span><i className="bi bi-download"></i></button></div>)}{!attachments.length && <div className="task-detail-empty"><i className="bi bi-paperclip"></i> Nenhum anexo.</div>}</div>{canManage && <form className="task-attachment-form" onSubmit={(event) => { event.preventDefault(); if (!attachmentFile) return; run(() => api.addTaskAttachment(task.id, attachmentFile).then(() => setAttachmentFile(null))); }}><input className="field-input" type="file" accept=".pdf,.doc,.docx,.xls,.xlsx,.csv,.txt,.png,.jpg,.jpeg" onChange={(event) => setAttachmentFile(event.target.files?.[0] || null)} /><button className="btn btn-outline-secondary" type="submit" disabled={!attachmentFile || busy}><i className="bi bi-cloud-arrow-up"></i> Anexar</button></form>}</section>

              <section className="task-detail-panel"><div className="task-detail-panel-head"><h2>Integrações</h2><i className="bi bi-diagram-3"></i></div><div className="task-integrations-list"><a href={`/agenda?tarefa=${task.id}`}><i className="bi bi-calendar2-check-fill"></i><span><strong>Agenda Municipal</strong><small>Consultar eventos relacionados</small></span></a><a href="/documentos"><i className="bi bi-file-earmark-text-fill"></i><span><strong>Documentos</strong><small>Consultar arquivos oficiais do setor</small></span></a><a href="/caixa-entrada"><i className="bi bi-inbox-fill"></i><span><strong>Caixa de Entrada</strong><small>Atribuições e demandas relacionadas</small></span></a></div></section>
            </aside>
          </div>
        </>}
      </div>
    </div>
  );
}

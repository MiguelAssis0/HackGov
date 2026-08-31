import { useEffect, useState } from "react";
import { useParams, useSearchParams, useNavigate } from "react-router-dom";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api, getSelectedCityHall, getStoredUser } from "../services/api.js";

const typeLabels = { TASK: "Tarefa", DOCUMENT: "Documento", ALERT: "Alerta", REQUEST: "Solicitacao" };
const statusLabels = { NEW: "Nova", IN_PROGRESS: "Em andamento", COMPLETED: "Concluida", ARCHIVED: "Arquivada" };
const priorityLabels = { LOW: "Baixa", NORMAL: "Normal", HIGH: "Alta" };

function formatDate(v){ if(!v) return "—"; try{ const d=new Date(v); return d.toLocaleString("pt-BR",{day:"2-digit",month:"2-digit",year:"numeric",hour:"2-digit",minute:"2-digit"});}catch{ return v; } }

export default function InboxDetailPage(){
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [entrada, setEntrada] = useState(null);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState("");
  const [message, setMessage] = useState(null);
  const backUrl = searchParams.get("next") || "/caixa-entrada";
  const cityHall = getSelectedCityHall() || { name: getStoredUser()?.prefeitura || "Prefeitura" };
  const user = getStoredUser();
  const canViewAll = ["admin_cidade","admin_equipe"].includes(user?.tipoUsuario || user?.role);

  useEffect(()=>{
    let mounted=true;
    api.getInboxEntry(id).then((data)=>{ if(mounted) setEntrada(data); }).catch((e)=> setMessage({type:"danger",text:e.message})).finally(()=> setLoading(false));
    return ()=>{ mounted=false; };
  },[id]);

  async function doAction(action, extra={}){
    try{
      let next;
      if(action==="assumir") next = await api.claimInboxEntry(id);
      else if(action==="liberar") next = await api.releaseInboxEntry(id);
      else if(action==="concluir") next = await api.completeInboxEntry(id);
      else if(action==="reabrir") next = await api.reopenInboxEntry(id);
      else if(action==="aceitar") { await api.acceptTaskRequest(entrada.objectId, feedback); next = await api.getInboxEntry(id); setMessage({type:"success",text:"Demanda aceita e enviada para o quadro kanban do setor."}); }
      else if(action==="recusar") { if(!feedback.trim()) { setMessage({type:"danger",text:"Informe o motivo da recusa."}); return; } await api.rejectTaskRequest(entrada.objectId, feedback); next = await api.getInboxEntry(id); setMessage({type:"success",text:"Demanda recusada com feedback ao setor solicitante."}); }
      if(next) setEntrada(next);
    } catch(e){ setMessage({type:"danger",text:e.message}); }
  }

  const isPersonalTask = entrada && entrada.objectType==="task" && !!entrada.destinationEmployeeId;
  const isTaskRequest = entrada && entrada.objectType==="cross_sector_task_request" && entrada.status!=="COMPLETED";

  if(loading) return <DashboardLayout styles={["/css/caixa-entrada.css"]}><div className="inbox-empty">Carregando...</div></DashboardLayout>;
  if(!entrada) return <DashboardLayout styles={["/css/caixa-entrada.css"]}><div className="inbox-empty">Entrada não encontrada. <Link to={backUrl}>Voltar</Link></div></DashboardLayout>;

  return (
    <DashboardLayout styles={["/css/caixa-entrada.css"]}>
      <main className="dashboard">
        <div className="container inbox-page">
          <div className="inbox-header-card">
            <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
              <div>
                <p className="text-white-50 small text-uppercase fw-bold mb-1">{cityHall.name}</p>
                <h2 className="h3 fw-bold mb-0 text-white"><i className="bi bi-envelope-open-fill me-2"></i>Caixa de Entrada</h2>
                <p className="text-white-50 mb-0 small mt-1">{entrada.title}</p>
              </div>
              <Link to={backUrl} className="btn btn-light fw-bold shadow-sm"><i className="bi bi-arrow-left me-1 text-primary"></i>Voltar</Link>
            </div>
          </div>

          {message && <div className={`auth-message ${message.type==="danger"?"error":"success"} mb-3`}>{message.text}</div>}

          <article className="inbox-detail-card">
            <header className="inbox-detail-head">
              <div>
                <p className="eyebrow dark mb-1">{typeLabels[entrada.type] || entrada.type}</p>
                <h3>{entrada.title}</h3>
              </div>
              <span className="tag status">{statusLabels[entrada.status] || entrada.status}</span>
            </header>
            <dl className="inbox-detail-meta">
              <div><dt>Remetente</dt><dd>{entrada.senderName || "Sistema"}</dd></div>
              <div><dt>Destinatário</dt><dd>{entrada.destinationEmployeeName || entrada.destinationSectorName || "Prefeitura"}</dd></div>
              <div><dt>Data e hora</dt><dd>{formatDate(entrada.createdAt)}</dd></div>
              <div><dt>Prioridade</dt><dd>{priorityLabels[entrada.priority] || entrada.priority}</dd></div>
            </dl>
            <section className="inbox-detail-content">{entrada.description || "Sem conteúdo."}</section>
            <div className="inbox-detail-actions">
              {isPersonalTask ? (
                entrada.url && <Link to={entrada.url} className="btn btn-primary"><i className="bi bi-kanban-fill me-1"></i>Abrir em Tarefas</Link>
              ) : (
                <>
                  <button type="button" className="btn btn-outline-primary" onClick={()=> setMessage({type:"success",text:"Responder em breve — encaminhe via chat interno."})}><i className="bi bi-reply-fill me-1"></i>Responder</button>
                  <button type="button" className="btn btn-outline-primary" onClick={()=> setMessage({type:"success",text:"Encaminhar em breve."})}><i className="bi bi-forward-fill me-1"></i>Encaminhar</button>
                  {entrada.url && <Link to={entrada.url} className="btn btn-primary"><i className="bi bi-box-arrow-up-right me-1"></i>Abrir origem</Link>}
                </>
              )}
            </div>
          </article>

          {!isPersonalTask && (
            <section className="inbox-detail-card inbox-detail-card-compact mt-3">
              <div className="inbox-panel-head px-0 pt-0">
                <div><p className="eyebrow dark mb-0">Ações</p><h4>Tratamento da mensagem</h4></div>
              </div>
              <div className="inbox-actions">
                {isTaskRequest ? (
                  <div className="inbox-feedback-form">
                    <textarea rows="3" placeholder="Feedback para o setor solicitante" value={feedback} onChange={(e)=> setFeedback(e.target.value)} />
                    <div className="inbox-feedback-actions">
                      <button className="btn btn-primary btn-sm" onClick={()=> doAction("aceitar")}><i className="bi bi-person-check"></i> Aceitar e assumir</button>
                      <button className="btn btn-outline-danger btn-sm" onClick={()=> doAction("recusar")}><i className="bi bi-x-circle"></i> Recusar</button>
                    </div>
                  </div>
                ) : entrada.status !== "COMPLETED" ? (
                  <>
                    {!entrada.assignedToId ? (
                      <button className="btn btn-primary btn-sm" onClick={()=> doAction("assumir")}><i className="bi bi-person-check"></i> Assumir</button>
                    ) : (
                      <>
                        <button className="btn btn-primary btn-sm" onClick={()=> doAction("concluir")}><i className="bi bi-check2"></i> Concluir</button>
                        <button className="btn btn-outline-secondary btn-sm" onClick={()=> doAction("liberar")}>Liberar</button>
                      </>
                    )}
                  </>
                ) : canViewAll ? (
                  <button className="btn btn-primary btn-sm" onClick={()=> doAction("reabrir")}>Reabrir</button>
                ) : null}
              </div>
            </section>
          )}
        </div>
      </main>
    </DashboardLayout>
  );
}

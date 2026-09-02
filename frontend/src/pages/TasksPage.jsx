import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api, getSelectedCityHall, getStoredUser } from "../services/api.js";
import { TaskDetailPanel } from "../components/TaskDetailPanel.jsx";

const emptyForm = { title: "", description: "", boardId: "", responsibleIds: [], startDate: "", endDate: "", status: "TODO", priority: "NORMAL", businessPoints: 0, protocol: "", expectedResult: "" };
const emptyDemand = { destinationSectorId: "", title: "", description: "", priority: "NORMAL", deadline: "" };

function pageItems(payload) { if (Array.isArray(payload)) return payload; return payload?.content || payload?.items || []; }
function employeeName(e) { const full = [e?.firstName, e?.lastName].filter(Boolean).join(" "); return e?.fullName || e?.name || e?.nome || full || e?.email || "Servidor"; }
function sectorName(s) { return s?.name || s?.nome || "Setor sem nome"; }
function hydrateTask(dto, employees, boards, sectors){
  // backend TaskResponseDTO has boardId/sectorId/sectorName + responsibleIds/responsibleId flat
  const board = dto.board || (dto.boardId ? (boards.find(b=> String(b.id)===String(dto.boardId)) || {id: dto.boardId, sector: dto.sectorId? {id: dto.sectorId, name: dto.sectorName}: null}) : null);
  // ensure board has sector
  if(board && !board.sector && dto.sectorId) board.sector={id: dto.sectorId, name: dto.sectorName};
  // hydrate responsibles from responsibleIds
  const ids = new Set([...(dto.responsibleIds||[]).map(String), dto.responsibleId? String(dto.responsibleId): null].filter(Boolean));
  const responsibles = [...ids].map(id=> employees.find(e=> String(e.id)===String(id)) || {id}).filter(r=> r.id);
  const responsible = dto.responsibleId ? (employees.find(e=> String(e.id)===String(dto.responsibleId)) || {id: dto.responsibleId}) : (responsibles[0]||null);
  return {...dto, board, responsibles, responsible, sectorId: dto.sectorId, sectorName: dto.sectorName};
}
function hydrateTasks(list, employees, boards, sectors){ return list.map(dto=> hydrateTask(dto, employees, boards, sectors)); }

function mapStatusDjango(javaStatus){
  const m={TODO:"a_fazer",IN_PROGRESS:"em_andamento",IN_REVIEW:"em_revisao",COMPLETED:"concluida"};
  return m[javaStatus]||"a_fazer";
}
function prazoSituacao(task){
  if(task.status==="COMPLETED") return "concluida";
  if(!task.endDate) return "sem_prazo";
  const hoje=new Date(); hoje.setHours(0,0,0,0);
  const prazo=new Date(task.endDate); prazo.setHours(0,0,0,0);
  if(prazo < hoje) return "atrasada";
  if(prazo.getTime()===hoje.getTime()) return "hoje";
  const diff=(prazo-hoje)/86400000;
  if(diff<=3) return "proxima";
  return "futura";
}
function toDatetimeLocal(v){ if(!v) return ""; const d=new Date(v); if(isNaN(d)) return ""; const off=new Date(d.getTime()-d.getTimezoneOffset()*60000); return off.toISOString().slice(0,16); }
function toDateInput(v){ if(!v) return ""; return v.slice(0,10); }

export default function TasksPage(){
  const user=getStoredUser();
  const cityHall=getSelectedCityHall()||{name:user?.prefeitura||"Prefeitura"};
  const isAdmin=["admin_cidade","admin_equipe"].includes(user?.tipoUsuario||user?.role);
  const [tasks,setTasks]=useState([]);
  const [boards,setBoards]=useState([]);
  const [sectors,setSectors]=useState([]);
  const [employees,setEmployees]=useState([]);
  const [requests,setRequests]=useState([]);
  const [loading,setLoading]=useState(true);
  const [message,setMessage]=useState(null);
  const [setorAtivo,setSetorAtivo]=useState("");
  const [filtros,setFiltros]=useState({responsavel:"", prioridade:"", origem:""});
  const [query, setQuery]=useState("");
  const [detailTask,setDetailTask]=useState(null);
  // modals
  const [showNova,setShowNova]=useState(new URLSearchParams(window.location.search).get("nova")==="1");
  const [showDemanda,setShowDemanda]=useState(false);
  const [showDelegar,setShowDelegar]=useState(null);
  const [delegarIds,setDelegarIds]=useState([]);
  const [currentEmployee,setCurrentEmployee]=useState(null);
  const [showExcluir,setShowExcluir]=useState(null);
  const [showEditar,setShowEditar]=useState(null);
  const [form,setForm]=useState(emptyForm);
  const [demandForm,setDemandForm]=useState(emptyDemand);
  const [editForm,setEditForm]=useState(emptyForm);
  const [saving,setSaving]=useState(false);

  useEffect(()=>{ let m=true; (async()=>{
    setLoading(true);
    const [t,b,s,e,r,d]=await Promise.allSettled([api.getTasks(), api.getBoards(), api.getSectors(), api.getEmployees(), api.getTaskRequests(), api.getEmployeeDetails().catch(()=>null)]);
    if(!m) return;
    const nb=pageItems(b.status==="fulfilled"?b.value:[]), ns=pageItems(s.status==="fulfilled"?s.value:[]), ne=pageItems(e.status==="fulfilled"?e.value:[]), nr=pageItems(r.status==="fulfilled"?r.value:[]);
    const rawTasks=pageItems(t.status==="fulfilled"?t.value:[]);
    const hydrated=hydrateTasks(rawTasks, ne, nb, ns);
    setTasks(hydrated); setBoards(nb); setSectors(ns); setEmployees(ne); setRequests(nr);
    const det=d.status==="fulfilled"?d.value:null;
    const me = det && (det.id? det : det.employee || det.user) || ne.find(emp=> String(emp.email).toLowerCase()===String(user?.email||"").toLowerCase()) || ne.find(emp=> String(emp.id)===String(user?.id)) || null;
    if(me) setCurrentEmployee(me);
    if(ns.length && !setorAtivo) setSetorAtivo(ns[0].id);
    setLoading(false);
    if(t.status==="rejected"||b.status==="rejected") setMessage({type:"warning", text:"Alguns dados não carregaram do backend."});
  })(); return()=>{m=false;}; },[]);

  const membrosSetor=useMemo(()=>{
    const sid=setorAtivo;
    if(!sid) return employees;
    const sname=sectors.find(s=> String(s.id)===String(sid))?.name || sectors.find(s=> String(s.id)===String(sid))?.nome;
    return employees.filter(emp=>{
      const empSid=emp.sectorId?.id || emp.sector?.id || emp.sectorId || emp.sector_id;
      if(empSid) return String(empSid)===String(sid);
      // EmployeeResponseDTO só tem sectorName string, não sectorId UUID
      if(emp.sectorName) return sname && emp.sectorName===sname;
      if(emp.sector) return sname && String(emp.sector).toLowerCase()===String(sname).toLowerCase();
      return false;
    });
  },[employees, setorAtivo, sectors]);

  const delegarCandidatos=useMemo(()=>{
    if(!showDelegar) return membrosSetor;
    const board=showDelegar.board;
    const bid=board?.id || showDelegar.boardId;
    const foundBoard=boards.find(b=> String(b.id)===String(bid));
    const sid=foundBoard?.sector?.id || foundBoard?.sectorId?.id || board?.sector?.id || board?.sectorId?.id || showDelegar.sectorId || setorAtivo;
    const sname=sectors.find(s=> String(s.id)===String(sid))?.name || showDelegar.sectorName || sectors.find(s=> String(s.id)===String(sid))?.nome;
    const filtered=employees.filter(emp=>{
      const empSid=emp.sectorId?.id || emp.sector?.id || emp.sectorId || emp.sector_id;
      if(empSid) return String(empSid)===String(sid);
      if(emp.sectorName) return sname && emp.sectorName===sname;
      if(emp.sector) return sname && String(emp.sector).toLowerCase()===String(sname).toLowerCase();
      return false;
    });
    return filtered.length? filtered : (membrosSetor.length? membrosSetor : employees);
  },[showDelegar, employees, boards, membrosSetor, setorAtivo, sectors]);

  const tarefasFiltradas=useMemo(()=>{
    let list=[...tasks];
    // setor_ativo filter: task board sector == setorAtivo (or board match)
    if(setorAtivo){
      list=list.filter(t=>{
        const sid=t.board?.sector?.id||t.board?.sectorId?.id||t.boardId;
        // fallback: if no board sector, check boards map
        const board=boards.find(bb=> String(bb.id)===String(t.board?.id||t.boardId));
        const bSid=board?.sector?.id||board?.sectorId?.id||board?.id;
        return String(sid)===String(setorAtivo)|| String(bSid)===String(setorAtivo);
      });
    }
    if(query.trim()){
      const q=query.trim().toLowerCase();
      list=list.filter(t=> [t.title,t.description, employeeName(t.responsible), t.protocol].join(" ").toLowerCase().includes(q));
    }
    if(filtros.responsavel){
      if(filtros.responsavel==="minhas"){
        const uid=user?.id;
        list=list.filter(t=> (t.responsibles||[]).some(r=> String(r.id)===String(uid)) || String(t.responsible?.id)===String(uid));
      } else {
        list=list.filter(t=> (t.responsibles||[]).some(r=> String(r.id)===String(filtros.responsavel)) || String(t.responsible?.id)===String(filtros.responsavel));
      }
    }
    if(filtros.prioridade){
      const map={baixa:"LOW", normal:"NORMAL", alta:"HIGH", urgente:"URGENT"};
      const want=map[filtros.prioridade]||filtros.prioridade;
      list=list.filter(t=> t.priority===want);
    }
    if(filtros.origem){
      // externas = tarefas que vieram de solicitacao (generatedTask)
      const generatedIds=new Set(requests.filter(r=>r.generatedTaskId).map(r=> String(r.generatedTaskId)));
      if(filtros.origem==="externas") list=list.filter(t=> generatedIds.has(String(t.id)));
      if(filtros.origem==="internas") list=list.filter(t=> !generatedIds.has(String(t.id)));
    }
    return list;
  },[tasks, setorAtivo, boards, filtros, query, requests, user]);

  const total=tarefasFiltradas.length;
  const semResp=tarefasFiltradas.filter(t=> !(t.responsibles||[]).length && !t.responsible).length;
  const emAndamento=tarefasFiltradas.filter(t=> t.status==="IN_PROGRESS").length;
  const concluidas=tarefasFiltradas.filter(t=> t.status==="COMPLETED").length;

  const kanbanColumns=[
    {status:"TODO", label:"A fazer", django:"a_fazer"},
    {status:"IN_PROGRESS", label:"Em andamento", django:"em_andamento"},
    {status:"IN_REVIEW", label:"Em revisão", django:"em_revisao"},
    {status:"COMPLETED", label:"Concluída", django:"concluida"},
  ].map(col=>({ ...col, tarefas: tarefasFiltradas.filter(t=> t.status===col.status)}));

  const solicitacoesEnviadas=requests.filter(r=> String(r.originSectorId)===String(setorAtivo)).slice(0,6);
  const solicitacoesRecebidas=requests.filter(r=> String(r.destinationSectorId)===String(setorAtivo)).slice(0,6);

  async function submitNova(e){
    e.preventDefault(); setSaving(true);
    try{
      const boardId=form.boardId||boards.find(b=> String(b.sector?.id)===String(setorAtivo))?.id||boards[0]?.id;
      if(!boardId) throw new Error("Selecione um setor/quadro");
      const responsibleId=form.responsibleIds[0]||employees[0]?.id;
      if(!responsibleId) throw new Error("Selecione um responsável");
      const payload={ title:form.title, description:form.description, responsible:{id:responsibleId}, board:{id:boardId}, startDate: form.startDate? new Date(form.startDate).toISOString(): null, endDate: form.endDate? new Date(form.endDate).toISOString(): null, status:form.status, priority:form.priority, businessPoints: Number(form.businessPoints)||0, protocol: form.protocol, expectedResult: form.expectedResult, responsibleIds: form.responsibleIds };
      const createdRaw=await api.createTask(payload);
      const created=hydrateTask(createdRaw, employees, boards, sectors);
      setTasks(cur=>[created, ...cur]); setMessage({type:"success", text:`Tarefa ${created.title} criada.`}); setShowNova(false); setForm(emptyForm);
    }catch(err){ setMessage({type:"error", text: err.message}); } finally{ setSaving(false); }
  }
  async function submitDemanda(e){
    e.preventDefault(); setSaving(true);
    try{
      const payload={ destinationSectorId: demandForm.destinationSectorId, title: demandForm.title, description: demandForm.description, priority: demandForm.priority, deadline: demandForm.deadline||null };
      const created=await api.createTaskRequest(payload);
      setRequests(cur=>[created, ...cur]); setMessage({type:"success", text:`Demanda enviada para ${created.destinationSectorName||"setor"}. Verifique a Caixa de Entrada do setor destino.`}); setShowDemanda(false); setDemandForm(emptyDemand);
    }catch(err){ setMessage({type:"error", text: err.message}); } finally{ setSaving(false); }
  }
  async function moverStatus(task, novo){
    try{ const raw=await api.updateTask(task.id, {status:novo}); const updated=hydrateTask(raw, employees, boards, sectors); setTasks(cur=> cur.map(t=> String(t.id)===String(task.id)? updated: t)); setMessage({type:"success", text:"Tarefa movida."}); }catch(err){ setMessage({type:"error", text: err.message}); }
  }
  async function assumir(task){
    try{ const me=currentEmployee || employees.find(emp=> String(emp.email).toLowerCase()===String(user?.email||"").toLowerCase()); const uid=me?.id || user?.id; if(!uid || String(uid)==="1") throw new Error("Não foi possível identificar seu funcionário (UUID). Recarregue a página após login)."); const raw=await api.updateTask(task.id, {responsibleIds: [...new Set([...(task.responsibles||[]).map(r=> String(r.id)), String(uid)].filter(Boolean))]}); const updated=hydrateTask(raw, employees, boards, sectors); setTasks(cur=> cur.map(t=> String(t.id)===String(task.id)? updated: t)); setMessage({type:"success", text:"Tarefa assumida."}); }catch(err){ setMessage({type:"error", text: err.message}); }
  }
  async function liberar(task){
    try{
      const me=currentEmployee || employees.find(emp=> String(emp.email).toLowerCase()===String(user?.email||"").toLowerCase());
      const uid=me?.id || user?.id;
      if(!uid || String(uid)==="1") throw new Error("Não foi possível identificar seu funcionário (UUID). Recarregue após login).");
      const rest=(task.responsibles||[]).filter(r=> String(r.id)!==String(uid)).map(r=> String(r.id));
      // ponytail: Django exige >=1 responsável — se você é o único, delegue antes (validado no backend)
      if(rest.length===0) { setMessage({type:"error", text:"Delegue para outra pessoa antes de liberar. Você é o único responsável."}); return; }
      const raw=await api.updateTask(task.id, {responsibleIds: rest}); const updated=hydrateTask(raw, employees, boards, sectors); setTasks(cur=> cur.map(t=> String(t.id)===String(task.id)? updated: t)); setMessage({type:"success", text:"Você saiu da tarefa."});
    }catch(err){ setMessage({type:"error", text: err.message}); }
  }
  async function delegar(task, ids){
    try{ const raw=await api.updateTask(task.id, {responsibleIds: ids}); const updated=hydrateTask(raw, employees, boards, sectors); setTasks(cur=> cur.map(t=> String(t.id)===String(task.id)? updated: t)); setShowDelegar(null); setMessage({type:"success", text:"Responsáveis atualizados."}); }catch(err){ setMessage({type:"error", text: err.message}); }
  }
  async function excluir(task){
    try{ await api.deleteTask(task.id); setTasks(cur=> cur.filter(t=> String(t.id)!==String(task.id))); setShowExcluir(null); setMessage({type:"success", text:`Tarefa ${task.title} excluída.`}); }catch(err){ setMessage({type:"error", text: err.message}); }
  }
  async function salvarEdicao(e){
    e.preventDefault();
    try{ const raw=await api.updateTask(showEditar.id, {title:editForm.title, description:editForm.description, expectedResult: editForm.expectedResult, status: editForm.status, priority: editForm.priority, businessPoints: Number(editForm.businessPoints)||0, protocol: editForm.protocol, startDate: editForm.startDate? new Date(editForm.startDate).toISOString(): null, endDate: editForm.endDate? new Date(editForm.endDate).toISOString(): null, responsibleIds: editForm.responsibleIds}); const updated=hydrateTask(raw, employees, boards, sectors); setTasks(cur=> cur.map(t=> String(t.id)===String(showEditar.id)? updated: t)); setShowEditar(null); setMessage({type:"success", text:"Tarefa atualizada."}); }catch(err){ setMessage({type:"error", text: err.message}); }
  }
  function openEditar(task){
    setEditForm({ title:task.title||"", description:task.description||"", expectedResult: task.expectedResult||"", status:task.status||"TODO", priority:task.priority||"NORMAL", businessPoints: task.businessPoints||0, protocol: task.protocol||"", startDate: toDatetimeLocal(task.startDate), endDate: toDatetimeLocal(task.endDate), responsibleIds:(task.responsibles||[]).map(r=> String(r.id)), boardId: task.board?.id||"" });
    setShowEditar(task);
  }

  const setorQuadro=sectors.find(s=> String(s.id)===String(setorAtivo))|| {nome: sectorName(boards.find(b=> String(b.id)===String(setorAtivo))?.sector)||"Setor"};

  return (
    <DashboardLayout styles={["/css/tarefas.css"]}>
      <main className="dashboard task-dashboard">
        <div className="container task-page">
        <div className="task-header">
          <div><p className="eyebrow dark mb-0">{cityHall.name}</p><h3>Tarefas do setor</h3><span>{setorQuadro.nome||setorQuadro.name||"Setor"}</span></div>
          <div className="task-actions">
            {isAdmin && (
              <label className="task-sector-select"><span>Quadro</span>
                <select value={setorAtivo} onChange={e=> setSetorAtivo(e.target.value)}>{sectors.map(s=> <option key={s.id} value={s.id}>{sectorName(s)}</option>)}</select>
              </label>
            )}
            <button className="btn btn-primary" onClick={()=> setShowDemanda(true)}><i className="bi bi-send-plus"></i> Solicitar demanda</button>
            <button className="btn btn-primary" onClick={()=> { setForm({...emptyForm, responsibleIds: employees.slice(0,1).map(e=> String(e.id)), boardId: boards.find(b=> String(b.sector?.id)===String(setorAtivo))?.id||""}); setShowNova(true); }}><i className="bi bi-plus-circle"></i> Nova tarefa</button>
          </div>
        </div>

        {message && <div className={`auth-message ${message.type} tarefas-message`}><i className={`bi ${message.type==="error"?"bi-exclamation-circle-fill":"bi-check-circle-fill"}`}></i> {message.text}</div>}

        <section className="task-summary"><div><span>Total</span><strong>{total}</strong></div><div><span>Sem responsável</span><strong>{semResp}</strong></div><div><span>Em andamento</span><strong>{emAndamento}</strong></div><div><span>Concluídas</span><strong>{concluidas}</strong></div></section>

        <div className="task-filters">
          <div><label>Responsável</label><select value={filtros.responsavel} onChange={e=> setFiltros({...filtros, responsavel:e.target.value})}><option value="">Todos</option><option value="minhas">Minhas tarefas</option>{membrosSetor.map(m=> <option key={m.id} value={m.id}>{employeeName(m)}</option>)}</select></div>
          <div><label>Prioridade</label><select value={filtros.prioridade} onChange={e=> setFiltros({...filtros, prioridade:e.target.value})}><option value="">Todas</option><option value="baixa">Baixa</option><option value="normal">Normal</option><option value="alta">Alta</option><option value="urgente">Urgente</option></select></div>
          <div><label>Origem</label><select value={filtros.origem} onChange={e=> setFiltros({...filtros, origem:e.target.value})}><option value="">Todas</option><option value="externas">Recebidas de outros setores</option><option value="internas">Criadas no setor</option></select></div>
          <div className="task-filter-actions"><input className="field-input" placeholder="Buscar..." value={query} onChange={e=> setQuery(e.target.value)}/><button className="btn btn-outline-secondary" onClick={()=> {setFiltros({responsavel:"", prioridade:"", origem:""}); setQuery("");}}>Limpar</button></div>
        </div>

        <section className="kanban-board" aria-label="Quadro kanban do setor">
          {kanbanColumns.map(col=> (
            <div key={col.status} className={`kanban-column status-${col.django}`}>
              <header><div><span>{col.label}</span><strong>{col.tarefas.length}</strong></div></header>
              <div className="kanban-list">
                {col.tarefas.map(tarefa=>{
                  const situacao=prazoSituacao(tarefa);
                  const podeExcluir= isAdmin || ((tarefa.responsibles||[]).length===1 && String((tarefa.responsibles?.[0]?.id||tarefa.responsible?.id))===String(user.id));
                  const meId=currentEmployee?.id || employees.find(e=> String(e.email).toLowerCase()===String(user?.email||"").toLowerCase())?.id || user?.id;
                  const isResp=(tarefa.responsibles||[]).some(r=> String(r.id)===String(meId)) || String(tarefa.responsible?.id)===String(meId);
                  return (
                    <article key={tarefa.id} className="task-card">
                      <div className="task-card-top">
                        <span className={`task-priority prioridade-${String(tarefa.priority||"NORMAL").toLowerCase()}`}>{tarefa.priority}</span>
                        <div className="task-card-top-actions">
                          {tarefa.endDate && <span className="task-date"><i className="bi bi-calendar2"></i> {new Date(tarefa.endDate).toLocaleDateString("pt-BR")}</span>}
                          <button className="task-card-edit" title="Editar" onClick={()=> openEditar(tarefa)}><i className="bi bi-pencil-square"></i></button>
                          {podeExcluir && <button className="task-card-delete" title="Excluir" onClick={()=> setShowExcluir(tarefa)}><i className="bi bi-trash3"></i></button>}
                        </div>
                      </div>
                      <div className={`task-due-state due-${situacao}`}>
                        {situacao==="atrasada" && <><i className="bi bi-exclamation-triangle-fill"></i> Atrasada</>}
                        {situacao==="hoje" && <><i className="bi bi-alarm-fill"></i> Vence hoje</>}
                        {situacao==="proxima" && <><i className="bi bi-hourglass-split"></i> Próxima do vencimento</>}
                        {situacao==="futura" && <><i className="bi bi-check-circle-fill"></i> No prazo</>}
                        {situacao==="concluida" && <><i className="bi bi-check2-circle"></i> Concluída</>}
                        {situacao==="sem_prazo" && <><i className="bi bi-calendar2-minus"></i> Sem prazo definido</>}
                      </div>
                      <h4><button className="task-title-link" onClick={()=> setDetailTask(tarefa)}>{tarefa.title}</button></h4>
                      {tarefa.description && <p>{String(tarefa.description).slice(0,150)}</p>}
                      <div className="task-tags">
                        {tarefa.startDate && <span><i className="bi bi-play-circle"></i> Início {new Date(tarefa.startDate).toLocaleDateString("pt-BR", {day:"2-digit", month:"2-digit"})}</span>}
                        {tarefa.businessPoints ? <span><i className="bi bi-gem"></i> {tarefa.businessPoints} pts</span>: null}
                        {tarefa.protocol && <span><i className="bi bi-upc-scan"></i> {tarefa.protocol}</span>}
                      </div>
                      <div className="task-people">
                        {(tarefa.responsibles||[]).length? (tarefa.responsibles||[]).slice(0,5).map(r=> <span key={r.id} title={employeeName(r)}>{employeeName(r).slice(0,2).toUpperCase()}</span>) : tarefa.responsible? <span title={employeeName(tarefa.responsible)}>{employeeName(tarefa.responsible).slice(0,2).toUpperCase()}</span> : <em>Sem responsável</em>}
                      </div>
                      <div className="task-card-indicators"><span><i className="bi bi-list-check"></i> 0/0</span><span><i className="bi bi-chat-square-text"></i> 0</span><span><i className="bi bi-paperclip"></i> 0</span></div>
                      <div className="task-card-actions">
                        {/* assumir / liberar */}
                        {!isResp && tarefa.status!=="COMPLETED" && <button className="btn btn-outline-secondary btn-sm task-assume-btn" onClick={()=> assumir(tarefa)}><i className="bi bi-person-check"></i> Assumir</button>}
                        {isResp && tarefa.status!=="COMPLETED" && (
                          (tarefa.responsibles||[]).length>1
                            ? <button className="btn btn-outline-secondary btn-sm" onClick={()=> liberar(tarefa)}><i className="bi bi-person-dash"></i> Liberar</button>
                            : <button className="btn btn-outline-secondary btn-sm" disabled title="Delegue para outra pessoa antes de liberar"><i className="bi bi-person-dash"></i> Liberar</button>
                        )}
                        <button className="btn btn-outline-secondary btn-sm" onClick={()=> { setShowDelegar(tarefa); setDelegarIds((tarefa.responsibles||[]).map(r=> String(r.id))); }}><i className="bi bi-people"></i> Delegar</button>
                        <label className="task-move-form"><select value={tarefa.status} onChange={e=> moverStatus(tarefa, e.target.value)} aria-label="Mover tarefa"><option value="TODO">A fazer</option><option value="IN_PROGRESS">Em andamento</option><option value="IN_REVIEW">Em revisão</option><option value="COMPLETED">Concluída</option></select></label>
                      </div>
                    </article>
                  );
                })}
                {col.tarefas.length===0 && <div className="kanban-empty"><i className="bi bi-check2-square"></i> Nenhuma tarefa nesta etapa.</div>}
              </div>
            </div>
          ))}
        </section>

        <section className="task-requests">
          <div className="task-request-panel"><div className="task-panel-head"><p className="eyebrow dark mb-0">Enviadas</p><h4>Demandas solicitadas</h4></div>{solicitacoesEnviadas.map(s=> <div key={s.id} className={`request-row status-${s.status?.toLowerCase()}`}><strong>{s.title}</strong><span>Para {s.destinationSectorName||s.destinationSectorId} · {s.status}</span>{s.feedback && <p>{String(s.feedback).slice(0,120)}</p>}</div>)}{solicitacoesEnviadas.length===0 && <div className="request-empty">Nenhuma demanda enviada por este setor.</div>}</div>
          <div className="task-request-panel"><div className="task-panel-head"><p className="eyebrow dark mb-0">Recebidas</p><h4>Histórico de demandas</h4></div>{solicitacoesRecebidas.map(s=> <div key={s.id} className={`request-row status-${s.status?.toLowerCase()}`}><strong>{s.title}</strong><span>De {s.originSectorName||s.originSectorId} · {s.status}</span>{s.feedback && <p>{String(s.feedback).slice(0,120)}</p>}</div>)}{solicitacoesRecebidas.length===0 && <div className="request-empty">Nenhuma demanda recebida por este setor.</div>}</div>
        </section>
      </div>
      </main>

      {/* Nova tarefa - 1:1 tarefas.html:418 */}
      {showNova && <div className="react-modal-backdrop" onMouseDown={()=> setShowNova(false)}><div className="modal-dialog modal-dialog-centered task-modal-dialog task-modal-dialog-lg" onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><form onSubmit={submitNova}><div className="modal-header"><div><h5 className="modal-title">Nova tarefa</h5><small>{setorQuadro.nome}</small></div><button type="button" className="btn-close" onClick={()=> setShowNova(false)} aria-label="Fechar"></button></div><div className="modal-body"><div className="row g-3"><div className="col-12"><label className="form-label">Título</label><input className="form-control" required value={form.title} onChange={e=> setForm({...form, title:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Prioridade</label><select className="form-select" value={form.priority} onChange={e=> setForm({...form, priority:e.target.value})}><option value="LOW">Baixa</option><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option></select></div><div className="col-md-6"><label className="form-label">Pontos de valor público</label><input className="form-control" type="number" min="0" max="100" value={form.businessPoints} onChange={e=> setForm({...form, businessPoints:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Data de início</label><input className="form-control" type="datetime-local" value={form.startDate} onChange={e=> setForm({...form, startDate:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Prazo de entrega</label><input className="form-control" type="datetime-local" value={form.endDate} onChange={e=> setForm({...form, endDate:e.target.value})}/></div><div className="col-12"><label className="form-label">Protocolo ou processo relacionado</label><input className="form-control" value={form.protocol} onChange={e=> setForm({...form, protocol:e.target.value})}/></div><div className="col-12"><label className="form-label">Descrição</label><textarea className="form-control" rows="3" value={form.description} onChange={e=> setForm({...form, description:e.target.value})}></textarea></div><div className="col-12"><label className="form-label">Resultado esperado</label><textarea className="form-control" rows="2" value={form.expectedResult} onChange={e=> setForm({...form, expectedResult:e.target.value})}></textarea></div><div className="col-12"><label className="form-label">Responsáveis</label><select multiple size="5" className="form-select" value={form.responsibleIds} onChange={e=> setForm({...form, responsibleIds: [...e.target.selectedOptions].map(o=>o.value)})}>{employees.map(emp=> <option key={emp.id} value={String(emp.id)}>{employeeName(emp)}</option>)}</select><small className="text-muted">Segure Ctrl para selecionar múltiplos.</small></div></div></div><div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> setShowNova(false)}>Cancelar</button><button type="submit" className="btn btn-primary" disabled={saving}>{saving? "Criando...":"Criar tarefa"}</button></div></form></div></div></div>}

      {/* Editar tarefa - 1:1 tarefas.html:381 */}
      {showEditar && <div className="react-modal-backdrop" onMouseDown={()=> setShowEditar(null)}><div className="modal-dialog modal-dialog-centered modal-lg" style={{maxWidth:760, width:"95%", margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><form onSubmit={salvarEdicao}><div className="modal-header"><div><h5 className="modal-title">Editar tarefa</h5><small>{showEditar.title}</small></div><button type="button" className="btn-close" onClick={()=> setShowEditar(null)} aria-label="Fechar"></button></div><div className="modal-body"><div className="row g-3"><div className="col-12"><label className="form-label">Título</label><input className="form-control" required value={editForm.title} onChange={e=> setEditForm({...editForm, title:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Estado</label><select className="form-select" value={editForm.status} onChange={e=> setEditForm({...editForm, status:e.target.value})}><option value="TODO">A fazer</option><option value="IN_PROGRESS">Em andamento</option><option value="IN_REVIEW">Em revisão</option><option value="COMPLETED">Concluída</option></select></div><div className="col-md-6"><label className="form-label">Prioridade</label><select className="form-select" value={editForm.priority} onChange={e=> setEditForm({...editForm, priority:e.target.value})}><option value="LOW">Baixa</option><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option></select></div><div className="col-md-6"><label className="form-label">Data de início</label><input className="form-control" type="datetime-local" value={editForm.startDate} onChange={e=> setEditForm({...editForm, startDate:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Prazo de entrega</label><input className="form-control" type="datetime-local" value={editForm.endDate} onChange={e=> setEditForm({...editForm, endDate:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Pontos de valor público</label><input className="form-control" type="number" value={editForm.businessPoints} onChange={e=> setEditForm({...editForm, businessPoints:e.target.value})}/></div><div className="col-md-6"><label className="form-label">Protocolo/processo</label><input className="form-control" value={editForm.protocol} onChange={e=> setEditForm({...editForm, protocol:e.target.value})}/></div><div className="col-12"><label className="form-label">Descrição</label><textarea className="form-control" rows="3" value={editForm.description} onChange={e=> setEditForm({...editForm, description:e.target.value})}></textarea></div><div className="col-12"><label className="form-label">Resultado esperado</label><textarea className="form-control" rows="2" value={editForm.expectedResult} onChange={e=> setEditForm({...editForm, expectedResult:e.target.value})}></textarea></div><div className="col-12"><label className="form-label">Responsáveis</label><select multiple size="5" className="form-select" value={editForm.responsibleIds} onChange={e=> setEditForm({...editForm, responsibleIds: [...e.target.selectedOptions].map(o=>o.value)})}>{employees.map(emp=> <option key={emp.id} value={String(emp.id)}>{employeeName(emp)}</option>)}</select></div></div></div><div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> setShowEditar(null)}>Cancelar</button><button type="submit" className="btn btn-primary">Salvar alterações</button></div></form></div></div></div>}

      {/* Solicitar demanda - 1:1 tarefas.html:481 */}
      {showDemanda && <div className="react-modal-backdrop" onMouseDown={()=> setShowDemanda(false)}><div className="modal-dialog modal-dialog-centered modal-lg" style={{maxWidth:760, width:"95%", margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><form onSubmit={submitDemanda}><div className="modal-header"><div><h5 className="modal-title">Solicitar demanda</h5><small>Origem: {setorQuadro.nome}</small></div><button type="button" className="btn-close" onClick={()=> setShowDemanda(false)} aria-label="Fechar"></button></div><div className="modal-body"><div className="row g-3"><div className="col-md-6"><label className="form-label">Setor de destino</label><select className="form-select" required value={demandForm.destinationSectorId} onChange={e=> setDemandForm({...demandForm, destinationSectorId:e.target.value})}><option value="">Selecione</option>{sectors.filter(s=> String(s.id)!==String(setorAtivo)).map(s=> <option key={s.id} value={s.id}>{sectorName(s)}</option>)}</select></div><div className="col-md-3"><label className="form-label">Prioridade</label><select className="form-select" value={demandForm.priority} onChange={e=> setDemandForm({...demandForm, priority:e.target.value})}><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option><option value="LOW">Baixa</option></select></div><div className="col-md-3"><label className="form-label">Prazo</label><input className="form-control" type="date" min={new Date().toISOString().slice(0,10)} value={demandForm.deadline} onChange={e=> setDemandForm({...demandForm, deadline:e.target.value})}/></div><div className="col-12"><label className="form-label">Título</label><input className="form-control" required value={demandForm.title} onChange={e=> setDemandForm({...demandForm, title:e.target.value})}/></div><div className="col-12"><label className="form-label">Descrição</label><textarea className="form-control" rows="4" value={demandForm.description} onChange={e=> setDemandForm({...demandForm, description:e.target.value})}></textarea></div></div></div><div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> setShowDemanda(false)}>Cancelar</button><button type="submit" className="btn btn-primary" disabled={saving}>Enviar para caixa do setor</button></div></form></div></div></div>}

      {/* Delegar - 1:1 igual a Sistema-ERP-Municipal/templates/dashboard/tarefas.html:269 */}
      {showDelegar && <div className="react-modal-backdrop" onMouseDown={()=> setShowDelegar(null)}><div className="modal-dialog modal-dialog-centered" style={{maxWidth:560, width:"95%", margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><form onSubmit={e=>{e.preventDefault(); delegar(showDelegar, delegarIds);}}><div className="modal-header"><div><h5 className="modal-title">Delegar tarefa</h5><small>{showDelegar.title}</small></div><button type="button" className="btn-close" onClick={()=> setShowDelegar(null)} aria-label="Fechar"></button></div><div className="modal-body"><label className="form-label">Responsáveis do setor</label><select name="responsaveis" className="form-select" multiple size="7" required value={delegarIds} onChange={e=> setDelegarIds([...e.target.selectedOptions].map(o=>o.value))}>{delegarCandidatos.map(emp=> <option key={emp.id} value={String(emp.id)}>{employeeName(emp)}</option>)}</select>{delegarCandidatos.length===0 && <div className="text-warning small mt-2">Nenhum funcionário encontrado para este setor. Verifique cadastro de funcionários.</div>}<small className="text-muted">Selecione pelo menos uma pessoa. Segure Ctrl para selecionar mais de uma.</small></div><div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> setShowDelegar(null)}>Cancelar</button><button type="submit" className="btn btn-primary">Salvar responsáveis</button></div></form></div></div></div>}

      {/* Excluir */}
      {showExcluir && <div className="react-modal-backdrop" onMouseDown={()=> setShowExcluir(null)}><div className="modal-dialog modal-dialog-centered" style={{maxWidth:560, width:"95%", margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><div className="modal-header"><h5 className="modal-title">Excluir tarefa</h5><button type="button" className="btn-close" onClick={()=> setShowExcluir(null)} aria-label="Fechar"></button></div><div className="modal-body"><div className="task-delete-confirmation"><i className="bi bi-exclamation-triangle-fill"></i><div><strong>{showExcluir.title}</strong><p>A tarefa, seus comentários, anexos, checklist e apontamentos serão removidos. O registro permanecerá na auditoria.</p></div></div></div><div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> setShowExcluir(null)}>Cancelar</button><button type="button" className="btn btn-danger" onClick={()=> excluir(showExcluir)}><i className="bi bi-trash3"></i> Excluir tarefa</button></div></div></div></div>}

      {detailTask && <TaskDetailPanel task={detailTask} onClose={()=> setDetailTask(null)} onMessage={setMessage} />}
    </DashboardLayout>
  );
}

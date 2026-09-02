import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

function initials(name){ return String(name||"").slice(0,2).map(c=>c[0]).join("").toUpperCase(); }

export default function AgriculturePage(){
  const cityHall=getSelectedCityHall()||{name:getStoredUser()?.prefeitura||"Prefeitura"};
  const canManage=["admin_cidade","admin_equipe"].includes(getUserType(getStoredUser()));
  const [aba,setAba]=useState(new URLSearchParams(window.location.search).get("aba")==="operacional"?"operacional":"servicos");
  const [catalog,setCatalog]=useState({serviceTypes:[], paymentTypes:[], machinery:[], drivers:[]});
  const [services,setServices]=useState([]);
  const [clients,setClients]=useState([]);
  const [query,setQuery]=useState(new URLSearchParams(window.location.search).get("q")||"");
  const [status,setStatus]=useState(new URLSearchParams(window.location.search).get("status")||"");
  const [page,setPage]=useState(0);
  const [selectedId,setSelectedId]=useState(new URLSearchParams(window.location.search).get("servico")||"");
  const [controlQuery,setControlQuery]=useState("");
  const [selectedControlId,setSelectedControlId]=useState(new URLSearchParams(window.location.search).get("controle")||"");
  const [showNovo,setShowNovo]=useState(false);
  const [editingId,setEditingId]=useState(null);
  const [showTipo,setShowTipo]=useState(false);
  const [showComp,setShowComp]=useState(false);
  const [form,setForm]=useState({numero_protocolo:"",status:"PENDING",clienteId:"",data_agendada:"",tipo_servicoId:"",horas_solicitadas:"",endereco:"",e_doacao:false,origem_doacao:"",data_pagamento:"",id_funder:"",tipo_comprovanteId:"",valor_funder:"",comprovante:null});
  const [controlForm,setControlForm]=useState({maquinarioId:"",tratoristaId:"",horimetro_inicial:"",horimetro_final:""});
  const [message,setMessage]=useState(null);

  async function load(){
    try{
      const [cat, cli, srv]=await Promise.all([api.getAgricultureCatalog(), api.getClients(), api.getAgricultureServices({query, page})]);
      setCatalog(cat); setClients(cli?.content||[]); 
      const list=srv?.content||[];
      // filtra por status se servicos
      const filtered= status? list.filter(s=> s.status===status): list;
      setServices(filtered);
      if(!selectedId && filtered[0]) setSelectedId(filtered[0].id);
    }catch(e){ setMessage({type:"error", text:e.message}); }
  }
  useEffect(()=>{ load(); },[query, status, page, aba]);
  useEffect(()=>{
    const url=new URL(window.location.href);
    url.searchParams.set("aba", aba);
    window.history.replaceState({}, "", url);
  },[aba]);

  const totais=useMemo(()=>({total:services.length, pendentes:services.filter(s=>s.status==="PENDING").length, concluidos:services.filter(s=>s.status==="COMPLETED").length, expirados:services.filter(s=>s.status==="EXPIRED").length}),[services]);
  const selected=useMemo(()=> services.find(s=> String(s.id)===String(selectedId))||null,[services,selectedId]);
  const controles=useMemo(()=> services.map(s=> s.control? {...s.control, servico:s}: null).filter(Boolean),[services]);
  const filteredControles=useMemo(()=>{ if(!controlQuery) return controles; const q=controlQuery.toLowerCase(); return controles.filter(c=> [c.servico?.protocol, c.servico?.clientName, c.machineryName, c.tractorDriverName].join(" ").toLowerCase().includes(q)); },[controles, controlQuery]);
  const selectedControl=useMemo(()=> filteredControles.find(c=> String(c.id)===String(selectedControlId) || String(c.servico?.id)===String(selectedControlId))||null,[filteredControles, selectedControlId]);

  async function submitNovo(e){
    e.preventDefault();
    const payload={
      protocol: form.numero_protocolo||undefined, status: (form.status==="PENDENTE"?"PENDING":form.status)||"PENDING", clientId: form.clienteId, scheduledDate: form.data_agendada, serviceTypeId: form.tipo_servicoId,
      requestedHours: form.horas_solicitadas, address: form.endereco, donation: form.e_doacao, donationOrigin: form.origem_doacao,
      paymentDate: form.data_pagamento||null, funderId: form.id_funder, paymentProofTypeId: form.tipo_comprovanteId||null, funderAmount: form.valor_funder? Number(form.valor_funder):null
    };
    try{
      const created= editingId? await api.updateAgricultureService(editingId, payload): await api.createAgricultureService(payload);
      if(form.comprovante) await api.uploadAgricultureProof(created.id, form.comprovante).catch(()=>{});
      setMessage({type:"success", text: editingId? "Serviço atualizado":"Serviço cadastrado e controle operacional criado"});
      setShowNovo(false); setEditingId(null); setForm({numero_protocolo:"",status:"PENDING",clienteId:"",data_agendada:"",tipo_servicoId:"",horas_solicitadas:"",endereco:"",e_doacao:false,origem_doacao:"",data_pagamento:"",id_funder:"",tipo_comprovanteId:"",valor_funder:"",comprovante:null});
      load();
    }catch(err){ setMessage({type:"error", text:err.message}); }
  }
  async function submitControl(e){
    e.preventDefault();
    if(!selected) return;
    try{
      await api.updateAgricultureControl(selected.id, {machineryId: controlForm.maquinarioId||null, tractorDriverId: controlForm.tratoristaId||null, initialHourMeter: controlForm.horimetro_inicial? Number(controlForm.horimetro_inicial):null, finalHourMeter: controlForm.horimetro_final? Number(controlForm.horimetro_final):null});
      setMessage({type:"success", text:"Controle operacional atualizado"});
      load();
    }catch(err){ setMessage({type:"error", text:err.message}); }
  }
  function openEdit(s){
    setForm({numero_protocolo:s.protocol||"",status:s.status||"PENDENTE",clienteId:s.clientId||"",data_agendada:s.scheduledDate||"",tipo_servicoId:s.serviceTypeId||"",horas_solicitadas:s.requestedHours||"",endereco:s.address||"",e_doacao:s.donation||false,origem_doacao:s.donationOrigin||"",data_pagamento:s.paymentDate||"",id_funder:s.funderId||"",tipo_comprovanteId:s.paymentProofTypeId||"",valor_funder:s.funderAmount||"",comprovante:null});
    setEditingId(s.id); setShowNovo(true);
  }

  return (
    <DashboardLayout styles={["/css/patrulha_agricola.css"]}>
      <main className="dashboard">
        <div className="container patrol-page">
          <header className="patrol-header">
            <div><p className="eyebrow dark mb-0">{cityHall.name}</p><h3>Patrulha Agrícola</h3><p className="mb-0">Controle de serviços, agenda, pagamentos e doações.</p></div>
            <div className="patrol-actions">
              {canManage && <>
                <a className="btn btn-outline-primary btn-outline-primary2" href="/clientes"><i className="bi bi-people"></i> Clientes</a>
                <button className="btn btn-outline-primary btn-outline-primary2" onClick={()=> setShowTipo(true)}><i className="bi bi-tools"></i> Tipos de serviço</button>
                <button className="btn btn-outline-primary btn-outline-primary2" onClick={()=> setShowComp(true)}><i className="bi bi-receipt"></i> Comprovantes</button>
                <button className="btn btn-primary" onClick={()=> { setForm({numero_protocolo:"",status:"PENDING",clienteId:clients[0]?.id||"",data_agendada:new Date().toISOString().slice(0,10),tipo_servicoId:catalog.serviceTypes[0]?.id||"",horas_solicitadas:"",endereco:"",e_doacao:false,origem_doacao:"",data_pagamento:"",id_funder:"",tipo_comprovanteId:"",valor_funder:"",comprovante:null}); setEditingId(null); setShowNovo(true); }}><i className="bi bi-plus-circle"></i> Novo serviço</button>
              </>}
            </div>
          </header>

          <section className="patrol-summary"><div><span>Total</span><strong>{totais.total}</strong></div><div><span>Pendentes</span><strong>{totais.pendentes}</strong></div><div><span>Concluídos</span><strong>{totais.concluidos}</strong></div><div><span>Expirados</span><strong>{totais.expirados}</strong></div></section>

          <nav className="patrol-tabs" aria-label="Seções da patrulha agrícola">
            <a href="#" onClick={e=>{e.preventDefault(); setAba("servicos");}} className={aba==="servicos"?"active":""}><i className="bi bi-list-task"></i> Serviços</a>
            <a href="#" onClick={e=>{e.preventDefault(); setAba("operacional");}} className={aba==="operacional"?"active":""}><i className="bi bi-speedometer2"></i> Operacional</a>
          </nav>

          {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}

          {aba==="servicos" ? (
            <>
              <form className="patrol-filters" onSubmit={e=>{e.preventDefault(); setPage(0); load();}}>
                <div><label htmlFor="patrol-search">Buscar</label><input id="patrol-search" value={query} onChange={e=> setQuery(e.target.value)} placeholder="Protocolo, cliente, CPF ou serviço" /></div>
                <div><label htmlFor="patrol-status">Status</label><select id="patrol-status" value={status} onChange={e=> setStatus(e.target.value)}><option value="">Todos</option><option value="PENDING">Pendente</option><option value="COMPLETED">Concluído</option><option value="CANCELLED">Cancelado</option><option value="EXPIRED">Expirado</option></select></div>
                <div className="patrol-filter-actions"><button className="btn btn-primary" type="submit"><i className="bi bi-funnel"></i> Filtrar</button><button className="btn btn-outline-secondary" type="button" onClick={()=>{setQuery(""); setStatus(""); setPage(0);}}>Limpar</button></div>
              </form>

              <section className="patrol-layout">
                <div className="patrol-table-panel">
                  <div className="table-responsive"><table className="patrol-table">
                    <thead><tr><th>Protocolo</th><th>Cliente</th><th>Agendamento</th><th>Serviço</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                      {services.map(s=> (
                        <tr key={s.id} className={String(selected?.id)===String(s.id)?"active":""}>
                          <td>{s.protocol||"-"}</td>
                          <td><strong>{s.clientName}</strong><small>{s.clientCpf||""}</small></td>
                          <td>{s.scheduledDate? new Date(s.scheduledDate+"T12:00:00").toLocaleDateString("pt-BR"):"-"}</td>
                          <td>{s.serviceTypeName}<small>{s.serviceTypeArea||""}</small></td>
                          <td><span className={`patrol-status status-${String(s.status).toLowerCase()}`}>{s.status}</span><span className={`operational-status status-${String(s.control?.hoursStatus||"aguardando_operacional").toLowerCase()}`}>{s.control?.hoursStatus||"Aguardando operacional"}</span></td>
                          <td><button className="patrol-open-link" onClick={()=> setSelectedId(s.id)} aria-label="Abrir serviço"><i className="bi bi-chevron-right"></i></button></td>
                        </tr>
                      ))}
                      {services.length===0 && <tr><td colSpan={6} className="patrol-empty">Nenhum serviço encontrado.</td></tr>}
                    </tbody>
                  </table></div>
                  <nav className="patrol-pagination"><button className="btn btn-outline-secondary btn-sm" disabled={page===0} onClick={()=> setPage(p=> Math.max(0,p-1))}>Anterior</button><span>Página {page+1}</span><button className="btn btn-outline-secondary btn-sm" onClick={()=> setPage(p=>p+1)}>Próxima</button></nav>
                </div>
                <aside className="patrol-detail-panel">
                  {selected ? (
                    <>
                      <div className="patrol-detail-heading"><div><p className="eyebrow dark mb-1">Serviço selecionado</p><h4>{selected.protocol||"Sem protocolo"}</h4></div><span className={`patrol-status status-${String(selected.status).toLowerCase()}`}>{selected.status}</span></div>
                      <div className={`service-operational-alert status-${String(selected.control?.hoursStatus||"aguardando_operacional").toLowerCase()}`}><i className="bi bi-speedometer2"></i>{selected.control?.hoursStatus||"Aguardando operacional"} · saldo de {selected.control?.remainingHours??selected.requestedHours} h</div>
                      <dl className="patrol-detail-grid">
                        <div><dt>Cliente</dt><dd>{selected.clientName}</dd></div>
                        <div><dt>Tipo de serviço</dt><dd>{selected.serviceTypeName} · {selected.serviceTypeArea||""}</dd></div>
                        <div><dt>Data agendada</dt><dd>{selected.scheduledDate? new Date(selected.scheduledDate+"T12:00:00").toLocaleDateString("pt-BR"):"-"}</dd></div>
                        <div><dt>Horas solicitadas</dt><dd>{selected.requestedHours} h</dd></div>
                        <div><dt>Endereço</dt><dd>{selected.address}</dd></div>
                        <div><dt>Valor</dt><dd>{selected.donation? "Doação": (selected.amount!=null? `R$ ${Number(selected.amount).toFixed(2)}`:"-")}</dd></div>
                        <div><dt>Pagamento</dt><dd>{selected.paymentDate? new Date(selected.paymentDate).toLocaleDateString("pt-BR"):"Não informado"}</dd></div>
                        <div><dt>Expira em</dt><dd>{selected.expirationDate? new Date(selected.expirationDate).toLocaleDateString("pt-BR"):"Sem pagamento"}</dd></div>
                        <div><dt>ID do FUNDER</dt><dd>{selected.funderId||"Não informado"}</dd></div>
                        <div><dt>Valor do FUNDER</dt><dd>{selected.funderAmount!=null? `R$ ${Number(selected.funderAmount).toFixed(2)}`:"Não informado"}</dd></div>
                        {selected.donation && <div className="patrol-detail-wide"><dt>Origem da doação</dt><dd>{selected.donationOrigin}</dd></div>}
                      </dl>
                      {selected.paymentProof && <a className="btn btn-outline-primary patrol-receipt" href="#" onClick={e=>{e.preventDefault(); api.uploadAgricultureProof(selected.id, new Blob([]));}}><i className="bi bi-download"></i> Baixar comprovante</a>}
                      {canManage && <button className="btn btn-primary w-100" onClick={()=> openEdit(selected)}><i className="bi bi-pencil-square"></i> Editar serviço</button>}
                    </>
                  ) : <div className="patrol-detail-empty"><i className="bi bi-tractor"></i><h4>Selecione um serviço</h4><p>Consulte os dados completos de um agendamento.</p></div>}
                </aside>
              </section>
            </>
          ) : (
            <section className="operational-layout">
              <div className="operational-list-panel">
                <div className="operational-toolbar">
                  <form className="operational-search" onSubmit={e=>{e.preventDefault();}}>
                    <i className="bi bi-search"></i><input value={controlQuery} onChange={e=> setControlQuery(e.target.value)} placeholder="Protocolo, cliente, maquinário ou tratorista" />
                    <button className="btn btn-primary" type="submit">Buscar</button>
                  </form>
                  {canManage && <div className="operational-toolbar-actions"><button className="btn btn-outline-primary" onClick={()=>{ const n=prompt("Nome do maquinário"); if(n) api.addAgricultureCatalog("MACHINERY",{name:n}).then(load); }}><i className="bi bi-truck-front"></i> Maquinários</button><button className="btn btn-outline-primary" onClick={()=>{ const n=prompt("Nome do tratorista"); if(n) api.addAgricultureCatalog("DRIVER",{name:n}).then(load); }}><i className="bi bi-person-gear"></i> Tratoristas</button></div>}
                </div>
                <div className="operational-list-panel">
                  <header className="operational-panel-header"><div><p className="eyebrow dark mb-1">Controle operacional</p><h4>Serviços em execução</h4></div><span>{filteredControles.length} registro(s)</span></header>
                  <div className="table-responsive"><table className="operational-table">
                    <thead><tr><th>Serviço</th><th>Maquinário</th><th>Tratorista</th><th>Horas realizadas</th><th>Situação</th><th></th></tr></thead>
                    <tbody>
                      {filteredControles.map(c=> (
                        <tr key={c.id} className={String(selectedControl?.id)===String(c.id)?"active":""}>
                          <td><strong>{c.servico?.protocol||c.serviceTypeName||"-"}</strong><small>{c.servico?.clientName||""}</small></td>
                          <td>{c.machineryName||"Não informado"}</td>
                          <td>{c.tractorDriverName||"Não informado"}</td>
                          <td>{c.performedHours!=null? `${c.performedHours} h`:"Não concluído"}</td>
                          <td><span className={`operational-status status-${String(c.hoursStatus||"aguardando_operacional").toLowerCase()}`}>{c.hoursStatus||"Aguardando operacional"}</span></td>
                          <td><button className="patrol-open-link" onClick={()=> setSelectedControlId(c.id)}><i className="bi bi-chevron-right"></i></button></td>
                        </tr>
                      ))}
                      {filteredControles.length===0 && <tr><td colSpan={6} className="patrol-empty">Nenhum controle operacional encontrado.</td></tr>}
                    </tbody>
                  </table></div>
                </div>
              </div>
              <aside className="operational-detail-panel">
                {selectedControl ? (
                  <>
                    <div className="operational-detail-heading"><div><p className="eyebrow dark mb-1">Controle do serviço</p><h4>{selectedControl.servico?.protocol||"Sem protocolo"}</h4><p>{selectedControl.servico?.clientName||""}</p></div><span className={`operational-status status-${String(selectedControl.hoursStatus||"aguardando_operacional").toLowerCase()}`}>{selectedControl.hoursStatus||"Aguardando operacional"}</span></div>
                    <dl className="operational-metrics">
                      <div><dt>Horas do serviço</dt><dd>{selectedControl.servico?.requestedHours||selectedControl.requestedHours} h</dd></div>
                      <div><dt>Horas realizadas</dt><dd>{selectedControl.performedHours!=null? `${selectedControl.performedHours} h`:"Aguardando horímetro final"}</dd></div>
                      <div><dt>Saldo</dt><dd className={`saldo-${String(selectedControl.hoursStatus||"aguardando_operacional").toLowerCase()}`}>{selectedControl.remainingHours} h</dd></div>
                      <div><dt>Maquinário</dt><dd>{selectedControl.machineryName||"Não informado"}</dd></div>
                      <div><dt>Tratorista</dt><dd>{selectedControl.tractorDriverName||"Não informado"}</dd></div>
                    </dl>
                    {canManage && <button className="btn btn-primary w-100" onClick={()=>{ if(selectedControl.servico) { setSelectedId(selectedControl.servico.id); setControlForm({maquinarioId:selectedControl.machineryId||"",tratoristaId:selectedControl.tractorDriverId||"",horimetro_inicial:selectedControl.initialHourMeter||"",horimetro_final:selectedControl.finalHourMeter||""}); } }}><i className="bi bi-pencil-square"></i> Preencher controle operacional</button>}
                    <form className="row g-2 mt-3" onSubmit={async e=>{e.preventDefault(); await api.updateAgricultureControl(selectedControl.servico?.id||selectedControl.id, {machineryId: controlForm.maquinarioId||null, tractorDriverId: controlForm.tratoristaId||null, initialHourMeter: controlForm.horimetro_inicial? Number(controlForm.horimetro_inicial):null, finalHourMeter: controlForm.horimetro_final? Number(controlForm.horimetro_final):null}); load();}}>
                      <div className="col-12"><label className="field-label">Maquinário</label><select className="field-input" value={controlForm.maquinarioId} onChange={e=> setControlForm({...controlForm,maquinarioId:e.target.value})}><option value="">Selecione</option>{catalog.machinery.map(m=><option key={m.id} value={m.id}>{m.name}</option>)}</select></div>
                      <div className="col-12"><label className="field-label">Tratorista</label><select className="field-input" value={controlForm.tratoristaId} onChange={e=> setControlForm({...controlForm,tratoristaId:e.target.value})}><option value="">Selecione</option>{catalog.drivers.map(d=><option key={d.id} value={d.id}>{d.name}</option>)}</select></div>
                      <div className="col-6"><label className="field-label">Horímetro inicial</label><input className="field-input" type="number" step=".01" value={controlForm.horimetro_inicial} onChange={e=> setControlForm({...controlForm,horimetro_inicial:e.target.value})} /></div>
                      <div className="col-6"><label className="field-label">Horímetro final</label><input className="field-input" type="number" step=".01" value={controlForm.horimetro_final} onChange={e=> setControlForm({...controlForm,horimetro_final:e.target.value})} /></div>
                      <div className="col-12"><button className="btn btn-primary w-100">Salvar controle</button></div>
                    </form>
                  </>
                ) : <div className="patrol-detail-empty"><i className="bi bi-speedometer2"></i><h4>Selecione um controle</h4><p>Informe maquinário, tratorista e os horímetros para acompanhar o saldo de horas.</p></div>}
              </aside>
            </section>
          )}
        </div>
      </main>

      {showNovo && (
        <div id={editingId? `modalEditarServico${editingId}`:"modalNovoServico"} className="react-modal-backdrop" onMouseDown={()=> {setShowNovo(false); setEditingId(null);}}>
          <div className="modal-dialog modal-xl modal-dialog-scrollable" style={{maxWidth:760,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}>
            <div className="modal-content">
              <form onSubmit={submitNovo}>
                <div className="modal-header"><div><h5 className="modal-title">{editingId? "Editar serviço":"Novo serviço de patrulha agrícola"}</h5><small className="text-muted">Campos com * são obrigatórios.</small></div><button type="button" className="btn-close" onClick={()=> {setShowNovo(false); setEditingId(null);}}></button></div>
                <div className="modal-body">
                  <div className="row g-3 patrol-form-fields">
                    <div className="col-12 col-md-6"><label className="form-label">Número de protocolo</label><input className="form-control" value={form.numero_protocolo} onChange={e=> setForm({...form, numero_protocolo:e.target.value})} /></div>
                    <div className="col-12 col-md-6"><label className="form-label">Status *</label><select className="form-select" value={form.status} onChange={e=> setForm({...form, status:e.target.value})}><option value="PENDING">Pendente</option><option value="COMPLETED">Concluído</option><option value="CANCELLED">Cancelado</option><option value="EXPIRED">Expirado</option></select></div>
                    <div className="col-12"><label className="form-label">Cliente *</label><select className="form-select" value={form.clienteId} onChange={e=> setForm({...form, clienteId:e.target.value})} required><option value="">Selecione</option>{clients.map(c=> <option key={c.id} value={c.id}>{c.fullName} - {c.cpf}</option>)}</select></div>
                    <div className="col-12 col-md-6"><label className="form-label">Dia agendado *</label><input className="form-control" type="date" value={form.data_agendada} onChange={e=> setForm({...form, data_agendada:e.target.value})} required /></div>
                    <div className="col-12 col-md-6"><label className="form-label">Tipo de serviço *</label><select className="form-select" value={form.tipo_servicoId} onChange={e=> setForm({...form, tipo_servicoId:e.target.value})} required><option value="">Selecione</option>{catalog.serviceTypes.map(t=> <option key={t.id} value={t.id}>{t.name} · {t.area} · R$ {Number(t.hourlyValue).toFixed(2)}</option>)}</select></div>
                    <div className="col-12 col-md-6"><label className="form-label">Horas solicitadas *</label><input className="form-control" type="number" step=".01" min=".01" value={form.horas_solicitadas} onChange={e=> setForm({...form, horas_solicitadas:e.target.value})} required /></div>
                    <div className="col-12 col-md-6 payment-field"><label className="form-label">Valor total</label><input className="form-control" value={catalog.serviceTypes.find(t=> String(t.id)===String(form.tipo_servicoId))? `R$ ${(Number(catalog.serviceTypes.find(t=> String(t.id)===String(form.tipo_servicoId)).hourlyValue)*Number(form.horas_solicitadas||0)).toFixed(2)}`:"Selecione tipo e horas"} disabled /></div>
                    <div className="col-12"><label className="form-label">Endereço *</label><input className="form-control" value={form.endereco} onChange={e=> setForm({...form, endereco:e.target.value})} required /></div>
                    <div className="col-12"><hr className="patrol-divider" /></div>
                    <div className="col-12"><div className="form-check patrol-donation-check"><input className="form-check-input" type="checkbox" checked={form.e_doacao} onChange={e=> setForm({...form, e_doacao:e.target.checked})} id="doacao-check" /><label className="form-check-label" htmlFor="doacao-check">Este serviço é uma doação</label></div></div>
                    <div className="col-12 donation-origin-field" style={{display: form.e_doacao? "block":"none"}}><label className="form-label">Origem da doação</label><input className="form-control" value={form.origem_doacao} onChange={e=> setForm({...form, origem_doacao:e.target.value})} /></div>
                    <div className="col-12"><h6 className="patrol-form-section">Pagamento e comprovante</h6></div>
                    <div className="col-12 col-md-6 payment-field"><label className="form-label">Data do pagamento</label><input className="form-control" type="date" value={form.data_pagamento} onChange={e=> setForm({...form, data_pagamento:e.target.value})} /></div>
                    <div className="col-12 col-md-6 payment-field"><label className="form-label">ID do FUNDER</label><input className="form-control" value={form.id_funder} onChange={e=> setForm({...form, id_funder:e.target.value})} /></div>
                    <div className="col-12 col-md-6 payment-field"><label className="form-label">Tipo do comprovante</label><select className="form-select" value={form.tipo_comprovanteId} onChange={e=> setForm({...form, tipo_comprovanteId:e.target.value})}><option value="">Selecione</option>{catalog.paymentTypes.map(t=> <option key={t.id} value={t.id}>{t.name}</option>)}</select></div>
                    <div className="col-12 col-md-6 payment-field"><label className="form-label">Valor do FUNDER</label><input className="form-control" type="number" step=".01" value={form.valor_funder} onChange={e=> setForm({...form, valor_funder:e.target.value})} /></div>
                    <div className="col-12 payment-field"><label className="form-label">Arquivo do comprovante</label><input className="form-control" type="file" onChange={e=> setForm({...form, comprovante:e.target.files?.[0]||null})} /></div>
                  </div>
                </div>
                <div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> {setShowNovo(false); setEditingId(null);}}>Cancelar</button><button className="btn btn-primary" type="submit">{editingId? "Salvar alterações":"Cadastrar serviço"}</button></div>
              </form>
            </div>
          </div>
        </div>
      )}

      {showTipo && <div className="react-modal-backdrop" onMouseDown={()=> setShowTipo(false)}><div className="modal-dialog" style={{maxWidth:520,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><form onSubmit={async e=>{e.preventDefault(); const fd=new FormData(e.target); await api.addAgricultureCatalog("SERVICE_TYPE",{name:fd.get("nome"), area:fd.get("area"), hourlyValue: Number(fd.get("valor"))}); setShowTipo(false); load();}}><div className="modal-header"><h5 className="modal-title">Tipos de serviço</h5><button type="button" className="btn-close" onClick={()=> setShowTipo(false)}></button></div><div className="modal-body"><div className="row g-3"><div className="col-12"><label className="form-label">Nome</label><input name="nome" className="form-control" required /></div><div className="col-6"><label className="form-label">Área</label><select name="area" className="form-select" required><option value="RURAL">Rural</option><option value="URBANA">Urbana</option></select></div><div className="col-6"><label className="form-label">Valor padrão</label><input name="valor" type="number" step=".01" className="form-control" required /></div></div><div className="patrol-types-list">{catalog.serviceTypes.map(t=> <span key={t.id}>{t.name} · {t.area} · R$ {Number(t.hourlyValue).toFixed(2)}</span>)}</div></div><div className="modal-footer"><button className="btn btn-primary" type="submit">Cadastrar tipo</button></div></form></div></div></div>}
      {showComp && <div className="react-modal-backdrop" onMouseDown={()=> setShowComp(false)}><div className="modal-dialog" style={{maxWidth:520,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}><div className="modal-content"><form onSubmit={async e=>{e.preventDefault(); const fd=new FormData(e.target); await api.addAgricultureCatalog("PAYMENT_TYPE",{name:fd.get("nome")}); setShowComp(false); load();}}><div className="modal-header"><h5 className="modal-title">Tipos de comprovante</h5><button type="button" className="btn-close" onClick={()=> setShowComp(false)}></button></div><div className="modal-body"><label className="form-label">Nome</label><input name="nome" className="form-control" required /><div className="patrol-types-list">{catalog.paymentTypes.map(t=> <span key={t.id}>{t.name}</span>)}</div></div><div className="modal-footer"><button className="btn btn-primary" type="submit">Cadastrar tipo</button></div></form></div></div></div>}
    </DashboardLayout>
  );
}

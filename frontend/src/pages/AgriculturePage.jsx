import { useEffect, useMemo, useState } from "react";
import "./Patrol.css";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

function PatrolHeader({ cityHall, canManage, onNovo }) {
  return (
    <header className="patrol-header">
      <div>
        <p className="eyebrow dark mb-0">{cityHall.name}</p>
        <h3>Patrulha Agrícola</h3>
        <p className="mb-0">Controle de serviços, agenda, pagamentos e doações.</p>
      </div>
      <div className="patrol-actions">
        {canManage && (
          <>
            <a className="btn btn-outline-primary btn-outline-primary2" href="/clientes"><i className="bi bi-people"></i> Clientes</a>
            <button className="btn btn-outline-primary btn-outline-primary2" type="button" onClick={() => document.getElementById("patrulha-tipo-servico-trigger")?.click()}><i className="bi bi-tools"></i> Tipos de serviço</button>
            <button className="btn btn-outline-primary btn-outline-primary2" type="button" onClick={() => document.getElementById("patrulha-tipo-comprovante-trigger")?.click()}><i className="bi bi-receipt"></i> Comprovantes</button>
            <button className="btn btn-primary" type="button" onClick={onNovo}><i className="bi bi-plus-circle"></i> Novo serviço</button>
          </>
        )}
      </div>
    </header>
  );
}
function PatrolSummary({ totais }) {
  return (
    <section className="patrol-summary">
      <div><span>Total</span><strong>{totais.total}</strong></div>
      <div><span>Pendentes</span><strong>{totais.pendentes}</strong></div>
      <div><span>Concluídos</span><strong>{totais.concluidos}</strong></div>
      <div><span>Expirados</span><strong>{totais.expirados}</strong></div>
    </section>
  );
}
function PatrolTabs({ aba, setAba }) {
  return (
    <nav className="patrol-tabs" aria-label="Seções da patrulha agrícola">
      <a href="?aba=servicos" onClick={e=>{e.preventDefault(); setAba("servicos");}} className={aba==="servicos"?"active":""}><i className="bi bi-list-task"></i> Serviços</a>
      <a href="?aba=operacional" onClick={e=>{e.preventDefault(); setAba("operacional");}} className={aba==="operacional"?"active":""}><i className="bi bi-speedometer2"></i> Operacional</a>
    </nav>
  );
}
function PatrolServicoFields({ form, setForm, catalog, clients }) {
  const valor = useMemo(()=>{
    const t=catalog.serviceTypes.find(x=> String(x.id)===String(form.tipo_servicoId));
    const h=Number(form.horas_solicitadas||0);
    if(!t||!h) return "Selecione um tipo e informe as horas para calcular.";
    return `R$ ${(Number(t.hourlyValue)*h).toFixed(2)}`;
  },[form.tipo_servicoId, form.horas_solicitadas, catalog.serviceTypes]);
  return (
    <div className="row g-3 patrol-form-fields">
      <div className="col-12 col-md-6"><label className="form-label" htmlFor="id_numero_protocolo">Número de protocolo</label><input id="id_numero_protocolo" name="numero_protocolo" className="form-control" value={form.numero_protocolo} onChange={e=> setForm({...form, numero_protocolo:e.target.value})} /></div>
      <div className="col-12 col-md-6"><label className="form-label" htmlFor="id_status">Status *</label><select id="id_status" name="status" className="form-select" value={form.status} onChange={e=> setForm({...form, status:e.target.value})}><option value="PENDING">Pendente</option><option value="COMPLETED">Concluído</option><option value="CANCELLED">Cancelado</option><option value="EXPIRED">Expirado</option></select></div>
      <div className="col-12"><div className="patrol-field-heading"><label className="form-label" htmlFor="id_cliente">Cliente *</label><a href="/clientes" target="_blank" rel="noopener"><i className="bi bi-box-arrow-up-right"></i> Gerenciar clientes</a></div><select id="id_cliente" name="cliente" className="form-select" value={form.clienteId} onChange={e=> setForm({...form, clienteId:e.target.value})} required><option value="">Selecione</option>{clients.map(c=> <option key={c.id} value={c.id}>{c.fullName} - {c.cpf}</option>)}</select><small className="form-text">O cliente é selecionado da ferramenta Clientes.</small></div>
      <div className="col-12 col-md-6"><label className="form-label" htmlFor="id_data_agendada">Dia agendado *</label><input id="id_data_agendada" name="data_agendada" className="form-control" type="date" value={form.data_agendada} onChange={e=> setForm({...form, data_agendada:e.target.value})} required /></div>
      <div className="col-12 col-md-6"><div className="patrol-field-heading"><label className="form-label" htmlFor="id_tipo_servico">Tipo de serviço *</label><a href="#" onClick={e=>{e.preventDefault(); document.getElementById("patrulha-tipo-servico-trigger")?.click();}}><i className="bi bi-plus-circle"></i> Gerenciar</a></div><select id="id_tipo_servico" name="tipo_servico" className="form-select" value={form.tipo_servicoId} onChange={e=> setForm({...form, tipo_servicoId:e.target.value})} required><option value="">Selecione</option>{catalog.serviceTypes.map(t=> <option key={t.id} value={t.id}>{t.name} · {t.area} · R$ {Number(t.hourlyValue).toFixed(2)}</option>)}</select></div>
      <div className="col-12 col-md-6"><label className="form-label" htmlFor="id_horas_solicitadas">Horas solicitadas *</label><input id="id_horas_solicitadas" name="horas_solicitadas" className="form-control" type="number" step=".01" min=".01" value={form.horas_solicitadas} onChange={e=> setForm({...form, horas_solicitadas:e.target.value})} required /></div>
      <div className="col-12 col-md-6 payment-field"><label className="form-label" htmlFor="id_valor">Valor total do serviço *</label><input id="id_valor" name="valor" className="form-control" value={valor} disabled /><small className="form-text patrol-calculation-hint" data-valor-calculado-text>{valor}</small></div>
      <div className="col-12"><label className="form-label" htmlFor="id_endereco">Endereço *</label><input id="id_endereco" name="endereco" className="form-control" value={form.endereco} onChange={e=> setForm({...form, endereco:e.target.value})} required /></div>
      <div className="col-12"><hr className="patrol-divider" /></div>
      <div className="col-12"><div className="form-check patrol-donation-check"><input className="form-check-input" type="checkbox" checked={form.e_doacao} onChange={e=> setForm({...form, e_doacao:e.target.checked})} id="id_e_doacao" name="e_doacao" /><label className="form-check-label" htmlFor="id_e_doacao">Este serviço é uma doação</label></div><small className="form-text">Para doações, os dados financeiros não são necessários.</small></div>
      <div className="col-12 donation-origin-field" style={{display: form.e_doacao? "block":"none"}}><label className="form-label" htmlFor="id_origem_doacao">Origem da doação</label><input id="id_origem_doacao" name="origem_doacao" className="form-control" value={form.origem_doacao} onChange={e=> setForm({...form, origem_doacao:e.target.value})} /></div>
      <div className="col-12"><h6 className="patrol-form-section">Pagamento e comprovante</h6></div>
      <div className="col-12 col-md-6 payment-field"><label className="form-label" htmlFor="id_data_pagamento">Data do pagamento</label><input id="id_data_pagamento" name="data_pagamento" className="form-control" type="date" value={form.data_pagamento} onChange={e=> setForm({...form, data_pagamento:e.target.value})} /></div>
      <div className="col-12 col-md-6 payment-field"><label className="form-label" htmlFor="id_id_funder">ID do FUNDER</label><input id="id_id_funder" name="id_funder" className="form-control" value={form.id_funder} onChange={e=> setForm({...form, id_funder:e.target.value})} /></div>
      <div className="col-12 col-md-6 payment-field"><div className="patrol-field-heading"><label className="form-label" htmlFor="id_tipo_comprovante">Tipo do comprovante</label><a href="#" onClick={e=>{e.preventDefault(); document.getElementById("patrulha-tipo-comprovante-trigger")?.click();}}><i className="bi bi-plus-circle"></i> Gerenciar</a></div><select id="id_tipo_comprovante" name="tipo_comprovante" className="form-select" value={form.tipo_comprovanteId} onChange={e=> setForm({...form, tipo_comprovanteId:e.target.value})}><option value="">Selecione</option>{catalog.paymentTypes.map(t=> <option key={t.id} value={t.id}>{t.name}</option>)}</select></div>
      <div className="col-12 col-md-6 payment-field"><label className="form-label" htmlFor="id_valor_funder">Valor do FUNDER</label><input id="id_valor_funder" name="valor_funder" className="form-control" type="number" step=".01" value={form.valor_funder} onChange={e=> setForm({...form, valor_funder:e.target.value})} /></div>
      <div className="col-12 payment-field"><label className="form-label" htmlFor="id_comprovante_pagamento">Arquivo do comprovante</label><input id="id_comprovante_pagamento" name="comprovante_pagamento" className="form-control" type="file" onChange={e=> setForm({...form, comprovante:e.target.files?.[0]||null})} /></div>
    </div>
  );
}

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
  const [form,setForm]=useState({numero_protocolo:"",status:"PENDING",clienteId:"",data_agendada:"",tipo_servicoId:"",horas_solicitadas:"",endereco:"",e_doacao:false,origem_doacao:"",data_pagamento:"",id_funder:"",tipo_comprovanteId:"",valor_funder:"",comprovante:null});
  const [controlForm,setControlForm]=useState({maquinarioId:"",tratoristaId:"",horimetro_inicial:"",horimetro_final:""});
  const [message,setMessage]=useState(null);

  const load=async()=>{
    try{
      const [cat, cli, srv]=await Promise.all([api.getAgricultureCatalog(), api.getClients(), api.getAgricultureServices({query, page})]);
      setCatalog(cat); setClients(cli?.content||[]);
      const list=srv?.content||[];
      const filtered= status? list.filter(s=> String(s.status).toUpperCase()===String(status).toUpperCase()): list;
      setServices(filtered);
      if(!selectedId && filtered[0]) setSelectedId(filtered[0].id);
    }catch(e){ setMessage({type:"error", text:e.message}); }
  };
  useEffect(()=>{ load(); },[query, status, page, aba]);
  useEffect(()=>{ const url=new URL(window.location.href); url.searchParams.set("aba", aba); window.history.replaceState({}, "", url); },[aba]);

  const totais=useMemo(()=>({total:services.length, pendentes:services.filter(s=>String(s.status).toUpperCase()==="PENDING").length, concluidos:services.filter(s=>String(s.status).toUpperCase()==="COMPLETED").length, expirados:services.filter(s=>String(s.status).toUpperCase()==="EXPIRED").length}),[services]);
  const selected=useMemo(()=> services.find(s=> String(s.id)===String(selectedId))||null,[services,selectedId]);
  const controles=useMemo(()=> services.map(s=> s.control? {...s.control, servico:s}: null).filter(Boolean),[services]);
  const filteredControles=useMemo(()=>{ if(!controlQuery) return controles; const q=controlQuery.toLowerCase(); return controles.filter(c=> [c.servico?.protocol, c.servico?.clientName, c.machineryName, c.tractorDriverName].join(" ").toLowerCase().includes(q)); },[controles, controlQuery]);
  const selectedControl=useMemo(()=> filteredControles.find(c=> String(c.id)===String(selectedControlId) || String(c.servico?.id)===String(selectedControlId))||null,[filteredControles, selectedControlId]);

  async function submitServico(e){
    e.preventDefault();
    const payload={ protocol: form.numero_protocolo||undefined, status: form.status||"PENDING", clientId: form.clienteId, scheduledDate: form.data_agendada, serviceTypeId: form.tipo_servicoId, requestedHours: form.horas_solicitadas, address: form.endereco, donation: form.e_doacao, donationOrigin: form.origem_doacao, paymentDate: form.data_pagamento||null, funderId: form.id_funder, paymentProofTypeId: form.tipo_comprovanteId||null, funderAmount: form.valor_funder? Number(form.valor_funder):null };
    try{
      const created= editingId? await api.updateAgricultureService(editingId, payload): await api.createAgricultureService(payload);
      if(form.comprovante) await api.uploadAgricultureProof(created.id, form.comprovante).catch(()=>{});
      setMessage({type:"success", text: editingId? "Serviço atualizado":"Serviço cadastrado e controle operacional criado"});
      setShowNovo(false); setEditingId(null);
      load();
    }catch(err){ setMessage({type:"error", text:err.message}); }
  }

  return (
    <DashboardLayout styles={[]}>
      <main className="dashboard">
        <div className="container patrol-page">
          <PatrolHeader cityHall={cityHall} canManage={canManage} onNovo={()=>{ setForm({numero_protocolo:"",status:"PENDING",clienteId:clients[0]?.id||"",data_agendada:new Date().toISOString().slice(0,10),tipo_servicoId:catalog.serviceTypes[0]?.id||"",horas_solicitadas:"",endereco:"",e_doacao:false,origem_doacao:"",data_pagamento:"",id_funder:"",tipo_comprovanteId:"",valor_funder:"",comprovante:null}); setEditingId(null); setShowNovo(true); }} />
          <PatrolSummary totais={totais} />
          <PatrolTabs aba={aba} setAba={setAba} />
          {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}
          {aba==="servicos" ? (
            <>
              <form className="patrol-filters" onSubmit={e=>{e.preventDefault(); setPage(0); load();}}>
                <div><label htmlFor="patrol-search">Buscar</label><input id="patrol-search" name="q" value={query} onChange={e=> setQuery(e.target.value)} placeholder="Protocolo, cliente, CPF ou serviço" /></div>
                <div><label htmlFor="patrol-status">Status</label><select id="patrol-status" name="status" value={status} onChange={e=> setStatus(e.target.value)}><option value="">Todos</option><option value="PENDING">Pendente</option><option value="COMPLETED">Concluído</option><option value="CANCELLED">Cancelado</option><option value="EXPIRED">Expirado</option></select></div>
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
                      {selected.paymentProof && <a className="btn btn-outline-primary patrol-receipt" href="#"><i className="bi bi-download"></i> Baixar comprovante</a>}
                      {canManage && <button className="btn btn-primary w-100" onClick={()=>{ setForm({numero_protocolo:selected.protocol||"",status:selected.status||"PENDING",clienteId:selected.clientId||"",data_agendada:selected.scheduledDate||"",tipo_servicoId:selected.serviceTypeId||"",horas_solicitadas:selected.requestedHours||"",endereco:selected.address||"",e_doacao:selected.donation||false,origem_doacao:selected.donationOrigin||"",data_pagamento:selected.paymentDate||"",id_funder:selected.funderId||"",tipo_comprovanteId:selected.paymentProofTypeId||"",valor_funder:selected.funderAmount||"",comprovante:null}); setEditingId(selected.id); setShowNovo(true); }}><i className="bi bi-pencil-square"></i> Editar serviço</button>}
                    </>
                  ) : <div className="patrol-detail-empty"><i className="bi bi-tractor"></i><h4>Selecione um serviço</h4><p>Consulte os dados completos de um agendamento.</p></div>}
                </aside>
              </section>
            </>
          ) : (
            <>
              <section className="operational-toolbar">
                <form method="get" className="operational-search" onSubmit={e=>{e.preventDefault();}}>
                  <input type="hidden" name="aba" value="operacional" />
                  <label className="visually-hidden" htmlFor="operational-search">Buscar controle operacional</label>
                  <i className="bi bi-search"></i>
                  <input id="operational-search" name="q" value={controlQuery} onChange={e=> setControlQuery(e.target.value)} placeholder="Protocolo, cliente, maquinário ou tratorista" />
                  <button className="btn btn-primary" type="submit">Buscar</button>
                </form>
                {canManage && <div className="operational-toolbar-actions"><button className="btn btn-outline-primary" onClick={()=>{ const n=prompt("Nome do maquinário"); if(n) api.addAgricultureCatalog("MACHINERY",{name:n}).then(load); }}><i className="bi bi-truck-front"></i> Maquinários</button><button className="btn btn-outline-primary" onClick={()=>{ const n=prompt("Nome do tratorista"); if(n) api.addAgricultureCatalog("DRIVER",{name:n}).then(load); }}><i className="bi bi-person-gear"></i> Tratoristas</button></div>}
              </section>
              <section className="operational-layout">
                <div className="operational-list-panel">
                  <header className="operational-panel-header"><div><p className="eyebrow dark mb-1">Controle operacional</p><h4>Serviços em execução</h4></div><span>{filteredControles.length} registro(s)</span></header>
                  <div className="table-responsive"><table className="operational-table">
                    <thead><tr><th>Serviço</th><th>Maquinário</th><th>Tratorista</th><th>Horas realizadas</th><th>Situação</th><th></th></tr></thead>
                    <tbody>
                      {filteredControles.map(c=> (
                        <tr key={c.id} className={String(selectedControl?.id)===String(c.id)?"active":""}>
                          <td><strong>{c.servico?.protocol||"-"}</strong><small>{c.servico?.clientName||""}</small></td>
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
            </>
          )}
        </div>
      </main>

      {showNovo && (
        <div id={editingId? `modalEditarServico${editingId}`:"modalNovoServico"} className="react-modal-backdrop" onMouseDown={()=> {setShowNovo(false); setEditingId(null);}}>
          <div className="modal-dialog modal-xl modal-dialog-scrollable" style={{maxWidth:760,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}>
            <div className="modal-content">
              <form onSubmit={submitServico} data-patrulha-servico-form>
                <div className="modal-header"><div><h5 className="modal-title">{editingId? "Editar serviço":"Novo serviço de patrulha agrícola"}</h5><small className="text-muted">Campos com * são obrigatórios.</small></div><button type="button" className="btn-close" onClick={()=> {setShowNovo(false); setEditingId(null);}}></button></div>
                <div className="modal-body"><PatrolServicoFields form={form} setForm={setForm} catalog={catalog} clients={clients} /></div>
                <div className="modal-footer"><button type="button" className="btn btn-outline-secondary" onClick={()=> {setShowNovo(false); setEditingId(null);}}>Cancelar</button><button className="btn btn-primary" type="submit">{editingId? "Salvar alterações":"Cadastrar serviço"}</button></div>
              </form>
            </div>
          </div>
        </div>
      )}

      <button id="patrulha-tipo-servico-trigger" style={{display:"none"}}></button>
      <button id="patrulha-tipo-comprovante-trigger" style={{display:"none"}}></button>
    </DashboardLayout>
  );
}

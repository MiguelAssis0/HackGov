import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

function pageItems(payload) { if (Array.isArray(payload)) return payload; return payload?.content || payload?.items || []; }
function resolveCityHall(){ const s=getSelectedCityHall(); if(s?.id) return s; const u=getStoredUser()||{}; if(u?.cityHall?.id) return u.cityHall; if(u?.cityHallId) return {id:u.cityHallId, name:u.prefeitura||"Prefeitura vinculada"}; return null; }
function initials(name){ return String(name||"").split(" ").filter(Boolean).slice(0,2).map(p=>p[0]).join("").toUpperCase(); }
function maskEmail(email, canView){ if(canView) return email; if(!email||!email.includes("@")) return email; const [local,domain]=email.split("@"); return local.slice(0,2)+"***@"+domain; }

export default function EmployeesPage(){
  const cityHall=resolveCityHall();
  const user=getStoredUser();
  const userType=getUserType(user);
  const canManage=["admin_cidade","admin_equipe"].includes(userType);
  const allowCityAdmin=canManage;
  const canViewSensitive=canManage;
  const [sectors,setSectors]=useState([]);
  const [occupations,setOccupations]=useState([]);
  const [employees,setEmployees]=useState([]);
  const [loading,setLoading]=useState(true);
  const [message,setMessage]=useState(null);
  const [search,setSearch]=useState("");
  const [sectorId,setSectorId]=useState("");
  const [page,setPage]=useState(0);
  const pageSize=10;
  const [showNovo,setShowNovo]=useState(false);
  const [editing,setEditing]=useState(null);
  const [formNovo,setFormNovo]=useState({nome:"",email:"",cpf:"",celular:"",cep:"",numero_de_registro:"",setor:"",cargo:"",carga_horaria:"",salario:"",admissao:"",is_admin_cidade:false});
  const [formEdit,setFormEdit]=useState({nome:"",email:"",cpf:"",celular:"",cep:"",numero_de_registro:"",setor:"",cargo:"",carga_horaria:"",salario:"",admissao:"",data_de_desligamento:"",is_active:true,is_admin_cidade:false});

  async function load(){
    setLoading(true);
    try{
      const [empRes, secRes, occRes]=await Promise.all([api.getEmployees({q:search, setorId:sectorId, page, size:pageSize}), api.getSectors(), api.getOccupations()]);
      setEmployees(pageItems(empRes));
      setSectors(pageItems(secRes));
      setOccupations(pageItems(occRes));
    }catch(e){ setMessage({type:"error", text:e.message}); } finally{ setLoading(false); }
  }
  useEffect(()=>{ load(); },[page]);
  // filter cargos por setor selecionado (como funcionarios.js)
  const cargosNovo=useMemo(()=>{ if(!formNovo.setor) return occupations; return occupations.filter(o=> String(o.sectorId)===String(formNovo.setor) || String(o.sector?.id)===String(formNovo.setor)); },[occupations, formNovo.setor]);
  const cargosEdit=useMemo(()=>{ if(!formEdit.setor) return occupations; return occupations.filter(o=> String(o.sectorId)===String(formEdit.setor) || String(o.sector?.id)===String(formEdit.setor)); },[occupations, formEdit.setor]);

  const filtered=useMemo(()=>{
    const q=search.trim().toLowerCase();
    return employees.filter(e=>{
      const nome=[e.firstName,e.lastName].filter(Boolean).join(" ").toLowerCase();
      const email=String(e.email||"").toLowerCase();
      const matchQ=!q || nome.includes(q) || email.includes(q);
      const empSectorId=e.sectorId || e.sector?.id;
      const matchSetor=!sectorId || String(empSectorId)===String(sectorId) || String(e.sectorName).toLowerCase()===String(sectors.find(s=>String(s.id)===String(sectorId))?.name||"").toLowerCase();
      return matchQ && matchSetor;
    });
  },[employees, search, sectorId, sectors]);

  function openEdit(emp){
    const nome=[emp.firstName,emp.lastName].filter(Boolean).join(" ");
    setFormEdit({
      nome, email:emp.email||"", cpf:emp.cpf||"", celular:emp.phone||"",
      cep:emp.cep||"", numero_de_registro:emp.registrationNumber||"",
      setor:emp.sectorId||emp.sector?.id||"", cargo:emp.occupationId||emp.occupation?.id||"",
      carga_horaria:emp.hoursWorked??"", salario:emp.salary??"",
      admissao:emp.admissionDate? emp.admissionDate.slice(0,10):"",
      data_de_desligamento:emp.dismissalDate? emp.dismissalDate.slice(0,10):"",
      is_active: emp.status??true, is_admin_cidade: String(emp.role).includes("ADMIN")
    });
    setEditing(emp);
  }

  async function handleCreate(e){
    e.preventDefault();
    const [first,...rest]=formNovo.nome.trim().split(" ");
    const last=rest.join(" ");
    if(!cityHall?.id) return setMessage({type:"error", text:"Prefeitura não identificada"});
    try{
      await api.createEmployee({
        cityHallId: cityHall.id,
        sectorId: formNovo.setor,
        occupationId: formNovo.cargo,
        salary: Number(formNovo.salario)||0,
        admissionDate: formNovo.admissao? new Date(formNovo.admissao).toISOString(): new Date().toISOString(),
        registrationNumber: formNovo.numero_de_registro,
        hoursWorked: Number(formNovo.carga_horaria)||0,
        firstName: first||formNovo.nome,
        lastName: last||"Silva",
        cpf: formNovo.cpf.replace(/\D/g,""),
        email: formNovo.email,
        password: "HackGov123!",
        phone: formNovo.celular.replace(/\D/g,"")
      });
      setMessage({type:"success", text:"Funcionário cadastrado e senha gerada"});
      setShowNovo(false);
      load();
    }catch(err){ setMessage({type:"error", text:err.message}); }
  }
  async function handleUpdate(e){
    e.preventDefault();
    if(!editing) return;
    const [first,...rest]=formEdit.nome.trim().split(" ");
    const last=rest.join(" ");
    try{
      await api.updateEmployee(editing.id, {
        firstName: first, lastName: last||editing.lastName,
        email: formEdit.email, cpf: formEdit.cpf, phone: formEdit.celular,
        registrationNumber: formEdit.numero_de_registro,
        sectorId: formEdit.setor || undefined,
        occupationId: formEdit.cargo || undefined,
        salary: formEdit.salario? Number(formEdit.salario): undefined,
        hoursWorked: formEdit.carga_horaria? Number(formEdit.carga_horaria): undefined,
        admissionDate: formEdit.admissao? new Date(formEdit.admissao).toISOString(): undefined,
        dismissalDate: formEdit.data_de_desligamento? new Date(formEdit.data_de_desligamento).toISOString(): undefined,
        status: formEdit.is_active,
        isAdminCidade: formEdit.is_admin_cidade
      });
      setMessage({type:"success", text:"Alterações salvas"});
      setEditing(null);
      load();
    }catch(err){ setMessage({type:"error", text:err.message}); }
  }
  async function handleToggle(emp){
    try{ await api.toggleEmployee(emp.id); load(); }catch(err){ setMessage({type:"error", text:err.message}); }
  }

  return (
    <DashboardLayout styles={["/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
            <div>
              <p className="eyebrow dark mb-0">{cityHall?.name||"Prefeitura"}</p>
              <h3 className="funcionarios-title mb-0 fw-bold">Funcionários</h3>
            </div>
            {canManage && <button className="btn btn-primary" onClick={()=> setShowNovo(true)}><i className="bi bi-person-plus-fill"></i> Novo funcionário</button>}
          </div>

          {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}

          <section className="panel">
            <form className="row g-2 align-items-end mb-3" onSubmit={e=>{e.preventDefault(); setPage(0); load();}}>
              <div className="col-md-5">
                <label className="funcionarios-filter-label mb-1" style={{fontSize:".72rem",fontWeight:700,color:"#64748b",textTransform:"uppercase"}}>Buscar</label>
                <input className="form-control" name="q" value={search} onChange={e=> setSearch(e.target.value)} placeholder="Nome ou email" />
              </div>
              <div className="col-md-4">
                <label className="funcionarios-filter-label mb-1" style={{fontSize:".72rem",fontWeight:700,color:"#64748b",textTransform:"uppercase"}}>Setor</label>
                <select className="form-select" name="setor" value={sectorId} onChange={e=> setSectorId(e.target.value)}>
                  <option value="">Todos</option>
                  {sectors.map(s=> <option key={s.id} value={s.id}>{s.name||s.nome}</option>)}
                </select>
              </div>
              <div className="col-md-3">
                <button className="btn btn-primary w-100" type="submit"><i className="bi bi-search"></i> Filtrar</button>
              </div>
            </form>

            <div className="table-responsive funcionarios-table-wrap">
              <table className="table align-middle funcionarios-table">
                <thead><tr><th>Funcionário</th><th>Setor</th><th>Cargo</th><th>Perfil</th><th>Status</th>{canManage && <th className="text-end">Ações</th>}</tr></thead>
                <tbody>
                  {loading ? <tr><td colSpan={6} className="text-center py-4">Carregando...</td></tr> : filtered.map(emp=> {
                    const nome=[emp.firstName,emp.lastName].filter(Boolean).join(" ")||emp.email;
                    const isAdmin=String(emp.role).includes("ADMIN");
                    const isActive=emp.status!==false;
                    return (
                      <tr key={emp.id}>
                        <td><div className="user-cell" style={{display:"flex",gap:".6rem",alignItems:"center"}}><span className="avatar small" style={{width:32,height:32,borderRadius:"50%",background:"var(--azul)",color:"#fff",display:"inline-flex",alignItems:"center",justifyContent:"center",fontWeight:800,fontSize:".72rem"}}>{initials(nome)}</span><div><h4 className="mb-0" style={{fontSize:".92rem",fontWeight:700}}>{nome}</h4><small>{maskEmail(emp.email, canViewSensitive)}</small></div></div></td>
                        <td>{emp.sectorName||emp.sector?.name||"-"}</td>
                        <td>{emp.occupationName||emp.occupation?.name||"-"}</td>
                        <td>{isAdmin? <span className="badge text-bg-primary">Admin cidade</span>: <span className="badge text-bg-light">Servidor</span>}</td>
                        <td>{isActive? <span className="badge text-bg-success">Ativo</span>: <span className="badge text-bg-secondary">Inativo</span>}</td>
                        {canManage && <td className="text-end"><button className="icon-btn" title="Editar" onClick={()=> openEdit(emp)} style={{border:"1px solid #e2e8f0",borderRadius:6,padding:".3rem .5rem",marginRight:".3rem"}}><i className="bi bi-pencil"></i></button><button className="icon-btn danger" title="Ativar/desativar" onClick={()=> handleToggle(emp)} style={{border:"1px solid #fecaca",borderRadius:6,padding:".3rem .5rem",color:"#dc2626"}}><i className="bi bi-slash-circle"></i></button></td>}
                      </tr>
                    );
                  })}
                  {!loading && filtered.length===0 && <tr><td colSpan={6} className="empty-state text-center py-4">Nenhum funcionário encontrado.</td></tr>}
                </tbody>
              </table>
            </div>
            <div className="d-flex justify-content-between align-items-center mt-3">
              <small className="text-muted">Página {page+1} — {filtered.length} itens</small>
              <div className="d-flex gap-2"><button className="btn btn-outline-secondary btn-sm" disabled={page===0} onClick={()=> setPage(p=> Math.max(0,p-1))}>Anterior</button><button className="btn btn-outline-secondary btn-sm" onClick={()=> setPage(p=>p+1)}>Próxima</button></div>
            </div>
          </section>

          {canManage && showNovo && (
            <div className="react-modal-backdrop" onMouseDown={()=> setShowNovo(false)}>
              <div className="modal-dialog modal-lg modal-dialog-scrollable" style={{maxWidth:760,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}>
                <div className="modal-content">
                  <form onSubmit={handleCreate}>
                    <div className="modal-header"><h5 className="modal-title">Novo funcionário</h5><button type="button" className="btn-close" onClick={()=> setShowNovo(false)}></button></div>
                    <div className="modal-body">
                      <div className="row g-3">
                        <div className="col-md-6"><label className="form-label">Nome</label><input className="form-control" value={formNovo.nome} onChange={e=> setFormNovo({...formNovo,nome:e.target.value})} required /></div>
                        <div className="col-md-6"><label className="form-label">Email</label><input className="form-control" type="email" value={formNovo.email} onChange={e=> setFormNovo({...formNovo,email:e.target.value})} required /></div>
                        <div className="col-md-6"><label className="form-label">CPF</label><input className="form-control" value={formNovo.cpf} onChange={e=> setFormNovo({...formNovo,cpf:e.target.value})} /></div>
                        <div className="col-md-6"><label className="form-label">Celular</label><input className="form-control" value={formNovo.celular} onChange={e=> setFormNovo({...formNovo,celular:e.target.value})} /></div>
                        <div className="col-md-6"><label className="form-label">CEP</label><input className="form-control" value={formNovo.cep} onChange={e=> setFormNovo({...formNovo,cep:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Registro</label><input className="form-control" value={formNovo.numero_de_registro} onChange={e=> setFormNovo({...formNovo,numero_de_registro:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Setor</label><select className="form-select js-setor-select" value={formNovo.setor} onChange={e=> setFormNovo({...formNovo,setor:e.target.value, cargo:""})}>{[...sectors].map(s=> <option key={s.id} value={s.id}>{s.name}</option>)}<option value="">-</option></select></div>
                        <div className="col-md-4"><label className="form-label">Cargo</label><select className="form-select js-cargo-select" value={formNovo.cargo} onChange={e=> setFormNovo({...formNovo,cargo:e.target.value})}><option value="">-</option>{cargosNovo.map(c=> <option key={c.id} value={c.id} data-setor-id={c.sectorId||c.sector?.id||""}>{c.name||c.nome}</option>)}</select></div>
                        <div className="col-md-4"><label className="form-label">Carga horária</label><input className="form-control" type="number" step="0.01" value={formNovo.carga_horaria} onChange={e=> setFormNovo({...formNovo,carga_horaria:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Salário</label><input className="form-control" type="number" step="0.01" value={formNovo.salario} onChange={e=> setFormNovo({...formNovo,salario:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Admissão</label><input className="form-control" type="date" value={formNovo.admissao} onChange={e=> setFormNovo({...formNovo,admissao:e.target.value})} /></div>
                        {allowCityAdmin && <div className="col-12"><div className="form-check"><input className="form-check-input" type="checkbox" id="novo-admin-cidade" checked={formNovo.is_admin_cidade} onChange={e=> setFormNovo({...formNovo,is_admin_cidade:e.target.checked})} /><label className="form-check-label" htmlFor="novo-admin-cidade">Admin da cidade</label></div></div>}
                      </div>
                    </div>
                    <div className="modal-footer"><button className="btn btn-primary" type="submit">Cadastrar e gerar senha</button></div>
                  </form>
                </div>
              </div>
            </div>
          )}

          {editing && (
            <div className="react-modal-backdrop" onMouseDown={()=> setEditing(null)}>
              <div className="modal-dialog modal-lg modal-dialog-scrollable" style={{maxWidth:760,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}>
                <div className="modal-content">
                  <form onSubmit={handleUpdate}>
                    <div className="modal-header"><h5 className="modal-title">Editar {[editing.firstName,editing.lastName].filter(Boolean).join(" ")}</h5><button type="button" className="btn-close" onClick={()=> setEditing(null)}></button></div>
                    <div className="modal-body">
                      <div className="row g-3">
                        <div className="col-md-6"><label className="form-label">Nome</label><input className="form-control" value={formEdit.nome} onChange={e=> setFormEdit({...formEdit,nome:e.target.value})} required /></div>
                        <div className="col-md-6"><label className="form-label">Email</label><input className="form-control" type="email" value={formEdit.email} onChange={e=> setFormEdit({...formEdit,email:e.target.value})} required /></div>
                        <div className="col-md-6"><label className="form-label">CPF</label><input className="form-control" value={formEdit.cpf} onChange={e=> setFormEdit({...formEdit,cpf:e.target.value})} /></div>
                        <div className="col-md-6"><label className="form-label">Celular</label><input className="form-control" value={formEdit.celular} onChange={e=> setFormEdit({...formEdit,celular:e.target.value})} /></div>
                        <div className="col-md-6"><label className="form-label">CEP</label><input className="form-control" value={formEdit.cep} onChange={e=> setFormEdit({...formEdit,cep:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Registro</label><input className="form-control" value={formEdit.numero_de_registro} onChange={e=> setFormEdit({...formEdit,numero_de_registro:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Setor</label><select className="form-select js-setor-select" value={formEdit.setor} onChange={e=> setFormEdit({...formEdit,setor:e.target.value, cargo:""})}>{[...sectors].map(s=> <option key={s.id} value={s.id}>{s.name}</option>)}<option value="">-</option></select></div>
                        <div className="col-md-4"><label className="form-label">Cargo</label><select className="form-select js-cargo-select" value={formEdit.cargo} onChange={e=> setFormEdit({...formEdit,cargo:e.target.value})}><option value="">-</option>{cargosEdit.map(c=> <option key={c.id} value={c.id} data-setor-id={c.sectorId||c.sector?.id||""}>{c.name||c.nome}</option>)}</select></div>
                        <div className="col-md-4"><label className="form-label">Carga horária</label><input className="form-control" type="number" step="0.01" value={formEdit.carga_horaria} onChange={e=> setFormEdit({...formEdit,carga_horaria:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Salário</label><input className="form-control" type="number" step="0.01" value={formEdit.salario} onChange={e=> setFormEdit({...formEdit,salario:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Admissão</label><input className="form-control" type="date" value={formEdit.admissao} onChange={e=> setFormEdit({...formEdit,admissao:e.target.value})} /></div>
                        <div className="col-md-4"><label className="form-label">Desligamento</label><input className="form-control" type="date" value={formEdit.data_de_desligamento} onChange={e=> setFormEdit({...formEdit,data_de_desligamento:e.target.value})} /></div>
                        <div className="col-md-4 d-flex align-items-end"><label className="form-check"><input className="form-check-input" type="checkbox" checked={formEdit.is_active} onChange={e=> setFormEdit({...formEdit,is_active:e.target.checked})} /> Ativo</label></div>
                        {allowCityAdmin && <div className="col-md-4 d-flex align-items-end"><div className="form-check mb-0"><input className="form-check-input" type="checkbox" id="edit-admin-cidade" checked={formEdit.is_admin_cidade} onChange={e=> setFormEdit({...formEdit,is_admin_cidade:e.target.checked})} /><label className="form-check-label" htmlFor="edit-admin-cidade">Admin da cidade</label></div></div>}
                      </div>
                    </div>
                    <div className="modal-footer"><button className="btn btn-primary" type="submit">Salvar alterações</button></div>
                  </form>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>
    </DashboardLayout>
  );
}

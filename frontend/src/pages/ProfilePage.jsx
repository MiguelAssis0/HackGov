import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link, useRouter } from "../components/RouterContext.jsx";
import { api, clearSession, getStoredUser } from "../services/api.js";

function initials(name){ return String(name||"").split(" ").filter(Boolean).slice(0,2).map(p=>p[0]).join("").toUpperCase(); }

export default function ProfilePage(){
  const { navigate } = useRouter();
  const [profile,setProfile]=useState(null);
  const [sessions,setSessions]=useState([]);
  const [currentSessionKey,setCurrentSessionKey]=useState("");
  const [loading,setLoading]=useState(true);
  const [message,setMessage]=useState(null);
  const [editOpen,setEditOpen]=useState(false);
  const [draft,setDraft]=useState({nome:"",email:"",cpf:"",celular:"",avatar:""});
  const [settings,setSettings]=useState(()=>{ try{ return JSON.parse(localStorage.getItem("hackgov.profileSettings"))||{darkMode:document.body.classList.contains("theme-dark"), notifications:true, vlibras:false, fontSize:"Médio"} }catch{ return {darkMode:false,notifications:true,vlibras:false,fontSize:"Médio"} }});
  const [twoFactor,setTwoFactor]=useState(false);

  async function load(){
    setLoading(true);
    try{
      const [details, sess]=await Promise.all([api.getEmployeeDetails(), api.getSessions().catch(()=>[])]);
      setProfile(details);
      setDraft({nome:details?.name||"", email:details?.email||"", cpf:details?.cpf||"", celular:details?.phone||"", avatar:details?.avatarPath||""});
      setTwoFactor(Boolean(details?.twoFactor));
      const list=Array.isArray(sess)? sess: sess?.content||[];
      setSessions(list);
      // current session key from cookie/session? backend returns current via header, fallback first
      const cur=list.find(s=>s.current)?.sessionKey || list.find(s=>s.current)?.id || "";
      setCurrentSessionKey(cur);
      // sync settings
      setSettings(s=>{ const n={...s, twoFactor:Boolean(details?.twoFactor), vlibras:Boolean(details?.accessibility)}; localStorage.setItem("hackgov.profileSettings", JSON.stringify(n)); return n; });
    }catch(e){ setMessage({type:"error", text:e.message}); }
    finally{ setLoading(false); }
  }
  useEffect(()=>{ load(); },[]);
  useEffect(()=>{ document.body.classList.toggle("theme-dark", settings.darkMode); document.body.classList.toggle("vlibras", settings.vlibras); },[settings.darkMode, settings.vlibras]);
  useEffect(()=>{ const sz={Pequeno:"14px", Médio:"16px", Grande:"18px"}[settings.fontSize]||"16px"; document.documentElement.style.fontSize=sz; },[settings.fontSize]);

  function saveSettings(next){
    localStorage.setItem("hackgov.profileSettings", JSON.stringify(next));
    setSettings(next);
  }
  async function toggleSetting(key, value){
    const next={...settings, [key]:value};
    saveSettings(next);
    // auto-save para 2FA precisa backend
    if(key==="twoFactor"){
      try{
        const res=await api.toggleTwoFactor(value);
        setTwoFactor(Boolean(res.two_factor_auth ?? res.twoFactor ?? value));
        setMessage({type:"success", text: value? "2FA ativado — próximo login exigirá código por email":"2FA desativado"});
      }catch(e){ 
        // revert
        saveSettings({...next, twoFactor:!value}); 
        setMessage({type:"error", text:e.message});
      }
    }
  }
  async function saveProfile(e){
    e.preventDefault();
    try{
      await api.updateProfile({nome:draft.nome, email:draft.email, cpf:draft.cpf, celular:draft.celular, avatar:draft.avatar});
      setMessage({type:"success", text:"Perfil atualizado"});
      setEditOpen(false);
      load();
    }catch(err){ setMessage({type:"error", text:err.message}); }
  }
  async function revokeSession(key){
    if(!confirm("Remover este dispositivo? A sessão será encerrada e o usuário precisará fazer login novamente.")) return;
    try{ await api.revokeSession(key); setSessions(s=> s.filter(x=> (x.sessionKey||x.id)!==key)); }catch(err){ setMessage({type:"error", text:err.message}); }
  }
  function logout(e){ e.preventDefault(); clearSession(); navigate("/login"); }

  const user=getStoredUser();
  const prefeitura=profile?.cityhall || profile?.prefeitura || user?.prefeitura || "Sem prefeitura ativa";
  const setor=profile?.sector || profile?.setor || user?.setor || "";
  const cargo=profile?.occupation || profile?.cargo || user?.cargo || "";
  const nome=profile?.name || profile?.nome || user?.nome || "Perfil sem nome";
  const email=profile?.email || user?.email || "";
  const avatar=profile?.avatarPath || profile?.avatar || "";

  return (
    <DashboardLayout styles={["/css/perfil.css"]}>
      <div className="container perfil-page">
        {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}
        <section className="perfil-hero">
          <div className="perfil-avatar-wrap">
            {avatar ? <img className="perfil-avatar-img" src={avatar} alt={`Avatar de ${nome}`} /> : <div className="perfil-avatar-icon"><i className="bi bi-person-fill"></i></div>}
          </div>
          <div className="perfil-hero-info">
            <span>Meu perfil</span>
            <h2>{loading? "Carregando...": nome}</h2>
            <p>#{profile?.id||user?.id||""} · {email||"Sem e-mail"}</p>
            <small><i className="bi bi-building-fill"></i> {prefeitura}{setor? ` · ${setor}`:""}{cargo? ` · ${cargo}`:""}</small>
          </div>
          <button className="perfil-edit-btn" type="button" onClick={()=> setEditOpen(true)} title="Editar perfil"><i className="bi bi-pencil-fill"></i></button>
        </section>

        <div className="perfil-grid">
          <div className="perfil-main">
            <div className="perfil-card">
              <h4 className="perfil-card-title"><i className="bi bi-gear-fill"></i> Configurações</h4>
              <div className="perfil-card-body">
                <div className="perfil-setting-row">
                  <div><h4 className="mb-0">Modo escuro</h4><p>Ativa o tema escuro na plataforma.</p></div>
                  <div className="form-check form-switch"><input className="form-check-input" type="checkbox" role="switch" checked={settings.darkMode} onChange={e=> toggleSetting("darkMode", e.target.checked)} /></div>
                </div>
                <div className="perfil-setting-row">
                  <div><h4 className="mb-0">Notificações do sistema</h4><p>Receba avisos importantes sobre processos e tarefas.</p></div>
                  <div className="form-check form-switch"><input className="form-check-input" type="checkbox" role="switch" checked={settings.notifications} onChange={e=> toggleSetting("notifications", e.target.checked)} /></div>
                </div>
              </div>
            </div>

            <div className="perfil-card">
              <h4 className="perfil-card-title"><i className="bi bi-shield-fill"></i> Segurança</h4>
              <div className="perfil-card-body pb-0">
                <div className="perfil-setting-row">
                  <div><h4 className="mb-0">Autenticação de 2 fatores</h4><p>Campo preparado para ativar uma camada extra de segurança.</p></div>
                  <div className="d-flex align-items-center gap-2">
                    <span className={`perfil-status ${twoFactor? "ok":"off"}`} id="twoFactorStatus">{twoFactor? "Ativado":"Desativado"}</span>
                    <div className="form-check form-switch"><input className="form-check-input" type="checkbox" role="switch" checked={twoFactor} onChange={e=> toggleSetting("twoFactor", e.target.checked)} /></div>
                  </div>
                </div>
              </div>
              <div className="perfil-card-body border-top-0 pt-0">
                <h4 className="mb-2">Dispositivos conectados</h4>
                {sessions.map(s=>{
                  const key=s.sessionKey||s.id;
                  const isCur=key===currentSessionKey;
                  const icon=s.deviceIcon|| (s.deviceType==="mobile"?"bi-phone": s.deviceType==="tablet"?"bi-tablet":"bi-laptop");
                  return (
                    <div key={key} className={`device-card ${isCur? "device-current":""}`}>
                      <i className={`bi ${icon}`}></i>
                      <div className="device-info">
                        <h4 className="mb-1">{s.browser||"Dispositivo"}{s.os? ` — ${s.os}`:""}{isCur && <span className="device-badge-atual">Atual</span>}</h4>
                        {s.browser_version && <span className="device-detail">Versão {s.browser_version}</span>}
                        <span className="device-detail">{s.userAgent||s.user_agent||""}</span>
                        <small>{s.ipAddress||s.ip_address? `IP: ${s.ipAddress||s.ip_address} · `:""}Último acesso: {s.lastActivity||s.last_activity? new Date(s.lastActivity||s.last_activity).toLocaleString("pt-BR"):"-"}</small>
                      </div>
                      {key!==currentSessionKey && <button type="button" className="device-revoke-btn" title="Remover dispositivo" onClick={()=> revokeSession(key)}><i className="bi bi-x-lg"></i></button>}
                    </div>
                  );
                })}
                {sessions.length===0 && <p className="text-muted mb-0">Nenhum dispositivo registrado.</p>}

              </div>
            </div>

            <div className="perfil-card">
              <h4 className="perfil-card-title"><i className="bi bi-universal-access"></i> Acessibilidade</h4>
              <div className="perfil-card-body">
                <div className="perfil-setting-row">
                  <div><h4 className="mb-0">V-Libras</h4><p>Tradutor de Libras para português.</p></div>
                  <div className="form-check form-switch"><input className="form-check-input" type="checkbox" role="switch" checked={settings.vlibras} onChange={e=> toggleSetting("vlibras", e.target.checked)} /></div>
                </div>
                <div className="perfil-font-block">
                  <h4 className="mb-0">Tamanho da fonte</h4><p>Ajuste o tamanho do texto em todo o sistema.</p>
                  <div className="font-segmented">
                    {["Pequeno","Médio","Grande"].map(sz=> (
                      <label key={sz}><input type="radio" name="fontSize" checked={settings.fontSize===sz} onChange={()=> toggleSetting("fontSize", sz)} /><span>{sz}</span></label>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <aside className="perfil-side">
            <div className="perfil-side-card">
              <h4 className="mb-3">Ações do perfil</h4>
              <button type="button" onClick={()=> setEditOpen(true)}>Editar dados</button>
              <Link to="/ferramentas">Minhas ferramentas</Link>
              <Link to="/contato">Ajuda</Link>
              <a className="danger" href="/logout" onClick={logout}>Sair da conta</a>
            </div>
            <div className="perfil-side-card">
              <h4 className="mb-3">Precisa de ajuda?</h4>
              <p>Suporte: {email}</p>
              <Link className="btn btn-outline-primary btn-sm" to="/contato">Enviar mensagem</Link>
            </div>
          </aside>
        </div>

        {editOpen && (
          <div className="react-modal-backdrop" onMouseDown={()=> setEditOpen(false)}>
            <div className="modal-dialog modal-lg modal-dialog-scrollable" style={{maxWidth:760,width:"95%",margin:"1.2rem auto"}} onMouseDown={e=> e.stopPropagation()}>
              <div className="modal-content">
                <form onSubmit={saveProfile} encType="multipart/form-data">
                  <div className="modal-header"><h5 className="modal-title">Editar perfil</h5><button type="button" className="btn-close" onClick={()=> setEditOpen(false)}></button></div>
                  <div className="modal-body">
                    <div className="row g-3">
                      <div className="col-md-6"><label className="form-label">Nome</label><input className="form-control" value={draft.nome} onChange={e=> setDraft({...draft,nome:e.target.value})} /></div>
                      <div className="col-md-6"><label className="form-label">Email</label><input className="form-control" type="email" value={draft.email} onChange={e=> setDraft({...draft,email:e.target.value})} /></div>
                      <div className="col-md-6"><label className="form-label">CPF</label><input className="form-control" value={draft.cpf} onChange={e=> setDraft({...draft,cpf:e.target.value})} /></div>
                      <div className="col-md-6"><label className="form-label">Celular</label><input className="form-control" value={draft.celular} onChange={e=> setDraft({...draft,celular:e.target.value})} /></div>
                      <div className="col-12"><label className="form-label">Avatar</label><input className="form-control" type="file" accept="image/*" onChange={e=>{ const f=e.target.files?.[0]; if(!f) return; const r=new FileReader(); r.onload=()=> setDraft({...draft,avatar:r.result}); r.readAsDataURL(f); }} /></div>
                    </div>
                  </div>
                  <div className="modal-footer"><button className="btn btn-primary" type="submit">Salvar perfil</button></div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}

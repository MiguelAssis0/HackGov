import { useEffect, useState } from "react";
import {
  api,
  clearSession,
  getSelectedCityHall,
  getStoredUser,
  getUserDisplayName,
  getUserType,
  getUserTypeLabel,
  saveSelectedCityHall,
} from "../services/api.js";
import { usePageStyles } from "../hooks/usePageStyles.js";
import { Link, useRouter } from "./RouterContext.jsx";
import Messages from "./Messages.jsx";

const dashboardStyles = [
  "/css/index_dashboard.css",
  "/css/dashboard.css",
  "/css/ferramentas.css",
  "/css/setores.css",
  "/css/management.css",
  "/css/processos.css",
  "/css/tarefas.css",
  "/css/perfil.css",
  "/css/gestao.css",
  "/css/agenda.css",
  "/css/caixa-entrada.css",
  "/css/documentos.css",
  "/css/_dark_mode.css",
];

const demoUser = {
  nome: "Usuário",
  cargo: "Servidor",
  setor: "",
  prefeitura: "Prefeitura Demo",
  tipoUsuario: "usuario_comum",
};

function initials(name) {
  if (!name) return "";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content || [];
}

function normalizeCityHall(value) {
  if (!value) return null;

  if (typeof value === "string") {
    return value.trim() ? { id: value, name: value } : null;
  }

  const name = value.name || value.nome || value.prefeitura || value.cityHallName || "";
  if (!name) return null;

  return {
    id: value.id || value.cityHallId || value.prefeituraId || name,
    name,
    cnpj: value.cnpj || "",
  };
}

function userCityHall(user) {
  return (
    normalizeCityHall(user?.cityHall) ||
    normalizeCityHall(user?.prefeitura) ||
    normalizeCityHall(user?.cityHallName) ||
    (user?.cityHallId ? { id: user.cityHallId, name: "Prefeitura vinculada" } : null)
  );
}

export function DashboardLayout({ children, styles = [] }) {
  const { path, navigate } = useRouter();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [chatOpen, setChatOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("chats");
  const [cityHalls, setCityHalls] = useState([]);
  const [selectedCityHall, setSelectedCityHall] = useState(() => getSelectedCityHall());
  const user = getStoredUser() || demoUser;
  const displayName = getUserDisplayName(user);
  const userType = getUserType(user);
  const userTypeLabel = getUserTypeLabel(userType);
  const isTeamAdmin = userType === "admin_equipe";
  const fixedCityHall = userCityHall(user) || normalizeCityHall("Prefeitura Demo");
  const activeCityHall = isTeamAdmin ? selectedCityHall || fixedCityHall : fixedCityHall;
  const stylesReady = usePageStyles([...dashboardStyles, ...styles]);

  useEffect(() => {
    document.body.classList.add("dashboard-body");
    document.body.classList.remove("public-body");
    document.body.classList.toggle("modal-open", false);
    return () => document.body.classList.remove("dashboard-body");
  }, []);

  // 1:1 Django: body {% if modo_escuro %}theme-dark{% endif %} em todas as páginas — aplica globalmente, não só no perfil
  useEffect(() => {
    function apply(s){
      document.body.classList.toggle("theme-dark", !!s.darkMode);
      document.body.classList.toggle("vlibras", !!s.vlibras);
      document.body.classList.remove("font-pequeno","font-medio","font-grande");
      const f=(s.fontSize||"Médio").toLowerCase();
      document.body.classList.add(`font-${f}`);
      document.documentElement.style.fontSize= ({Pequeno:"14px", Médio:"16px", Grande:"18px"}[s.fontSize]||"16px");
      const id="vlibras-plugin-script";
      let el=document.getElementById(id);
      if(s.vlibras){
        if(!el){ el=document.createElement("script"); el.id=id; el.src="https://vlibras.gov.br/app/vlibras-plugin.js"; el.onload=()=>{ try{ window.VLibras && new window.VLibras.Widget('https://vlibras.gov.br/app'); }catch{} }; document.body.appendChild(el); }
        if(!document.querySelector("[vw]")){ const w=document.createElement("div"); w.setAttribute("vw",""); w.className="enabled"; w.innerHTML='<div vw-access-button class="active"></div><div vw-plugin-wrapper><div class="vw-plugin-top-wrapper"></div></div>'; document.body.appendChild(w); }
      } else { el?.remove(); document.querySelectorAll("[vw]").forEach(e=> e.remove()); }
    }
    try{ const cached=JSON.parse(localStorage.getItem("hackgov.profileSettings")||"null"); if(cached) apply(cached); }catch{}
    api.getProfileSettings().then(p=>{
      const s={darkMode:Boolean(p.modo_escuro??p.darkMode), notifications:p.notificacoes??p.notifications??true, vlibras:Boolean(p.vlibras), fontSize:(p.tamanho_fonte||p.fontSize||"medio").replace("medio","Médio").replace("grande","Grande").replace("pequeno","Pequeno"), twoFactor:Boolean(p.two_factor_auth??p.twoFactor)};
      localStorage.setItem("hackgov.profileSettings", JSON.stringify(s));
      apply(s);
    }).catch(()=>{});
    const onStorage=e=>{ if(e.key==="hackgov.profileSettings") try{ apply(JSON.parse(e.newValue)); }catch{} };
    window.addEventListener("storage", onStorage);
    return ()=> window.removeEventListener("storage", onStorage);
  }, []);

  useEffect(() => {
    if (!isTeamAdmin) {
      saveSelectedCityHall(null);
      setSelectedCityHall(null);
      return;
    }

    let mounted = true;

    async function loadCityHalls() {
      try {
        const response = await api.getCityHalls();
        if (!mounted) return;

        const nextCityHalls = pageItems(response)
          .map(normalizeCityHall)
          .filter(Boolean);
        setCityHalls(nextCityHalls);

        const storedCityHall = getSelectedCityHall();
        const current =
          nextCityHalls.find((cityHall) => cityHall.id === storedCityHall?.id) ||
          nextCityHalls.find((cityHall) => cityHall.id === fixedCityHall?.id) ||
          storedCityHall ||
          nextCityHalls[0] ||
          fixedCityHall;

        setSelectedCityHall(current);
        saveSelectedCityHall(current);
      } catch {
        const fallback = getSelectedCityHall() || fixedCityHall;
        setCityHalls(fallback ? [fallback] : []);
        setSelectedCityHall(fallback);
      }
    }

    loadCityHalls();

    return () => {
      mounted = false;
    };
  }, [isTeamAdmin, fixedCityHall?.id, fixedCityHall?.name]);

  async function logout(event) {
    event.preventDefault();
    clearSession();
    navigate("/login");
  }

  const isActive = (target) => path === target;
  const cityHallOptions =
    cityHalls.length > 0
      ? cityHalls
      : activeCityHall
        ? [activeCityHall]
        : [];

  if (!stylesReady) {
    return (
      <div className="route-loading" role="status" aria-live="polite">
        Carregando interface...
      </div>
    );
  }

  return (
    <section className="app-layout">
      <aside className={`sidebar ${sidebarOpen ? "open" : ""}`} id="sidebar">
        <div className="sidebar-brand">
          <Link to="/dashboard" className="brand-txt text-white">
            <i className="bi bi-file-earmark-fill"></i> Integra{" "}
            <span>Brasil</span>
          </Link>
        </div>

        <div className="sidebar-cityhall">
          <div className="sidebar-cityhall-label">
            Prefeitura
          </div>

          {isTeamAdmin ? (
            <div className="sidebar-cityhall-select-wrap">
              <select
                className="sidebar-cityhall-select"
                value={selectedCityHall?.id || ""}
                onChange={(event) => {
                  const nextCityHall =
                    cityHallOptions.find((cityHall) => String(cityHall.id) === event.target.value) ||
                    normalizeCityHall(event.target.selectedOptions[0]?.textContent);
                  setSelectedCityHall(nextCityHall);
                  saveSelectedCityHall(nextCityHall);
                }}
                aria-label="Selecionar prefeitura"
              >
                {cityHallOptions.map((cityHall) => (
                  <option value={cityHall.id} key={cityHall.id || cityHall.name}>
                    {cityHall.name}
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <div className="sidebar-cityhall-card">
              <div className="sidebar-cityhall-name">{activeCityHall?.name || "Prefeitura nao informada"}</div>
            </div>
          )}
        </div>

        <nav className="sidebar-nav">
          <div className="sidebar-cityhall-label">Principal</div>
          <Link
            className={`nav-item ${isActive("/dashboard") ? "active" : ""}`}
            to="/dashboard"
          >
            <i className="bi bi-house-fill"></i> Início
          </Link>
          <Link
            className={`nav-item ${isActive("/ferramentas") ? "active" : ""}`}
            to="/ferramentas"
          >
            <i className="bi bi-grid-1x2-fill"></i> Ferramentas
          </Link>
          <Link
            className={`nav-item ${isActive("/tarefas") ? "active" : ""}`}
            to="/tarefas"
          >
            <i className="bi bi-check2-square"></i> Tarefas
          </Link>
          <Link
            className={`nav-item ${isActive("/agenda") ? "active" : ""}`}
            to="/agenda"
          >
            <i className="bi bi-calendar3"></i> Agenda
          </Link>
          <Link className={`nav-item ${isActive("/caixa-entrada") ? "active" : ""}`} to="/caixa-entrada">
            <i className="bi bi-inbox-fill"></i> Caixa de Entrada
          </Link>
          <Link className={`nav-item ${isActive("/documentos") ? "active" : ""}`} to="/documentos">
            <i className="bi bi-folder2-open"></i> Documentos
          </Link>
          <Link
            className={`nav-item ${isActive("/funcionarios") ? "active" : ""}`}
            to="/funcionarios"
          >
            <i className="bi bi-people-fill"></i> Funcionários
          </Link>
          <Link className={`nav-item ${isActive("/auditoria") ? "active" : ""}`} to="/auditoria">
            <i className="bi bi-shield-check"></i> Auditoria
          </Link>
          <Link
            className={`nav-item ${isActive("/gestao") ? "active" : ""}`}
            to="/gestao"
          >
            <i className="bi bi-graph-up-arrow"></i> Gest&atilde;o
          </Link>

          {isTeamAdmin && (
            <>
              <div className="sidebar-cityhall-label" style={{ marginTop: "0.5rem" }}>
                Integra Brasil Admin
              </div>
              <Link
                className={`nav-item ${isActive("/nova-prefeitura") ? "active" : ""}`}
                to="/nova-prefeitura"
              >
                <i className="bi bi-building-add"></i> Nova prefeitura
              </Link>
            </>
          )}
        </nav>

        <div className="sidebar-footer">
          <div className="user-row">
            <Link to="/perfil" className="user-profile-link">
            <div className="user-avatar">
                {initials(displayName) || (
                <i
                  className="bi bi-person-fill"
                  style={{ fontSize: "1rem" }}
                ></i>
              )}
            </div>
            <div className="user-info">
                <div className="user-name">{displayName}</div>
              <div className="user-role">{user.cargo || "—"}</div>
            </div>
            </Link>
            <a
              href="/logout"
              className="user-settings-btn"
              title="Sair"
              onClick={logout}
            >
              <i className="bi bi-box-arrow-right"></i>
            </a>
          </div>
        </div>
      </aside>

      <button
        className="sidebar-toggle position-fixed top-0 start-0 m-3"
        type="button"
        aria-label="Abrir menu"
        onClick={() => setSidebarOpen(true)}
      >
        <i className="bi bi-list"></i>
      </button>

      <div
        className={`sidebar-overlay ${sidebarOpen ? "show" : ""}`}
        id="sideOverlay"
        onClick={() => setSidebarOpen(false)}
      ></div>

      {children}

      <nav className="bottom-nav">
        <div className="container">
          <div className="d-flex justify-content-around">
            <Link
              to="/dashboard"
              className={`bnav-btn ${isActive("/dashboard") ? "active" : ""}`}
            >
              <i className="bi bi-house-fill"></i>Início
            </Link>
            <Link
              to="/ferramentas"
              className={`bnav-btn ${isActive("/ferramentas") ? "active" : ""}`}
            >
              <i className="bi bi-wrench-adjustable"></i>Ferramentas
            </Link>
            <Link
              to="/tarefas"
              className={`bnav-btn ${isActive("/tarefas") ? "active" : ""}`}
            >
              <i className="bi bi-check2-square"></i>Tarefas
            </Link>
            <Link
              to="/perfil"
              className={`bnav-btn ${isActive("/perfil") ? "active" : ""}`}
            >
              <i className="bi bi-person-fill"></i>Perfil
            </Link>
            <button className="bnav-btn">
              <i className="bi bi-chat-dots-fill"></i>Chats
            </button>
          </div>
        </div>
      </nav>

      <Messages
        styles={["/css/messages.css"]}
        chatOpen={chatOpen}
        setChatOpen={setChatOpen}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />
    </section>
  );
}

import { useEffect, useMemo, useState } from "react";
import { clearSession, getStoredUser } from "../services/api.js";
import { usePageStyles } from "../hooks/usePageStyles.js";
import { Link, useRouter } from "./RouterContext.jsx";
import Messages from "./Messages.jsx";

const demoUser = {
  nome: "Usuário",
  cargo: "Servidor",
  setor: "",
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

export function DashboardLayout({ children, styles = [] }) {
  const { path, navigate } = useRouter();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [chatOpen, setChatOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("chats");
  const user = useMemo(() => getStoredUser() || demoUser, []);
  usePageStyles(["/css/index_dashboard.css", ...styles]);

  useEffect(() => {
    document.body.classList.toggle("modal-open", false);
  }, []);

  async function logout(event) {
    event.preventDefault();
    clearSession();
    navigate("/login");
  }

  const isActive = (target) => path === target;

  return (
    <section className="app-layout">
      <aside className={`sidebar ${sidebarOpen ? "open" : ""}`} id="sidebar">
        <div className="sidebar-brand">
          <Link to="/dashboard" className="brand-txt text-white">
            <i className="bi bi-file-earmark-fill"></i> Integra{" "}
            <span>Brasil</span>
          </Link>
        </div>

        <div className="sidebar-setor">
          <select defaultValue="">
            <option value="">Setor / Secretaria</option>
            <option>Administração</option>
            <option>Educação</option>
            <option>Saúde</option>
            <option>Obras</option>
            <option>Fazenda</option>
          </select>
        </div>

        <div className="sidebar-search">
          <div className="sidebar-search-wrap">
            <i className="bi bi-search"></i>
            <input
              type="text"
              className="sidebar-search-input"
              placeholder="Buscar..."
            />
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section-label">Principal</div>
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
            <i className="bi bi-wrench-adjustable"></i> Ferramentas
          </Link>
          <Link
            className={`nav-item ${isActive("/processos") ? "active" : ""}`}
            to="/processos"
          >
            <i className="bi bi-diagram-3"></i> Processos
          </Link>
          <a className="nav-item" href="#">
            <i className="bi bi-check2-square"></i> Tarefas
          </a>

          <div className="nav-section-label" style={{ marginTop: "0.5rem" }}>
            Administração
          </div>
          <a className="nav-item" href="#">
            <i className="bi bi-people"></i> Gestão
          </a>
          <a className="nav-item" href="#">
            <i className="bi bi-bell"></i> Notificações
          </a>
          <a className="nav-item" href="#">
            <i className="bi bi-gear"></i> Configurações
          </a>
        </nav>

        <div className="sidebar-footer">
          <div className="user-row">
            <div className="user-avatar">
              {initials(user.nome) || (
                <i
                  className="bi bi-person-fill"
                  style={{ fontSize: "1rem" }}
                ></i>
              )}
            </div>
            <div className="user-info">
              <div className="user-name">{user.nome}</div>
              <div className="user-role">{user.cargo || "—"}</div>
            </div>
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
            <button className="bnav-btn">
              <i className="bi bi-search"></i>Buscar
            </button>
            <button className="bnav-btn">
              <i className="bi bi-chat-dots-fill"></i>Chats
            </button>
            <a href="/logout" className="bnav-btn" onClick={logout}>
              <i className="bi bi-person-fill"></i>Perfil
            </a>
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

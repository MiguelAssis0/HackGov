import { useEffect, useMemo, useState } from "react";
import { clearSession, getStoredUser } from "../services/api.js";
import { usePageStyles } from "../hooks/usePageStyles.js";
import { Link, useRouter } from "./RouterContext.jsx";

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
            <i className="bi bi-file-earmark-fill"></i> Integra <span>Brasil</span>
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
            <input type="text" className="sidebar-search-input" placeholder="Buscar..." />
          </div>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section-label">Principal</div>
          <Link className={`nav-item ${isActive("/dashboard") ? "active" : ""}`} to="/dashboard">
            <i className="bi bi-house-fill"></i> Início
          </Link>
          <Link className={`nav-item ${isActive("/ferramentas") ? "active" : ""}`} to="/ferramentas">
            <i className="bi bi-wrench-adjustable"></i> Ferramentas
          </Link>
          <Link className={`nav-item ${isActive("/processos") ? "active" : ""}`} to="/processos">
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
              {initials(user.nome) || <i className="bi bi-person-fill" style={{ fontSize: "1rem" }}></i>}
            </div>
            <div className="user-info">
              <div className="user-name">{user.nome}</div>
              <div className="user-role">{user.cargo || "—"}</div>
            </div>
            <a href="/logout" className="user-settings-btn" title="Sair" onClick={logout}>
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
            <Link to="/dashboard" className={`bnav-btn ${isActive("/dashboard") ? "active" : ""}`}>
              <i className="bi bi-house-fill"></i>Início
            </Link>
            <Link to="/ferramentas" className={`bnav-btn ${isActive("/ferramentas") ? "active" : ""}`}>
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

      <section className="chat-float d-none d-lg-flex" id="chatFloat">
        <div className={`chat-widget ${chatOpen ? "" : "is-collapsed"}`} id="chatWidget">
          <div className="chat-widget-header">
            <div className="chat-widget-title-wrap">
              <h5 className="chat-widget-title">
                <i className="bi bi-chat-dots-fill me-1"></i> Mensagens
              </h5>
            </div>
            <div className="chat-widget-actions">
              <button
                type="button"
                className={`chat-action-btn ${chatOpen ? "is-expanded" : ""}`}
                title="Recolher"
                aria-expanded={chatOpen}
                onClick={() => setChatOpen((value) => !value)}
              >
                <i className="bi bi-chevron-up chat-chevron-icon"></i>
              </button>
            </div>
          </div>

          <div className="chat-widget-body">
            <div className="chat-widget-fixed">
              <div className="msg-busca-wrap">
                <i className="bi bi-search"></i>
                <input type="text" className="msg-busca" placeholder="Buscar mensagens..." />
              </div>
              <div className="msg-tabs">
                {[
                  ["chats", "bi-chat", "Chats"],
                  ["files", "bi-file-earmark", "Arquivos"],
                  ["contacts", "bi-person-lines-fill", "Contatos"],
                ].map(([id, icon, label]) => (
                  <button
                    key={id}
                    type="button"
                    className={`msg-tab ${activeTab === id ? "active" : ""}`}
                    onClick={() => setActiveTab(id)}
                  >
                    <i className={`bi ${icon}`}></i> {label}
                  </button>
                ))}
              </div>
            </div>

            <div className="chat-widget-scroll">
              <div className="d-flex flex-column">
                {[
                  ["FS", "Fulano da Silva (Setor de Obras)", "Enviou um documento", "bi-file-earmark-text"],
                  ["BM", "Beltrano Matos (Secretaria da Fazenda)", "Enviou uma imagem", "bi-image"],
                  ["FC", "Fulana Costa (Educação)", "Pode revisar o processo?", ""],
                  ["CL", "Ciclano Lima (Saúde)", "Reunião confirmada para amanhã", ""],
                ].map(([avatar, sender, preview, icon]) => (
                  <div className="msg-item" key={sender}>
                    <div className="msg-avatar">{avatar}</div>
                    <div className="msg-info">
                      <div className="msg-sender">{sender}</div>
                      <div className="msg-preview">
                        {icon && <i className={`bi ${icon}`}></i>} {preview}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>
    </section>
  );
}

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
  usePageStyles(["/css/index_dashboard.css", ...styles]);

  useEffect(() => {
    document.body.classList.toggle("modal-open", false);
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
          <a className="nav-item" href="#">
            <i className="bi bi-inbox-fill"></i> Caixa de Entrada
          </a>
          <Link
            className={`nav-item ${isActive("/funcionarios") ? "active" : ""}`}
            to="/funcionarios"
          >
            <i className="bi bi-people-fill"></i> Funcionários
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

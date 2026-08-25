import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link, useRouter } from "../components/RouterContext.jsx";
import { api, clearSession } from "../services/api.js";

const defaultProfile = {
  id: "",
  nome: "",
  email: "",
  cpf: "",
  celular: "",
  prefeitura: "",
  setor: "",
  cargo: "",
  avatar: "",
  twoFactor: false,
  accessibility: false,
};

function mapProfile(details) {
  return {
    id: details?.id || "",
    nome: details?.name || "",
    email: details?.email || "",
    cpf: details?.cpf || "",
    celular: details?.phone || "",
    prefeitura: details?.cityhall || "",
    setor: details?.sector || "",
    cargo: details?.occupation || "",
    avatar: details?.avatarPath || "",
    twoFactor: Boolean(details?.twoFactor),
    accessibility: Boolean(details?.accessibility),
  };
}

function hasProfileData(profile) {
  return Boolean(
    profile?.id ||
    profile?.nome ||
    profile?.email ||
    profile?.prefeitura ||
    profile?.setor ||
    profile?.cargo,
  );
}

function Accordion({ id, title, icon, openSection, setOpenSection, children }) {
  const open = openSection === id;

  return (
    <div className="perfil-accordion">
      <button
        className={`perfil-acc-head ${open ? "" : "collapsed"}`}
        type="button"
        aria-expanded={open}
        onClick={() => setOpenSection(open ? "" : id)}
      >
        <h4 className="mb-0">
          <span>
            <i className={`bi ${icon}`}></i> {title}
          </span>
        </h4>
        <i className="bi bi-chevron-down"></i>
      </button>
      {open && <div className="perfil-acc-body">{children}</div>}
    </div>
  );
}

function Switch({ checked, onChange, label }) {
  return (
    <div className="form-check form-switch">
      <input
        className="form-check-input"
        type="checkbox"
        role="switch"
        aria-label={label}
        checked={checked}
        onChange={(event) => onChange(event.target.checked)}
      />
    </div>
  );
}

function EditProfileModal({ open, profile, onClose, onSave }) {
  const [draft, setDraft] = useState(profile);

  useEffect(() => {
    setDraft(profile);
  }, [profile]);

  useEffect(() => {
    document.body.classList.toggle("modal-open", open);
    return () => document.body.classList.remove("modal-open");
  }, [open]);

  if (!open) return null;

  function update(field, value) {
    setDraft((current) => ({ ...current, [field]: value }));
  }

  function handleAvatar(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => update("avatar", reader.result);
    reader.readAsDataURL(file);
  }

  function handleSubmit(event) {
    event.preventDefault();
    onSave(draft);
  }

  return (
    <div className="react-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalEditarPerfilTitle">
      <div className="react-modal-card perfil-modal-card">
        <form onSubmit={handleSubmit}>
          <div className="modal-header">
            <h5 className="modal-title" id="modalEditarPerfilTitle">
              Editar perfil
            </h5>
            <button type="button" className="btn-close" aria-label="Fechar" onClick={onClose}></button>
          </div>
          <div className="modal-body">
            <div className="perfil-form-grid">
              <label className="form-label">
                Nome
                <input
                  className="form-control mt-1"
                  value={draft.nome}
                  onChange={(event) => update("nome", event.target.value)}
                />
              </label>
              <label className="form-label">
                Email
                <input
                  type="email"
                  className="form-control mt-1"
                  value={draft.email}
                  onChange={(event) => update("email", event.target.value)}
                />
              </label>
              <label className="form-label">
                CPF
                <input
                  className="form-control mt-1"
                  value={draft.cpf}
                  onChange={(event) => update("cpf", event.target.value)}
                />
              </label>
              <label className="form-label">
                Celular
                <input
                  className="form-control mt-1"
                  value={draft.celular}
                  onChange={(event) => update("celular", event.target.value)}
                />
              </label>
              <label className="form-label" style={{ gridColumn: "1 / -1" }}>
                Avatar
                <input type="file" className="form-control mt-1" accept="image/*" onChange={handleAvatar} />
              </label>
            </div>
          </div>
          <div className="modal-footer">
            <button className="btn btn-primary" type="submit">
              Salvar perfil
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function ProfilePage() {
  const { navigate } = useRouter();
  const [profile, setProfile] = useState(defaultProfile);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [openSection, setOpenSection] = useState("settings");
  const [editOpen, setEditOpen] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [settings, setSettings] = useState(() => {
    try {
      return (
        JSON.parse(localStorage.getItem("hackgov.profileSettings")) || {
          darkMode: false,
          notifications: true,
          twoFactor: false,
          vlibras: false,
          fontSize: "Médio",
        }
      );
    } catch {
      return { darkMode: false, notifications: true, twoFactor: false, vlibras: false, fontSize: "Médio" };
    }
  });

  useEffect(() => {
    let mounted = true;

    async function loadProfile() {
      setLoading(true);
      setLoadError("");

      try {
        const [response, sessionItems] = await Promise.all([api.getEmployeeDetails(), api.getSessions()]);
        if (!mounted) return;

        const nextProfile = mapProfile(response);
        setProfile(nextProfile);
        setSessions(sessionItems);
        setSettings((current) => {
          const next = {
            ...current,
            twoFactor: nextProfile.twoFactor,
            vlibras: nextProfile.accessibility,
          };
          saveSettings(next);
          return next;
        });
      } catch (error) {
        if (!mounted) return;
        setProfile(defaultProfile);
        setLoadError(error.message || "Nao foi possivel carregar os dados do perfil.");
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadProfile();
    return () => {
      mounted = false;
    };
  }, []);

  async function revokeSession(id) {
    if (!window.confirm("Remover este dispositivo e encerrar a sessao?")) return;
    try { await api.revokeSession(id); setSessions((items) => items.map((item) => item.id === id ? { ...item, active: false, revokedAt: new Date().toISOString() } : item)); }
    catch (error) { setLoadError(error.message); }
  }

  function saveSettings(nextSettings = settings) {
    localStorage.setItem("hackgov.profileSettings", JSON.stringify(nextSettings));
  }

  function updateSetting(field, value) {
    setSettings((current) => {
      const next = { ...current, [field]: value };
      saveSettings(next);
      return next;
    });
  }

  function saveProfile(nextProfile) {
    setProfile(nextProfile);
    setEditOpen(false);
  }

  function logout(event) {
    event.preventDefault();
    clearSession();
    navigate("/login");
  }

  return (
    <DashboardLayout styles={["/css/perfil.css"]}>
      <div className="dashboard">
        <div className="container perfil-page">
          {loadError && (
            <div className="auth-message error mb-3">
              <i className="bi bi-exclamation-circle-fill"></i>
              {loadError}
            </div>
          )}

          <section className="perfil-hero">
            <div className="perfil-avatar-wrap">
              {loading ? (
                <div className="perfil-avatar-icon">
                  <i className="bi bi-arrow-repeat"></i>
                </div>
              ) : profile.avatar ? (
                <img className="perfil-avatar-img" src={profile.avatar} alt={`Avatar de ${profile.nome}`} />
              ) : (
                <div className="perfil-avatar-icon">
                  <i className="bi bi-person-fill"></i>
                </div>
              )}
            </div>
            <div className="perfil-hero-info">
              <span>Meu perfil</span>
              <h2>{loading ? "Carregando perfil..." : profile.nome || "Perfil sem nome"}</h2>
              <p>
                {profile.id ? `#${profile.id} · ` : ""}
                {profile.email || "Sem e-mail cadastrado"}
              </p>
              <small>
                <i className="bi bi-building-fill"></i> {profile.prefeitura || "Sem prefeitura ativa"}
                {profile.setor ? ` · ${profile.setor}` : ""}
                {profile.cargo ? ` · ${profile.cargo}` : ""}
              </small>
            </div>
            <button
              className="perfil-edit-btn"
              type="button"
              title="Editar perfil"
              onClick={() => setEditOpen(true)}
              disabled={loading}
            >
              <i className="bi bi-pencil-fill"></i>
            </button>
          </section>

          {!loading && !hasProfileData(profile) && !loadError && (
            <div className="auth-message warning mb-3">
              <i className="bi bi-info-circle-fill"></i>
              Nenhum dado de perfil foi retornado pela API.
            </div>
          )}

          <div className="perfil-grid">
            <div className="perfil-main">
              <Accordion
                id="settings"
                title="Configurações"
                icon="bi-gear-fill"
                openSection={openSection}
                setOpenSection={setOpenSection}
              >
                <form
                  onSubmit={(event) => {
                    event.preventDefault();
                    saveSettings();
                  }}
                >
                  <div className="perfil-setting-row">
                    <div>
                      <h4 className="mb-0">Modo escuro</h4>
                      <p>Ativa o tema escuro na plataforma.</p>
                    </div>
                    <Switch
                      label="Modo escuro"
                      checked={settings.darkMode}
                      onChange={(value) => updateSetting("darkMode", value)}
                    />
                  </div>
                  <div className="perfil-setting-row">
                    <div>
                      <h4 className="mb-0">Notificações do sistema</h4>
                      <p>Receba avisos importantes sobre processos e tarefas.</p>
                    </div>
                    <Switch
                      label="Notificações do sistema"
                      checked={settings.notifications}
                      onChange={(value) => updateSetting("notifications", value)}
                    />
                  </div>
                  <button className="btn btn-primary btn-sm" type="submit">
                    Salvar configurações
                  </button>
                </form>
              </Accordion>

              <Accordion
                id="security"
                title="Segurança"
                icon="bi-shield-fill"
                openSection={openSection}
                setOpenSection={setOpenSection}
              >
                <form
                  onSubmit={(event) => {
                    event.preventDefault();
                    saveSettings();
                  }}
                >
                  <div className="perfil-setting-row">
                    <div>
                      <h4 className="mb-0">Autenticação de 2 fatores</h4>
                      <p>Campo preparado para ativar uma camada extra de segurança.</p>
                    </div>
                    <div className="d-flex align-items-center gap-2">
                      <span className={`perfil-status ${settings.twoFactor ? "ok" : "off"}`}>
                        {settings.twoFactor ? "Ativado" : "Desativado"}
                      </span>
                      <Switch
                        label="Autenticação de 2 fatores"
                        checked={settings.twoFactor}
                        onChange={(value) => updateSetting("twoFactor", value)}
                      />
                    </div>
                  </div>
                  <button className="btn btn-primary" type="submit">
                    Atualizar segurança
                  </button>

                  <h4 className="mb-2 mt-4">Dispositivos conectados</h4>
                  {sessions.map((session) => <div className={`device-card ${session.current ? "device-current" : ""}`} key={session.id}>
                    <i className={`bi ${session.deviceType === "mobile" ? "bi-phone" : session.deviceType === "tablet" ? "bi-tablet" : "bi-laptop"}`}></i>
                    <div className="device-info"><h4 className="mb-1">{session.browser}{session.operatingSystem ? ` — ${session.operatingSystem}` : ""} {session.current && <span className="device-badge-atual">Atual</span>}</h4><span className="device-detail">{session.userAgent}</span><small>{session.ipAddress && `IP: ${session.ipAddress} · `}Ultimo acesso: {session.lastActivity ? new Date(session.lastActivity).toLocaleString("pt-BR") : "-"}</small></div>
                    {session.active ? session.current ? <em>Ativo</em> : <button type="button" className="device-revoke-btn" onClick={() => revokeSession(session.id)}>Remover</button> : <em>Encerrada</em>}
                  </div>)}
                  {sessions.length === 0 && <div className="empty-state">Nenhuma sessao ativa encontrada.</div>}
                </form>
              </Accordion>

              <Accordion
                id="accessibility"
                title="Acessibilidade"
                icon="bi-universal-access"
                openSection={openSection}
                setOpenSection={setOpenSection}
              >
                <form
                  onSubmit={(event) => {
                    event.preventDefault();
                    saveSettings();
                  }}
                >
                  <div className="perfil-setting-row">
                    <div>
                      <h4 className="mb-0">V-Libras</h4>
                      <p>Tradutor de Libras para português.</p>
                    </div>
                    <Switch
                      label="V-Libras"
                      checked={settings.vlibras}
                      onChange={(value) => updateSetting("vlibras", value)}
                    />
                  </div>
                  <div className="perfil-font-block">
                    <h4 className="mb-0">Tamanho da fonte</h4>
                    <p>Ajuste o tamanho do texto em todo o sistema.</p>
                    <div className="font-segmented">
                      {["Pequeno", "Médio", "Grande"].map((size) => (
                        <label key={size}>
                          <input
                            type="radio"
                            name="font-size"
                            checked={settings.fontSize === size}
                            onChange={() => updateSetting("fontSize", size)}
                          />
                          <span>{size}</span>
                        </label>
                      ))}
                    </div>
                  </div>
                  <button className="btn btn-primary btn-sm mt-3" type="submit">
                    Salvar acessibilidade
                  </button>
                </form>
              </Accordion>
            </div>

            <aside className="perfil-side">
              <div className="perfil-side-card">
                <h4 className="mb-3">Ações do perfil</h4>
                <button type="button" onClick={() => setEditOpen(true)}>
                  Editar dados
                </button>
                <Link to="/ferramentas">Minhas ferramentas</Link>
                <Link to="/contato">Ajuda</Link>
                <a className="danger" href="/logout" onClick={logout}>
                  Sair da conta
                </a>
              </div>
              <div className="perfil-side-card">
                <h4 className="mb-3">Precisa de ajuda?</h4>
                <p>Suporte: {profile.email}</p>
                <Link className="btn btn-outline-primary btn-sm" to="/contato">
                  Enviar mensagem
                </Link>
              </div>
            </aside>
          </div>
        </div>
      </div>

      <EditProfileModal
        open={editOpen}
        profile={profile}
        onClose={() => setEditOpen(false)}
        onSave={saveProfile}
      />
    </DashboardLayout>
  );
}

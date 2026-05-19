import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link, useRouter } from "../components/RouterContext.jsx";
import { clearSession, getStoredUser } from "../services/api.js";

const defaultProfile = {
  id: 1,
  nome: "Admin Integra Brasil",
  email: "admin@integrabrasil.local",
  cpf: "",
  celular: "",
  prefeitura: "Prefeitura Demo",
  setor: "",
  cargo: "",
  avatar: "",
};

function loadProfile() {
  try {
    return JSON.parse(localStorage.getItem("hackgov.profile")) || null;
  } catch {
    return null;
  }
}

function mergeUserProfile() {
  const storedUser = getStoredUser() || {};
  const storedProfile = loadProfile() || {};
  const email = storedProfile.email || storedUser.email || (storedUser.nome?.includes("@") ? storedUser.nome : "");

  return {
    ...defaultProfile,
    nome:
      storedProfile.nome ||
      (storedUser.nome?.includes("@") ? defaultProfile.nome : storedUser.nome) ||
      defaultProfile.nome,
    email: email || defaultProfile.email,
    cargo: storedProfile.cargo || storedUser.cargo || defaultProfile.cargo,
    setor: storedProfile.setor || storedUser.setor || defaultProfile.setor,
    prefeitura: storedProfile.prefeitura || storedUser.prefeitura || defaultProfile.prefeitura,
    ...storedProfile,
  };
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
  const [profile, setProfile] = useState(mergeUserProfile);
  const [openSection, setOpenSection] = useState("settings");
  const [editOpen, setEditOpen] = useState(false);
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

  const userAgent = useMemo(() => navigator.userAgent.slice(0, 82), []);
  const lastAccess = useMemo(() => {
    return new Intl.DateTimeFormat("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(new Date());
  }, []);

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
    localStorage.setItem("hackgov.profile", JSON.stringify(nextProfile));
    localStorage.setItem(
      "hackgov.user",
      JSON.stringify({
        id: nextProfile.id,
        nome: nextProfile.nome,
        email: nextProfile.email,
        cargo: nextProfile.cargo,
        setor: nextProfile.setor,
        prefeitura: nextProfile.prefeitura,
      }),
    );
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
          <section className="perfil-hero">
            <div className="perfil-avatar-wrap">
              {profile.avatar ? (
                <img className="perfil-avatar-img" src={profile.avatar} alt={`Avatar de ${profile.nome}`} />
              ) : (
                <div className="perfil-avatar-icon">
                  <i className="bi bi-person-fill"></i>
                </div>
              )}
            </div>
            <div className="perfil-hero-info">
              <span>Meu perfil</span>
              <h2>{profile.nome}</h2>
              <p>
                #{profile.id} · {profile.email}
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
            >
              <i className="bi bi-pencil-fill"></i>
            </button>
          </section>

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
                  <div className="device-card">
                    <i className="bi bi-laptop"></i>
                    <div>
                      <h4 className="mb-1">Dispositivo atual</h4>
                      <span>{userAgent}</span>
                      <small>Último acesso: {lastAccess}</small>
                    </div>
                    <em>Ativo</em>
                  </div>
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

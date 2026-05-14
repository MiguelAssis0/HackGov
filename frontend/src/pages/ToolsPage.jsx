import { useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { Link } from "../components/RouterContext.jsx";

const sectors = [
  "Administração Geral",
  "Secretaria de Obras",
  "Secretaria de Educação",
  "Secretaria de Saúde",
  "Secretaria da Fazenda",
  "Assistência Social",
  "Secretaria de Urbanismo",
  "Secretaria de Meio Ambiente",
  "Secretaria de Cultura",
  "Secretaria de Transporte",
];

const categories = [
  {
    label: "Usuários e Acesso",
    items: [
      { id: "cadastro-usuario", name: "Cadastro de Usuários", icon: "bi-person-plus-fill", active: true },
      { id: "em-breve", name: "Controle de Acesso", icon: "bi-shield-lock-fill", soon: true },
    ],
  },
  {
    label: "Gestão",
    items: [
      { id: "em-breve", name: "Gestão de Setores", icon: "bi-diagram-3-fill", soon: true },
      { id: "em-breve", name: "Relatórios", icon: "bi-bar-chart-fill", soon: true },
      { id: "em-breve", name: "Agenda Municipal", icon: "bi-calendar2-check-fill", soon: true },
    ],
  },
  {
    label: "Dados",
    items: [
      { id: "em-breve", name: "Importação de Dados", icon: "bi-cloud-arrow-up-fill", soon: true },
      { id: "em-breve", name: "Exportação / Backup", icon: "bi-file-earmark-arrow-down-fill", soon: true },
    ],
  },
  {
    label: "Sistema",
    items: [
      { id: "em-breve", name: "Configurações", icon: "bi-gear-fill", soon: true },
      { id: "em-breve", name: "Notificações", icon: "bi-bell-fill", soon: true },
    ],
  },
];

const cards = [
  ["cadastro-usuario", "Cadastro de Usuários", "bi-person-plus-fill", "#e8f2ff", "var(--azul)", "Ativo", "#dcfce7", "#16a34a"],
  ["", "Controle de Acesso", "bi-shield-lock-fill", "#f3e8ff", "#7c3aed", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Gestão de Setores", "bi-diagram-3-fill", "#fef3c7", "#d97706", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Relatórios", "bi-bar-chart-fill", "#fce7f3", "#be185d", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Agenda Municipal", "bi-calendar2-check-fill", "#dcfce7", "#16a34a", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Importação de Dados", "bi-cloud-arrow-up-fill", "#e0f2fe", "#0284c7", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Exportação / Backup", "bi-file-earmark-arrow-down-fill", "#fef3c7", "#d97706", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Configurações", "bi-gear-fill", "var(--cinza-claro)", "var(--text-muted)", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
  ["", "Notificações", "bi-bell-fill", "#fee2e2", "#dc2626", "Em breve", "var(--cinza-claro)", "var(--text-muted)"],
];

function loadUsers() {
  try {
    return JSON.parse(localStorage.getItem("hackgov.users")) || [];
  } catch {
    return [];
  }
}

function saveUsers(users) {
  localStorage.setItem("hackgov.users", JSON.stringify(users));
}

function initials(name) {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

export default function ToolsPage() {
  const params = new URLSearchParams(window.location.search);
  const [activePanel, setActivePanel] = useState(params.get("ferramenta") || "inicio");
  const [toolSearch, setToolSearch] = useState("");
  const [userSearch, setUserSearch] = useState("");
  const [users, setUsers] = useState(loadUsers);
  const [message, setMessage] = useState(null);
  const [showPassword, setShowPassword] = useState({});
  const [form, setForm] = useState({
    nome: "",
    username: "",
    email: "",
    cargo: "",
    setor: "",
    perfil: "",
    senha: "",
    senha2: "",
    enviar_email: true,
  });

  const filteredUsers = useMemo(() => {
    const query = userSearch.toLowerCase();
    return users.filter((user) =>
      [user.nome, user.email, user.setor, user.cargo, user.perfil].join(" ").toLowerCase().includes(query),
    );
  }, [users, userSearch]);

  function activate(id) {
    setActivePanel(id);
    document.getElementById("ferramentaConteudo")?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submitUser(event) {
    event.preventDefault();
    if (form.senha !== form.senha2) {
      setMessage({ type: "error", text: "As senhas não coincidem." });
      return;
    }

    const nextUser = {
      id: globalThis.crypto?.randomUUID?.() || `${Date.now()}-${form.email}`,
      nome: form.nome,
      username: form.username,
      email: form.email,
      cargo: form.cargo,
      setor: form.setor,
      perfil: form.perfil,
      ativo: true,
    };
    const nextUsers = [nextUser, ...users];
    setUsers(nextUsers);
    saveUsers(nextUsers);
    setMessage({ type: "success", text: `Usuário '${form.nome}' cadastrado com sucesso!` });
    setForm({
      nome: "",
      username: "",
      email: "",
      cargo: "",
      setor: "",
      perfil: "",
      senha: "",
      senha2: "",
      enviar_email: true,
    });
  }

  return (
    <DashboardLayout styles={["/css/ferramentas.css"]}>
      <div className="dashboard">
        <div className="container">
          <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
            <div>
              <p className="section-label mb-0">Dashboard</p>
              <h3 className="mb-0" style={{ fontSize: "1.4rem", color: "var(--azul-escuro)" }}>
                Ferramentas
              </h3>
            </div>
          </div>

          <div className="ferramentas-layout">
            <div className="ferramentas-nav">
              <div className="ferramentas-nav-header">
                <div className="ferr-busca-wrap">
                  <i className="bi bi-search"></i>
                  <input
                    type="text"
                    className="ferr-busca"
                    placeholder="Buscar ferramenta..."
                    value={toolSearch}
                    onChange={(event) => setToolSearch(event.target.value)}
                  />
                </div>
              </div>

              <button
                className={`ferr-item ${activePanel === "inicio" ? "active" : ""}`}
                type="button"
                onClick={() => activate("inicio")}
              >
                <div className="ferr-item-icon">
                  <i className="bi bi-grid-fill"></i>
                </div>
                <span className="ferr-item-nome">Todas as ferramentas</span>
              </button>

              {categories.map((category) => {
                const visibleItems = category.items.filter((item) =>
                  item.name.toLowerCase().includes(toolSearch.toLowerCase()),
                );
                if (toolSearch && visibleItems.length === 0) return null;
                return (
                  <div key={category.label}>
                    {!toolSearch && <div className="ferr-categoria-label">{category.label}</div>}
                    {visibleItems.map((item, index) => (
                      <button
                        className={`ferr-item ${activePanel === item.id && item.active ? "active" : ""} ${
                          category.label === "Sistema" && index === visibleItems.length - 1 ? "mb-2" : ""
                        }`}
                        type="button"
                        data-ferramenta={item.id}
                        onClick={() => activate(item.id)}
                        key={`${category.label}-${item.name}`}
                      >
                        <div className="ferr-item-icon">
                          <i className={`bi ${item.icon}`}></i>
                        </div>
                        <span className="ferr-item-nome">{item.name}</span>
                        {item.soon && <span className="ferr-badge em-breve">Em breve</span>}
                      </button>
                    ))}
                  </div>
                );
              })}
            </div>

            <div id="ferramentaConteudo">
              <div className={`ferramenta-painel ${activePanel === "inicio" ? "active" : ""}`} id="painel-inicio">
                <div className="card-2" style={{ padding: "1.25rem 1.5rem" }}>
                  <p className="section-label mb-1">Disponíveis</p>
                  <h3 style={{ marginBottom: "0.25rem" }}>Todas as Ferramentas</h3>
                  <p style={{ fontSize: "0.85rem", color: "var(--text-muted)", margin: 0 }}>
                    Selecione uma ferramenta para começar. Novas funcionalidades são adicionadas continuamente.
                  </p>
                </div>

                <div className="ferr-grade">
                  {cards.map(([id, name, icon, bg, color, tag, tagBg, tagColor]) => {
                    const enabled = Boolean(id);
                    const Component = enabled ? "button" : "div";
                    return (
                      <Component
                        type={enabled ? "button" : undefined}
                        className={`ferr-card ${enabled ? "" : "disabled"}`}
                        onClick={enabled ? () => activate(id) : undefined}
                        key={name}
                      >
                        <div className="ferr-card-icon" style={{ background: bg, color }}>
                          <i className={`bi ${icon}`}></i>
                        </div>
                        <span className="ferr-card-nome">{name}</span>
                        <span className="ferr-card-tag" style={{ background: tagBg, color: tagColor }}>
                          {tag}
                        </span>
                      </Component>
                    );
                  })}
                </div>
              </div>

              <div
                className={`ferramenta-painel ${activePanel === "cadastro-usuario" ? "active" : ""}`}
                id="painel-cadastro-usuario"
              >
                <div className="ferramenta-header">
                  <div className="ferramenta-header-icon" style={{ background: "#e8f2ff", color: "var(--azul)" }}>
                    <i className="bi bi-person-plus-fill"></i>
                  </div>
                  <div className="flex-grow-1">
                    <h4 className="ferramenta-titulo">Cadastro de Usuários</h4>
                    <p className="ferramenta-desc">
                      Adicione novos funcionários ao sistema e defina seus perfis de acesso.
                    </p>
                  </div>
                  <button type="button" className="btn btn-primary" title="Voltar às ferramentas" onClick={() => activate("inicio")}>
                    <i className="bi bi-grid-fill"></i>
                  </button>
                </div>

                {message && (
                  <div className={`auth-message ${message.type}`}>
                    <i className={`bi ${message.type === "success" ? "bi-check-circle-fill" : "bi-exclamation-circle-fill"}`}></i>
                    {message.text}
                  </div>
                )}

                <div className="usuario-form-card">
                  <div
                    className="d-flex align-items-center justify-content-between px-4 py-3"
                    style={{ borderBottom: "var(--border1)", background: "var(--cinza-claro)" }}
                  >
                    <span style={{ fontSize: "0.85rem", fontWeight: 700, color: "var(--azul-escuro)" }}>
                      <i className="bi bi-person-fill-add primary me-1"></i> Novo Usuário
                    </span>
                  </div>

                  <div className="usuario-form-body">
                    <form id="formCadastroUsuario" onSubmit={submitUser}>
                      <div className="row g-3 mb-3">
                        <div className="col-12 col-sm-6">
                          <label className="field-label" htmlFor="u_nome">
                            Nome completo *
                          </label>
                          <input
                            type="text"
                            id="u_nome"
                            name="nome"
                            className="field-input"
                            placeholder="Nome do servidor"
                            required
                            value={form.nome}
                            onChange={(event) => update("nome", event.target.value)}
                          />
                        </div>
                        <div className="col-12 col-sm-6">
                          <label className="field-label" htmlFor="u_username">
                            Nome de usuário *
                          </label>
                          <input
                            type="text"
                            id="u_username"
                            name="username"
                            className="field-input"
                            placeholder="usuario.nome"
                            required
                            value={form.username}
                            onChange={(event) => update("username", event.target.value)}
                          />
                        </div>
                      </div>

                      <div className="row g-3 mb-3">
                        <div className="col-12 col-sm-6">
                          <label className="field-label" htmlFor="u_email">
                            E-mail institucional *
                          </label>
                          <input
                            type="email"
                            id="u_email"
                            name="email"
                            className="field-input"
                            placeholder="servidor@prefeitura.gov.br"
                            required
                            value={form.email}
                            onChange={(event) => update("email", event.target.value)}
                          />
                        </div>
                        <div className="col-12 col-sm-6">
                          <label className="field-label" htmlFor="u_cargo">
                            Cargo / Função *
                          </label>
                          <input
                            type="text"
                            id="u_cargo"
                            name="cargo"
                            className="field-input"
                            placeholder="Ex: Analista Administrativo"
                            required
                            value={form.cargo}
                            onChange={(event) => update("cargo", event.target.value)}
                          />
                        </div>
                      </div>

                      <div className="row g-3 mb-3">
                        <div className="col-12 col-sm-6">
                          <label className="field-label" htmlFor="u_setor">
                            Secretaria / Setor *
                          </label>
                          <select
                            id="u_setor"
                            name="setor"
                            className="field-input"
                            required
                            value={form.setor}
                            onChange={(event) => update("setor", event.target.value)}
                          >
                            <option value="">Selecionar...</option>
                            {sectors.map((sector) => (
                              <option key={sector}>{sector}</option>
                            ))}
                          </select>
                        </div>
                        <div className="col-12 col-sm-6">
                          <label className="field-label" htmlFor="u_perfil">
                            Perfil de Acesso *
                          </label>
                          <select
                            id="u_perfil"
                            name="perfil"
                            className="field-input"
                            required
                            value={form.perfil}
                            onChange={(event) => update("perfil", event.target.value)}
                          >
                            <option value="">Selecionar...</option>
                            <option value="servidor">Servidor - acesso básico</option>
                            <option value="gestor">Gestor - acesso ao setor</option>
                            <option value="admin">Administrador - acesso total</option>
                          </select>
                        </div>
                      </div>

                      <div className="row g-3 mb-4">
                        {[
                          ["u_senha", "senha", "Senha temporária *", "Mínimo 8 caracteres"],
                          ["u_senha2", "senha2", "Confirmar senha *", "Repita a senha"],
                        ].map(([id, field, label, placeholder]) => (
                          <div className="col-12 col-sm-6" key={id}>
                            <label className="field-label" htmlFor={id}>
                              {label}
                            </label>
                            <div className="position-relative">
                              <input
                                type={showPassword[field] ? "text" : "password"}
                                id={id}
                                name={field}
                                className="field-input"
                                placeholder={placeholder}
                                required
                                style={{ paddingRight: "2.5rem" }}
                                value={form[field]}
                                onChange={(event) => update(field, event.target.value)}
                              />
                              <button
                                type="button"
                                className="position-absolute top-50 end-0 translate-middle-y border-0 bg-transparent pe-2"
                                style={{ color: "var(--text-muted)", cursor: "pointer" }}
                                onClick={() => setShowPassword((value) => ({ ...value, [field]: !value[field] }))}
                              >
                                <i className={`bi ${showPassword[field] ? "bi-eye-slash" : "bi-eye"}`}></i>
                              </button>
                            </div>
                            {field === "senha" ? (
                              <small style={{ fontSize: "0.72rem", color: "var(--text-muted)" }}>
                                O usuário deverá alterar no primeiro acesso.
                              </small>
                            ) : (
                              <div
                                style={{
                                  display: form.senha2 && form.senha !== form.senha2 ? "block" : "none",
                                  fontSize: "0.72rem",
                                  color: "var(--vermelho)",
                                  marginTop: "0.25rem",
                                }}
                              >
                                <i className="bi bi-exclamation-circle-fill"></i> As senhas não coincidem.
                              </div>
                            )}
                          </div>
                        ))}
                      </div>

                      <div className="d-flex align-items-center justify-content-between flex-wrap gap-2">
                        <label
                          className="d-flex align-items-center gap-2"
                          style={{ fontSize: "0.82rem", color: "var(--azul-escuro)", cursor: "pointer" }}
                        >
                          <input
                            type="checkbox"
                            name="enviar_email"
                            checked={form.enviar_email}
                            onChange={(event) => update("enviar_email", event.target.checked)}
                          />
                          Enviar e-mail de boas-vindas ao usuário
                        </label>
                        <button type="submit" className="btn-primary d-flex align-items-center gap-2">
                          <i className="bi bi-person-check-fill"></i> Cadastrar Usuário
                        </button>
                      </div>
                    </form>
                  </div>
                </div>

                <div className="usuarios-table-wrap">
                  <div
                    className="d-flex align-items-center justify-content-between px-4 py-3"
                    style={{ borderBottom: "var(--border1)", background: "var(--cinza-claro)", flexWrap: "wrap", gap: "0.5rem" }}
                  >
                    <span style={{ fontSize: "0.85rem", fontWeight: 700, color: "var(--azul-escuro)" }}>
                      <i className="bi bi-people-fill primary me-1"></i> Usuários Cadastrados
                    </span>
                    <div className="position-relative">
                      <i
                        className="bi bi-search position-absolute"
                        style={{
                          left: "0.6rem",
                          top: "50%",
                          transform: "translateY(-50%)",
                          color: "var(--text-muted)",
                          fontSize: "0.75rem",
                        }}
                      ></i>
                      <input
                        type="text"
                        placeholder="Buscar usuário..."
                        style={{
                          background: "var(--branco)",
                          border: "var(--border1-5)",
                          borderRadius: "8px",
                          padding: "0.35rem 0.75rem 0.35rem 1.9rem",
                          fontSize: "0.78rem",
                          color: "var(--azul-escuro)",
                          outline: "none",
                          width: "200px",
                        }}
                        value={userSearch}
                        onChange={(event) => setUserSearch(event.target.value)}
                      />
                    </div>
                  </div>

                  <div style={{ overflowX: "auto" }}>
                    <table className="usuarios-table">
                      <thead>
                        <tr>
                          <th>Usuário</th>
                          <th>Setor</th>
                          <th>Cargo</th>
                          <th>Perfil</th>
                          <th>Status</th>
                          <th></th>
                        </tr>
                      </thead>
                      <tbody>
                        {filteredUsers.length > 0 ? (
                          filteredUsers.map((user) => (
                            <tr key={user.id}>
                              <td>
                                <div className="user-cell">
                                  <div className="user-table-avatar">{initials(user.nome)}</div>
                                  <div>
                                    <div style={{ fontWeight: 600 }}>{user.nome}</div>
                                    <div style={{ fontSize: "0.72rem", color: "var(--text-muted)" }}>{user.email}</div>
                                  </div>
                                </div>
                              </td>
                              <td>{user.setor}</td>
                              <td>{user.cargo}</td>
                              <td>
                                <span className={`role-badge role-${user.perfil}`}>{user.perfil}</span>
                              </td>
                              <td>
                                <span className="status-badge status-aberto">Ativo</span>
                              </td>
                              <td>
                                <div className="d-flex gap-1">
                                  <button className="table-action-btn" title="Editar" type="button">
                                    <i className="bi bi-pencil"></i>
                                  </button>
                                  <button className="table-action-btn danger" title="Desativar" type="button">
                                    <i className="bi bi-slash-circle"></i>
                                  </button>
                                </div>
                              </td>
                            </tr>
                          ))
                        ) : (
                          <tr>
                            <td colSpan="6">
                              <div style={{ textAlign: "center", padding: "1.5rem", color: "var(--text-muted)", fontSize: "0.85rem" }}>
                                <i
                                  className="bi bi-people"
                                  style={{
                                    fontSize: "1.5rem",
                                    display: "block",
                                    marginBottom: "0.5rem",
                                    color: "var(--border-color)",
                                  }}
                                ></i>
                                Nenhum usuário cadastrado ainda. Use o formulário acima para adicionar.
                              </div>
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>

                  <div className="table-paginacao">
                    <span>{users.length} usuário(s) cadastrado(s)</span>
                    <div className="d-flex gap-1">
                      <button className="pag-btn" disabled type="button">
                        <i className="bi bi-chevron-left"></i>
                      </button>
                      <button className="pag-btn" disabled type="button">
                        <i className="bi bi-chevron-right"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div className={`ferramenta-painel ${activePanel === "em-breve" ? "active" : ""}`} id="painel-em-breve">
                <div className="ferramenta-header">
                  <div className="ferramenta-header-icon" style={{ background: "var(--cinza-claro)", color: "var(--text-muted)" }}>
                    <i className="bi bi-tools"></i>
                  </div>
                  <div className="flex-grow-1">
                    <h4 className="ferramenta-titulo">Ferramenta em desenvolvimento</h4>
                    <p className="ferramenta-desc">Esta funcionalidade ainda não está disponível.</p>
                  </div>
                  <button type="button" className="btn btn-primary" onClick={() => activate("inicio")}>
                    <i className="bi bi-grid-fill"></i>
                  </button>
                </div>

                <div className="em-breve-wrap">
                  <i className="bi bi-hourglass-split"></i>
                  <h4 style={{ marginBottom: "0.5rem" }}>Em desenvolvimento</h4>
                  <p>Esta ferramenta está sendo desenvolvida e estará disponível em breve.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

import { useMemo, useState } from "react";
import { PublicLayout } from "../components/PublicLayout.jsx";
import { Link } from "../components/RouterContext.jsx";

const setores = [
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

function scorePassword(value) {
  let score = 0;
  if (value.length >= 8) score += 1;
  if (/[A-Z]/.test(value)) score += 1;
  if (/[0-9]/.test(value)) score += 1;
  if (/[^a-zA-Z0-9]/.test(value)) score += 1;
  return score;
}

export default function RegisterPage() {
  const [showPassword, setShowPassword] = useState({});
  const [message, setMessage] = useState(null);
  const [form, setForm] = useState({
    nome: "",
    cargo: "",
    username: "",
    email: "",
    setor: "",
    password: "",
    password_confirm: "",
    accept_terms: false,
  });

  const score = useMemo(() => scorePassword(form.password), [form.password]);
  const strength = score <= 1 ? "weak" : score <= 3 ? "medium" : "strong";
  const strengthLabel = form.password
    ? `Senha ${strength === "weak" ? "Fraca" : strength === "medium" ? "Média" : "Forte"}`
    : "Mínimo 8 caracteres, 1 maiúscula, 1 número e 1 caractere especial.";
  const passwordsMismatch = form.password_confirm && form.password !== form.password_confirm;

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    if (form.password !== form.password_confirm) {
      setMessage({ type: "error", text: "As senhas não coincidem." });
      return;
    }

    setMessage({
      type: "success",
      text: "Cadastro validado no front-end. Para ativar o usuário, use o fluxo administrativo ou a API do backend.",
    });
  }

  return (
    <PublicLayout styles={["/css/login.css"]}>
      <div className="row auth-wrapper">
        <div className="col-7 auth-panel">
          <div className="auth-panel-grid"></div>
        </div>

        <div className="col-12 col-md-5 auth-form-side">
          <div className="container">
            <div className="auth-form-box">
              <h1 className="auth-title">Criar conta</h1>
              <p className="text-muted mb-3">Preencha o formulário abaixo para criar sua conta.</p>

              {message && (
                <div className={`auth-message ${message.type}`}>
                  <i className={`bi ${message.type === "success" ? "bi-check-circle-fill" : "bi-exclamation-circle-fill"}`}></i>
                  {message.text}
                </div>
              )}

              <form id="registerForm" onSubmit={handleSubmit}>
                <div className="form-group-row">
                  <div className="form-group">
                    <label className="form-label" htmlFor="nome">
                      Nome completo
                    </label>
                    <div className="input-icon-wrap">
                      <i className="bi bi-person input-icon"></i>
                      <input
                        type="text"
                        id="nome"
                        name="nome"
                        className="form-control-custom"
                        placeholder="Seu nome completo"
                        required
                        value={form.nome}
                        onChange={(event) => update("nome", event.target.value)}
                      />
                    </div>
                  </div>
                  <div className="form-group">
                    <label className="form-label" htmlFor="cargo">
                      Cargo
                    </label>
                    <input
                      type="text"
                      id="cargo"
                      name="cargo"
                      className="form-control-custom"
                      placeholder="Ex: Analista"
                      required
                      value={form.cargo}
                      onChange={(event) => update("cargo", event.target.value)}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="username">
                    Nome de usuário
                  </label>
                  <div className="input-icon-wrap">
                    <i className="bi bi-at input-icon"></i>
                    <input
                      type="text"
                      id="username"
                      name="username"
                      className="form-control-custom"
                      placeholder="Digite um nome de usuário"
                      autoComplete="username"
                      required
                      value={form.username}
                      onChange={(event) => update("username", event.target.value)}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="email">
                    E-mail
                  </label>
                  <div className="input-icon-wrap">
                    <i className="bi bi-envelope input-icon"></i>
                    <input
                      type="email"
                      id="email"
                      name="email"
                      className="form-control-custom"
                      placeholder="seu.email@email.gov.br"
                      autoComplete="email"
                      required
                      value={form.email}
                      onChange={(event) => update("email", event.target.value)}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="setor">
                    Secretaria / Setor
                  </label>
                  <select
                    id="setor"
                    name="setor"
                    className="form-control-custom"
                    required
                    value={form.setor}
                    onChange={(event) => update("setor", event.target.value)}
                  >
                    <option value="">Selecionar setor...</option>
                    {setores.map((setor) => (
                      <option value={setor} key={setor}>
                        {setor}
                      </option>
                    ))}
                  </select>
                </div>

                {[
                  ["password", "Senha", "Digite uma senha", "new-password", "bi-lock"],
                  ["password_confirm", "Confirmar senha", "Repita a senha", "new-password", "bi-lock-fill"],
                ].map(([field, label, placeholder, autocomplete, icon]) => (
                  <div className="form-group" key={field}>
                    <label className="form-label" htmlFor={field}>
                      {label}
                    </label>
                    <div className="input-icon-wrap">
                      <i className={`bi ${icon} input-icon`}></i>
                      <input
                        type={showPassword[field] ? "text" : "password"}
                        id={field}
                        name={field}
                        className={`form-control-custom ${
                          field === "password_confirm" && passwordsMismatch ? "is-invalid" : ""
                        }`}
                        placeholder={placeholder}
                        autoComplete={autocomplete}
                        required
                        value={form[field]}
                        onChange={(event) => update(field, event.target.value)}
                      />
                      <button
                        type="button"
                        className="toggle-pass"
                        aria-label="Mostrar senha"
                        onClick={() => setShowPassword((value) => ({ ...value, [field]: !value[field] }))}
                      >
                        <i className={`bi ${showPassword[field] ? "bi-eye-slash" : "bi-eye"}`}></i>
                      </button>
                    </div>

                    {field === "password" && (
                      <div className="pw-strength">
                        <div className="pw-bars">
                          {[0, 1, 2, 3].map((index) => (
                            <div className={`pw-bar ${index < score ? strength : ""}`} key={index}></div>
                          ))}
                        </div>
                        <span className="pw-label">{strengthLabel}</span>
                      </div>
                    )}

                    {field === "password_confirm" && (
                      <div
                        id="senhaErro"
                        className="mt-1"
                        style={{
                          display: passwordsMismatch ? "block" : "none",
                          fontSize: "0.78rem",
                          color: "#dc2626",
                        }}
                      >
                        <i className="bi bi-exclamation-circle-fill"></i> As senhas não coincidem.
                      </div>
                    )}
                  </div>
                ))}

                <label className="pb-3 pt-2">
                  <input
                    type="checkbox"
                    name="accept_terms"
                    required
                    checked={form.accept_terms}
                    onChange={(event) => update("accept_terms", event.target.checked)}
                  />{" "}
                  Concordo com os <a href="#" className="text-decoration-none">Termos de Uso</a> e a{" "}
                  <a href="#" className="text-decoration-none">Política de Privacidade</a> do ERP Municipal
                </label>

                <button type="submit" className="btn btn-primary w-100" id="btnRegistrar">
                  Criar conta <i className="bi bi-person-plus-fill ms-1"></i>
                </button>
              </form>

              <div className="pt-3">
                Já tem conta?{" "}
                <Link to="/login" className="text-decoration-none">
                  Entrar agora
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </PublicLayout>
  );
}

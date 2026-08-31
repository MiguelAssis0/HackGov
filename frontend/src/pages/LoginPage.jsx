import { useState } from "react";
import { PublicLayout } from "../components/PublicLayout.jsx";
import { Link, useRouter } from "../components/RouterContext.jsx";
import { api, saveSession } from "../services/api.js";

export default function LoginPage() {
  const { navigate } = useRouter();
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({ email: "", password: "" });
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      const response = await api.login(form.email.trim(), form.password);
      await saveSession(response, form.email.trim());
      navigate("/dashboard");
    } catch (error) {
      setMessage({
        type: "error",
        text: error.message || "Não foi possível entrar. Verifique suas credenciais.",
      });
    } finally {
      setLoading(false);
    }
  }

  return (
    <PublicLayout styles={["/css/login.css"]}>
      <div className="row auth-wrapper">
        <div className="col-7 auth-panel">
          <div className="auth-panel-grid"></div>
        </div>

        <div className="col-12 col-md-5 auth-form-side">
          <div className="auth-form-box">
            <h1 className="auth-title">Entrar na conta</h1>
            <p className="text-muted mb-3">Preencha o formulário abaixo para entrar na sua conta.</p>

            {message && (
              <div className={`auth-message ${message.type}`}>
                <i className="bi bi-exclamation-circle-fill"></i>
                {message.text}
              </div>
            )}

            <form id="loginForm" onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label" htmlFor="email">
                  Email
                </label>
                <div className="input-icon-wrap">
                  <i className="bi bi-person input-icon"></i>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    className="form-control-custom"
                    placeholder="seu.email@email.com"
                    autoComplete="email"
                    required
                    value={form.email}
                    onChange={(event) => setForm((value) => ({ ...value, email: event.target.value }))}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">
                  Senha
                </label>
                <div className="input-icon-wrap">
                  <i className="bi bi-lock input-icon"></i>
                  <input
                    type={showPassword ? "text" : "password"}
                    id="password"
                    name="password"
                    className="form-control-custom"
                    placeholder="Digite sua senha"
                    autoComplete="current-password"
                    required
                    value={form.password}
                    onChange={(event) => setForm((value) => ({ ...value, password: event.target.value }))}
                  />
                  <button
                    type="button"
                    className="toggle-pass"
                    aria-label={showPassword ? "Ocultar senha" : "Mostrar senha"}
                    onClick={() => setShowPassword((value) => !value)}
                  >
                    <i className={`bi ${showPassword ? "bi-eye-slash" : "bi-eye"}`}></i>
                  </button>
                </div>
              </div>

              <div className="pb-3">
                <a href="#" className="forgot-link text-decoration-none">
                  Esqueci minha senha
                </a>
              </div>

              <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                {loading ? "Entrando..." : "Entrar"} <i className="bi bi-box-arrow-in-right ms-1"></i>
              </button>
            </form>

            <div className="pt-3">
              Ainda não tem conta?{" "}
              <Link to="/contato" className="text-decoration-none">
                Entre em contato
              </Link>
            </div>
          </div>
        </div>
      </div>
    </PublicLayout>
  );
}

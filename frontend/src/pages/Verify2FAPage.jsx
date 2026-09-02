import { useState } from "react";
import { PublicLayout } from "../components/PublicLayout.jsx";
import { Link, useRouter } from "../components/RouterContext.jsx";
import { api, saveSession } from "../services/api.js";

export default function Verify2FAPage() {
  const { navigate } = useRouter();
  const params = new URLSearchParams(window.location.search);
  const email = params.get("email") || localStorage.getItem("hackgov.pending2FA") || "";
  const [code, setCode] = useState("");
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleVerify(e) {
    e.preventDefault();
    if (!/^\d{6}$/.test(code.trim())) {
      setMessage({ type: "error", text: "Informe um código de 6 dígitos." });
      return;
    }
    setLoading(true);
    setMessage(null);
    try {
      const res = await api.verifyTwoFactor(email, code.trim());
      await saveSession(res, email);
      localStorage.removeItem("hackgov.pending2FA");
      navigate("/dashboard");
    } catch (err) {
      setMessage({ type: "error", text: err.message || "Código inválido ou expirado." });
    } finally {
      setLoading(false);
    }
  }

  async function handleResend(e) {
    e.preventDefault();
    if (!email) return setMessage({ type: "error", text: "Email não encontrado. Volte ao login." });
    try {
      await api.resendTwoFactor(email);
      setMessage({ type: "success", text: "Código reenviado para seu email." });
    } catch (err) {
      setMessage({ type: "error", text: err.message });
    }
  }

  return (
    <PublicLayout styles={["/css/login.css"]}>
      <div className="row auth-wrapper">
        <div className="col-7 auth-panel"><div className="auth-panel-grid"></div></div>
        <div className="col-12 col-md-5 auth-form-side">
          <div className="auth-form-box">
            <h1 className="auth-title">Verificação de duas etapas</h1>
            <p className="text-muted mb-1">Enviamos um código de verificação para <strong>{email || "seu email"}</strong>.</p>
            <p className="text-muted mb-3" style={{ fontSize: "0.85rem" }}>O código expira em 10 minutos.</p>

            {message && (
              <div className={`d-flex align-items-center gap-2 px-3 py-2 rounded-3 mb-3 ${message.type === "error" ? "text-danger" : "text-success"}`} style={{ background: message.type === "error" ? "#fee2e2" : "#dcfce7", fontSize: "0.85rem", fontWeight: 500 }}>
                <i className={`bi ${message.type === "error" ? "bi-exclamation-circle-fill" : "bi-check-circle-fill"}`}></i>
                {message.text}
              </div>
            )}

            <form onSubmit={handleVerify} id="verifyCodeForm">
              <div className="form-group">
                <label className="form-label" htmlFor="id_code">Código de verificação</label>
                <div className="input-icon-wrap">
                  <i className="bi bi-shield-lock input-icon"></i>
                  <input
                    id="id_code"
                    type="text"
                    name="code"
                    className="form-control-custom text-center"
                    placeholder="000000"
                    autoComplete="one-time-code"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    maxLength={6}
                    value={code}
                    onChange={(e) => setCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
                    required
                  />
                </div>
              </div>

              <div className="pb-3">
                <span className="text-muted" style={{ fontSize: "0.85rem" }}>
                  Não recebeu o código? <a href="#" onClick={handleResend} className="text-decoration-none fw-medium">Reenviar código</a>
                </span>
              </div>

              <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                {loading ? "Verificando..." : "Verificar"} <i className="bi bi-shield-check ms-1"></i>
              </button>
            </form>

            <div className="pt-3">
              <Link to="/login" className="text-decoration-none"><i className="bi bi-arrow-left me-1"></i>Voltar ao login</Link>
            </div>

          </div>
        </div>
      </div>
    </PublicLayout>
  );
}

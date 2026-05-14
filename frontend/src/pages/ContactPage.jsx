import { useState } from "react";
import { PublicLayout } from "../components/PublicLayout.jsx";

const subjects = [
  ["compra", "Desejo comprar o serviço"],
  ["suporte", "Suporte técnico"],
  ["acesso", "Problema de acesso / login"],
  ["processo", "Dúvida sobre processos"],
  ["sugestao", "Sugestão de melhoria"],
  ["bug", "Reportar um problema"],
  ["outro", "Outro"],
];

export default function ContactPage() {
  const [form, setForm] = useState({ nome: "", email: "", assunto: "", mensagem: "" });
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);
  const chars = form.mensagem.length;

  function update(field, value) {
    const nextValue = field === "mensagem" ? value.slice(0, 1000) : value;
    setForm((current) => ({ ...current, [field]: nextValue }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    window.setTimeout(() => {
      setLoading(false);
      setMessage({
        type: "success",
        text: "Mensagem registrada no front-end. Conecte um endpoint de contato para envio real por e-mail.",
      });
      setForm({ nome: "", email: "", assunto: "", mensagem: "" });
    }, 350);
  }

  return (
    <PublicLayout styles={["/css/login.css", "/css/contato.css"]}>
      <section className="contato-hero gradiente-bg">
        <div className="container py-5">
          <div className="row justify-content-center text-center">
            <div className="col-lg-6 fade-up visible">
              <p className="section-label" style={{ color: "rgba(255,255,255,0.6)" }}>
                Atendimento e Suporte
              </p>
              <h1 style={{ marginBottom: "0.75rem" }}>
                Entre em <span className="primary">contato</span>
              </h1>
              <p className="subtitle mx-auto" style={{ textAlign: "center" }}>
                Quer contratar o serviço, tem dúvidas, sugestões ou precisa de ajuda? Preencha o formulário
                e responderemos em breve.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="contato-body cinza-claro-bg py-5" style={{ borderTop: "none", minHeight: "60vh" }}>
        <div className="container">
          <div className="row g-4 justify-content-center align-items-start">
            <div className="col-12 col-lg-4 d-flex flex-column gap-3">
              {[
                ["bi-envelope-fill", "E-mail", "erpmunicipal@gmail.com"],
                ["bi-clock-fill", "Horário de atendimento", "Seg à Sex, das 8h às 18h"],
                ["bi-telephone-fill", "Telefone", "(55) 99999-9999"],
              ].map(([icon, label, value], index) => (
                <div className={`contato-info-card fade-up visible delay-${index}`} key={label}>
                  <div className="contato-info-icon">
                    <i className={`bi ${icon}`}></i>
                  </div>
                  <div>
                    <div className="contato-info-label">{label}</div>
                    <div className="contato-info-valor">{value}</div>
                  </div>
                </div>
              ))}

              <div className="card-2 fade-up visible delay-3" style={{ padding: "1.25rem 1.5rem" }}>
                <p className="section-label mb-2">Perguntas frequentes</p>
                <div className="d-flex flex-column gap-2">
                  {[
                    "Como funciona o ERP Municipal?",
                    "Como criar um novo usuário?",
                    "Quais setores têm acesso ao sistema?",
                    "Como exportar relatórios?",
                  ].map((question) => (
                    <a href="#" className="contato-faq-link" key={question}>
                      <i className="bi bi-chevron-right" style={{ color: "var(--azul)", fontSize: "0.75rem" }}></i>
                      {question}
                    </a>
                  ))}
                </div>
              </div>
            </div>

            <div className="col-12 col-lg-7">
              <div className="card-2 fade-up visible delay-1" style={{ padding: "2rem 2.25rem" }}>
                {message && (
                  <div className={`auth-message ${message.type}`}>
                    <i className="bi bi-check-circle-fill"></i>
                    {message.text}
                  </div>
                )}

                <h3 style={{ fontSize: "1.2rem", marginBottom: "0.25rem" }}>Envie sua mensagem</h3>
                <p style={{ fontSize: "0.88rem", marginBottom: "1.75rem" }}>
                  Todos os campos marcados com * são obrigatórios.
                </p>

                <form id="formContato" onSubmit={handleSubmit}>
                  <div className="form-group mb-3">
                    <label className="form-label" htmlFor="nome">
                      Nome completo *
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

                  <div className="form-group mb-3">
                    <label className="form-label" htmlFor="email">
                      E-mail *
                    </label>
                    <div className="input-icon-wrap">
                      <i className="bi bi-envelope input-icon"></i>
                      <input
                        type="email"
                        id="email"
                        name="email"
                        className="form-control-custom"
                        placeholder="seu.email@email.gov.br"
                        required
                        value={form.email}
                        onChange={(event) => update("email", event.target.value)}
                      />
                    </div>
                  </div>

                  <div className="form-group mb-3">
                    <label className="form-label" htmlFor="assunto">
                      Assunto *
                    </label>
                    <div className="input-icon-wrap">
                      <i className="bi bi-tag input-icon"></i>
                      <select
                        id="assunto"
                        name="assunto"
                        className="form-control-custom"
                        required
                        style={{ paddingLeft: "2.5rem" }}
                        value={form.assunto}
                        onChange={(event) => update("assunto", event.target.value)}
                      >
                        <option value="">Selecione um assunto...</option>
                        {subjects.map(([value, label]) => (
                          <option value={value} key={value}>
                            {label}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="form-group mb-4">
                    <label className="form-label" htmlFor="mensagem">
                      Mensagem *
                    </label>
                    <textarea
                      id="mensagem"
                      name="mensagem"
                      className="form-control-custom"
                      placeholder="Descreva sua dúvida ou problema com o máximo de detalhes possível..."
                      rows="5"
                      required
                      style={{ resize: "vertical", minHeight: "130px" }}
                      value={form.mensagem}
                      onChange={(event) => update("mensagem", event.target.value)}
                    ></textarea>
                    <div className="d-flex justify-content-end mt-1">
                      <span
                        id="charCount"
                        style={{
                          fontSize: "0.75rem",
                          color: chars > 900 ? "var(--vermelho)" : "var(--text-muted)",
                        }}
                      >
                        {chars} / 1000
                      </span>
                    </div>
                  </div>

                  <div className="d-flex align-items-center justify-content-between flex-wrap gap-3">
                    <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
                      <i className="bi bi-shield-check" style={{ color: "var(--azul)" }}></i>
                      {" "}Suas informações são tratadas com segurança conforme a LGPD.
                    </span>
                    <button type="submit" className="btn-primary d-flex align-items-center gap-2" disabled={loading}>
                      <i className={`bi ${loading ? "bi-hourglass-split" : "bi-send"}`}></i>
                      {loading ? "Enviando..." : "Enviar mensagem"}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </section>
    </PublicLayout>
  );
}

import { useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api } from "../services/api.js";
import { AccessDenied, PageHeader } from "../components/DashboardShared.jsx";
import { isTeamAdmin, useStates } from "../services/mockupService.js";

export default function CityHallFormPage() {
  const allowed = isTeamAdmin();
  const states = useStates();
  const [message, setMessage] = useState(null);
  const [form, setForm] = useState({
    name: "",
    cnpj: "",
    stateId: "",
    initialModel: "Cidade vazia",
    administrators: "",
  });

  async function submit(event) {
    event.preventDefault();
    setMessage(null);

    const payload = {
      name: form.name,
      cnpj: form.cnpj,
      stateId: form.stateId,
    };

    try {
      await api.createCityHall(payload);
      setMessage({ type: "success", text: "Prefeitura criada com sucesso." });
      setForm({
        name: "",
        cnpj: "",
        stateId: "",
        initialModel: "Cidade vazia",
        administrators: "",
      });
    } catch (error) {
      setMessage({
        type: "warning",
        text:
          error?.message ||
          "Nao foi possivel enviar ao backend agora. Confira os dados e tente novamente.",
      });
    }
  }

  return (
    <DashboardLayout styles={["/css/management.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader eyebrow="Equipe Integra Brasil" title="Cadastro de prefeitura" />

          {allowed ? (
            <section className="panel">
              {message && <div className={`auth-message ${message.type}`}>{message.text}</div>}

              <form className="row g-3" onSubmit={submit}>
                <div className="col-md-6">
                  <label className="form-label">Nome da prefeitura</label>
                  <input
                    className="form-control"
                    required
                    value={form.name}
                    onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">CNPJ</label>
                  <input
                    className="form-control"
                    required
                    value={form.cnpj}
                    onChange={(event) => setForm((current) => ({ ...current, cnpj: event.target.value }))}
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Estado</label>
                  <select
                    className="form-select"
                    required
                    value={form.stateId}
                    onChange={(event) => setForm((current) => ({ ...current, stateId: event.target.value }))}
                  >
                    <option value="">---------</option>
                    {states.map((state) => (
                      <option value={state.id} key={state.id}>
                        {state.name}
                        {state.uf ? ` - ${state.uf}` : ""}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label">Modelo inicial</label>
                  <select
                    className="form-select"
                    value={form.initialModel}
                    onChange={(event) => setForm((current) => ({ ...current, initialModel: event.target.value }))}
                  >
                    <option>Cidade vazia</option>
                    <option>Prefeitura demo</option>
                    <option>Copiar estrutura atual</option>
                  </select>
                </div>
                <div className="col-12">
                  <label className="form-label">Administradores da cidade</label>
                  <textarea
                    className="form-control cityhall-admins-textarea"
                    value={form.administrators}
                    onChange={(event) => setForm((current) => ({ ...current, administrators: event.target.value }))}
                  ></textarea>
                  <div className="form-text">Um por linha: Nome; email; CPF opcional.</div>
                </div>
                <div className="col-12">
                  <button className="btn btn-primary" type="submit">
                    <i className="bi bi-check2-circle"></i> Criar prefeitura
                  </button>
                </div>
              </form>
            </section>
          ) : (
            <AccessDenied />
          )}
        </div>
      </main>
    </DashboardLayout>
  );
}

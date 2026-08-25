import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";

const emptyForm = { fullName: "", nickname: "", cpf: "", phone: "", secondaryContact: "", address: "", stateRegistration: "", caf: "" };

export default function ClientsPage() {
  const [clients, setClients] = useState([]);
  const [query, setQuery] = useState("");
  const [selected, setSelected] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [service, setService] = useState({ area: "Agricultura e Desenvolvimento Rural", description: "", observation: "", serviceDate: new Date().toISOString().slice(0, 10) });
  const [message, setMessage] = useState(null);

  async function load(search = query) {
    try { const payload = await api.getClients(search); setClients(payload?.content || []); }
    catch (error) { setMessage({ type: "error", text: error.message }); }
  }

  useEffect(() => { const timer = window.setTimeout(() => load(query), 250); return () => window.clearTimeout(timer); }, [query]);

  async function openClient(client) {
    try { setSelected(await api.getClient(client.id)); }
    catch (error) { setMessage({ type: "error", text: error.message }); }
  }

  function newClient() { setEditingId(null); setForm(emptyForm); setFormOpen(true); }
  function editClient(client) { setEditingId(client.id); setForm({ fullName: client.fullName, nickname: client.nickname || "", cpf: client.cpf, phone: client.phone, secondaryContact: client.secondaryContact || "", address: client.address || "", stateRegistration: client.stateRegistration || "", caf: client.caf || "" }); setFormOpen(true); }

  async function submit(event) {
    event.preventDefault();
    try {
      const response = editingId ? await api.updateClient(editingId, form) : await api.createClient(form);
      setFormOpen(false); setSelected(response); setMessage({ type: "success", text: "Cliente salvo com sucesso." }); await load();
    } catch (error) { setMessage({ type: "error", text: error.message }); }
  }

  async function addService(event) {
    event.preventDefault();
    try { await api.addClientService(selected.id, service); setSelected(await api.getClient(selected.id)); setService({ ...service, description: "", observation: "" }); }
    catch (error) { setMessage({ type: "error", text: error.message }); }
  }

  return <DashboardLayout styles={["/css/clientes.css"]}>
    <main className="dashboard"><div className="container">
      <PageHeader eyebrow="Cadastro unificado da prefeitura" title="Clientes Gerais" action={<button className="btn btn-primary" onClick={newClient}><i className="bi bi-person-plus-fill"></i> Novo cliente</button>} />
      {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}
      <section className="clients-shell">
        <div className="clients-list-panel">
          <div className="tarefas-search-wrap m-3"><i className="bi bi-search"></i><input className="tarefas-search" placeholder="Buscar por nome ou apelido" value={query} onChange={(e) => setQuery(e.target.value)} /></div>
          {clients.map((client) => <button className={`client-row ${selected?.id === client.id ? "active" : ""}`} key={client.id} onClick={() => openClient(client)}><div className="tarefa-avatar">{client.fullName.split(/\s+/).slice(0,2).map((part) => part[0]).join("")}</div><div><strong>{client.fullName}</strong><small>{client.cpf} · {client.phone}</small></div></button>)}
          {clients.length === 0 && <div className="empty-state">Nenhum cliente encontrado.</div>}
        </div>
        <article className="client-folder">
          {selected ? <>
            <div className="d-flex justify-content-between gap-2"><div><p className="eyebrow dark mb-1">Pasta do cliente</p><h2>{selected.fullName}</h2></div>{!selected.masked && <button className="btn btn-outline-primary" onClick={() => editClient(selected)}>Editar</button>}</div>
            {selected.masked && <div className="auth-message warning">Dados sensiveis mascarados para o seu perfil.</div>}
            <div className="client-data-grid"><div><span>CPF</span><strong>{selected.cpf}</strong></div><div><span>Telefone</span><strong>{selected.phone}</strong></div><div><span>Endereco</span><strong>{selected.address || "-"}</strong></div><div><span>CAF</span><strong>{selected.caf || "-"}</strong></div></div>
            <h3 className="mt-4">Historico de atendimentos</h3>
            <form className="client-service-form" onSubmit={addService}><input className="field-input" required maxLength="280" placeholder="Descricao do atendimento" value={service.description} onChange={(e) => setService({ ...service, description: e.target.value })} /><input className="field-input" type="date" max={new Date().toISOString().slice(0,10)} value={service.serviceDate} onChange={(e) => setService({ ...service, serviceDate: e.target.value })} /><input className="field-input" maxLength="2000" placeholder="Observacao" value={service.observation} onChange={(e) => setService({ ...service, observation: e.target.value })} /><button className="btn btn-primary">Registrar</button></form>
            <div className="client-timeline">{selected.services.map((item) => <article key={item.id}><span>{new Date(`${item.serviceDate}T12:00:00`).toLocaleDateString("pt-BR")}</span><div><strong>{item.description}</strong><p>{item.observation || item.area}</p><small>{item.createdByName}</small></div></article>)}</div>
          </> : <div className="empty-state">Selecione um cliente para abrir a pasta.</div>}
        </article>
      </section>
    </div></main>
    {formOpen && <div className="react-modal-backdrop" role="dialog" aria-modal="true"><div className="react-modal-card client-modal"><div className="task-modal-header"><h3>{editingId ? "Editar cliente" : "Novo cliente"}</h3><button className="btn-acao" onClick={() => setFormOpen(false)}><i className="bi bi-x-lg"></i></button></div><form className="row g-3" onSubmit={submit}>{Object.entries({ fullName: "Nome completo *", nickname: "Apelido", cpf: "CPF *", phone: "Telefone *", secondaryContact: "Contato secundario", address: "Endereco", stateRegistration: "Inscricao estadual", caf: "CAF" }).map(([key,label]) => <div className={key === "address" ? "col-12" : "col-md-6"} key={key}><label className="field-label">{label}</label><input className="field-input" required={["fullName","cpf","phone"].includes(key)} value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} /></div>)}<div className="col-12 text-end"><button className="btn btn-primary">Salvar cliente</button></div></form></div></div>}
  </DashboardLayout>;
}

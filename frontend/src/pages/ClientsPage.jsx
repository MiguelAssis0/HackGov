import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { EmptyState, Modal, PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";

const emptyForm = { fullName: "", nickname: "", cpf: "", phone: "", secondaryContact: "", address: "", stateRegistration: "", caf: "" };
const today = () => new Date().toISOString().slice(0, 10);

function pageItems(payload) {
  return Array.isArray(payload) ? payload : payload?.content || [];
}

function initials(name) {
  return String(name || "Cliente").split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase();
}

function formatDateTime(value) {
  if (!value) return "Sistema";
  return new Date(value).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

export default function ClientsPage() {
  const [clients, setClients] = useState([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({ totalPages: 1 });
  const [selected, setSelected] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [attendanceOpen, setAttendanceOpen] = useState(false);
  const [attendance, setAttendance] = useState({ area: "Agricultura e Desenvolvimento Rural", serviceDate: today(), observation: "" });
  const [canManage, setCanManage] = useState(false);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(null);

  async function load(search = query, currentPage = page) {
    setLoading(true);
    try {
      const payload = await api.getClients(search, currentPage);
      setClients(pageItems(payload));
      setPageInfo(payload || { totalPages: 1 });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel carregar os clientes." });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let mounted = true;
    Promise.all([api.getClientCapabilities(), api.getClients("", 0)])
      .then(([capabilities, payload]) => {
        if (!mounted) return;
        setCanManage(Boolean(capabilities?.canManage));
        setClients(pageItems(payload));
        setPageInfo(payload || { totalPages: 1 });
      })
      .catch((error) => { if (mounted) setMessage({ type: "error", text: error.message || "Nao foi possivel carregar os clientes." }); })
      .finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, []);

  function searchClients(event) {
    event.preventDefault();
    setSelected(null);
    setPage(0);
    load(query, 0);
  }

  function changePage(nextPage) {
    setSelected(null);
    setPage(nextPage);
    load(query, nextPage);
  }

  async function openClient(client) {
    try { setSelected(await api.getClient(client.id)); }
    catch (error) { setMessage({ type: "error", text: error.message || "Nao foi possivel abrir a pasta do cliente." }); }
  }

  function newClient() {
    setEditingId(null);
    setForm({ ...emptyForm });
    setFormOpen(true);
  }

  function editClient(client) {
    setEditingId(client.id);
    setForm({ fullName: client.fullName || "", nickname: client.nickname || "", cpf: client.cpf || "", phone: client.phone || "", secondaryContact: client.secondaryContact || "", address: client.address || "", stateRegistration: client.stateRegistration || "", caf: client.caf || "" });
    setFormOpen(true);
  }

  async function submit(event) {
    event.preventDefault();
    setBusy(true);
    try {
      const response = editingId ? await api.updateClient(editingId, form) : await api.createClient(form);
      setFormOpen(false);
      setSelected(response);
      setMessage({ type: "success", text: editingId ? "Cliente atualizado." : "Cliente cadastrado com sucesso." });
      await load();
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel salvar o cliente." });
    } finally {
      setBusy(false);
    }
  }

  function openAttendance() {
    setAttendance({ area: "Agricultura e Desenvolvimento Rural", serviceDate: today(), observation: "" });
    setAttendanceOpen(true);
  }

  async function addService(event) {
    event.preventDefault();
    setBusy(true);
    try {
      await api.addClientService(selected.id, attendance);
      setSelected(await api.getClient(selected.id));
      setAttendanceOpen(false);
      setMessage({ type: "success", text: "Atendimento registrado na pasta do cliente." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel registrar o atendimento." });
    } finally {
      setBusy(false);
    }
  }

  return <DashboardLayout styles={["/css/clientes.css", "/css/management.css"]}>
    <main className="dashboard"><div className="container clients-page">
      <PageHeader eyebrow="Cadastro unificado da prefeitura" title="Clientes" action={canManage && <button className="btn btn-primary" type="button" onClick={newClient}><i className="bi bi-person-plus-fill"></i> Novo cliente</button>} />
      <p className="clients-intro">Cadastros compartilhados e pasta de atendimentos municipais.</p>
      {message && <div className={`auth-message ${message.type} mb-3`}><i className="bi bi-info-circle-fill"></i> {message.text}</div>}

      <section className="clients-layout">
        <aside className="clients-list-panel">
          <form method="get" className="clients-search" onSubmit={searchClients}>
            <label className="visually-hidden" htmlFor="clientes-search">Buscar cliente</label>
            <i className="bi bi-search"></i>
            <input id="clientes-search" name="q" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Nome, apelido ou CPF" />
          </form>
          <div className="clients-list">
            {loading ? <p className="clients-empty">Carregando clientes...</p> : clients.map((client) => <button type="button" className={`client-item ${selected?.id === client.id ? "active" : ""}`} key={client.id} onClick={() => openClient(client)}><strong>{client.fullName}</strong><span>{client.cpf}{client.nickname ? ` · ${client.nickname}` : ""}</span></button>)}
            {!loading && clients.length === 0 && <p className="clients-empty">Nenhum cliente encontrado.</p>}
          </div>
          {pageInfo.totalPages > 1 && <nav className="clients-pagination" aria-label="Paginação de clientes"><button type="button" disabled={page <= 0} onClick={() => changePage(page - 1)}>Anterior</button><span>Página {page + 1} de {pageInfo.totalPages}</span><button type="button" disabled={page >= pageInfo.totalPages - 1} onClick={() => changePage(page + 1)}>Próxima</button></nav>}
        </aside>

        <section className="client-folder-panel">
          {selected ? <>
            <div className="client-folder-header">
              <div><p className="eyebrow dark mb-1">Pasta de atendimentos</p><h4>{selected.fullName}</h4><p className="mb-0">CPF {selected.cpf} · {selected.phone}</p></div>
              {canManage && <div className="client-folder-actions"><button className="btn btn-outline-primary" type="button" onClick={() => editClient(selected)}><i className="bi bi-pencil-square"></i> Editar</button><button className="btn btn-primary" type="button" onClick={openAttendance}><i className="bi bi-journal-plus"></i> Anotar atendimento</button></div>}
            </div>
            {selected.masked && <div className="auth-message warning mt-3">Dados sensíveis mascarados para o seu perfil.</div>}
            <dl className="client-data-grid">
              {selected.masked ? <div><dt>Dados privados</dt><dd>Visibilidade restrita.</dd></div> : <><div><dt>Endereço</dt><dd>{selected.address || "Não informado"}</dd></div><div><dt>Contato secundário</dt><dd>{selected.secondaryContact || "Não informado"}</dd></div><div><dt>Inscrição estadual</dt><dd>{selected.stateRegistration || "Não informada"}</dd></div><div><dt>CAF</dt><dd>{selected.caf || "Não informado"}</dd></div></>}
            </dl>
            <div className="client-attendance-title"><h5>Histórico de atendimentos</h5><span>{selected.services?.length || 0} registro{selected.services?.length === 1 ? "" : "s"}</span></div>
            <div className="client-attendance-list">
              {selected.services?.length ? selected.services.map((item) => <article className="client-attendance-item" key={item.id}><div className="client-attendance-icon"><i className="bi bi-check2-circle"></i></div><div><strong>{item.description}</strong>{item.observation && <p>{item.observation}</p>}<span>{item.createdByName || "Sistema"} · {formatDateTime(item.createdAt)}</span></div></article>) : <p className="clients-empty">Ainda não há atendimentos registrados para este cliente.</p>}
            </div>
          </> : <div className="clients-empty-state"><i className="bi bi-folder2-open"></i><h4>Selecione um cliente</h4><p>Abra um cadastro para consultar seus dados e atendimentos.</p></div>}
        </section>
      </section>
    </div></main>

    <Modal open={formOpen} size="lg" title={editingId ? "Editar cliente" : "Novo cliente"} onClose={() => setFormOpen(false)} footer={<><button className="btn btn-outline-secondary" type="button" onClick={() => setFormOpen(false)}>Cancelar</button><button className="btn btn-primary" type="submit" form="clientForm" disabled={busy}>{busy ? "Salvando..." : editingId ? "Salvar alterações" : "Cadastrar cliente"}</button></>}>
      <form id="clientForm" onSubmit={submit} className="row g-3">
        <div className="col-12 col-md-8"><label className="form-label" htmlFor="client-full-name">Nome completo *</label><input id="client-full-name" className="form-control" required maxLength="180" value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} /></div>
        <div className="col-12 col-md-4"><label className="form-label" htmlFor="client-nickname">Apelido</label><input id="client-nickname" className="form-control" maxLength="100" value={form.nickname} onChange={(event) => setForm({ ...form, nickname: event.target.value })} /></div>
        <div className="col-12 col-md-6"><label className="form-label" htmlFor="client-cpf">CPF *</label><input id="client-cpf" className="form-control" required placeholder="000.000.000-00" value={form.cpf} onChange={(event) => setForm({ ...form, cpf: event.target.value })} /></div>
        <div className="col-12 col-md-6"><label className="form-label" htmlFor="client-phone">Telefone *</label><input id="client-phone" className="form-control" required maxLength="30" placeholder="(00) 00000-0000" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></div>
        <div className="col-12 col-md-6"><label className="form-label" htmlFor="client-secondary">Contato secundário</label><input id="client-secondary" className="form-control" maxLength="100" value={form.secondaryContact} onChange={(event) => setForm({ ...form, secondaryContact: event.target.value })} /></div>
        <div className="col-12 col-md-6"><label className="form-label" htmlFor="client-state-registration">Inscrição estadual</label><input id="client-state-registration" className="form-control" maxLength="50" value={form.stateRegistration} onChange={(event) => setForm({ ...form, stateRegistration: event.target.value })} /></div>
        <div className="col-12 col-md-6"><label className="form-label" htmlFor="client-caf">CAF</label><input id="client-caf" className="form-control" maxLength="50" value={form.caf} onChange={(event) => setForm({ ...form, caf: event.target.value })} /></div>
        <div className="col-12"><label className="form-label" htmlFor="client-address">Endereço</label><textarea id="client-address" className="form-control" rows="2" maxLength="500" value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })}></textarea></div>
      </form>
    </Modal>

    <Modal open={attendanceOpen} title="Anotar atendimento" onClose={() => setAttendanceOpen(false)} footer={<><button className="btn btn-outline-secondary" type="button" onClick={() => setAttendanceOpen(false)}>Cancelar</button><button className="btn btn-primary" type="submit" form="attendanceForm" disabled={busy}>{busy ? "Registrando..." : "Registrar"}</button></>}>
      <form id="attendanceForm" onSubmit={addService} className="row g-3">
        <div className="col-12"><label className="form-label" htmlFor="attendance-area">Área responsável</label><input id="attendance-area" className="form-control" required maxLength="140" value={attendance.area} onChange={(event) => setAttendance({ ...attendance, area: event.target.value })} /></div>
        <div className="col-12"><label className="form-label" htmlFor="attendance-date">Data do atendimento</label><input id="attendance-date" className="form-control" type="date" required max={today()} value={attendance.serviceDate} onChange={(event) => setAttendance({ ...attendance, serviceDate: event.target.value })} /></div>
        <div className="col-12"><label className="form-label" htmlFor="attendance-observation">Observação interna</label><textarea id="attendance-observation" className="form-control" rows="3" maxLength="2000" value={attendance.observation} onChange={(event) => setAttendance({ ...attendance, observation: event.target.value })}></textarea></div>
      </form>
    </Modal>
  </DashboardLayout>;
}

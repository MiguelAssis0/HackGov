import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { AccessDenied, EmptyState, IconButton, PageHeader } from "../components/DashboardShared.jsx";
import { Link } from "../components/RouterContext.jsx";
import { api, getSelectedCityHall, getStoredUser, getUserType } from "../services/api.js";

const ADMIN_ONLY = new Set(["setores", "cargos", "controle-acesso"]);
const PAGE_SIZE = 15;
const emptyForm = { tool: "", sector: "", occupation: "", employee: "", level: "VIEW", dataScope: "ALL_SECTORS", visibleSectorIds: [] };

function pageItems(payload) {
  return Array.isArray(payload) ? payload : payload?.content || payload?.items || [];
}

function cityHallName() {
  const selected = getSelectedCityHall();
  if (selected?.name) return selected.name;
  const user = getStoredUser() || {};
  if (user.cityHall?.name) return user.cityHall.name;
  if (typeof user.cityHall === "string" && user.cityHall.trim()) return user.cityHall;
  if (user.prefeitura?.name) return user.prefeitura.name;
  return "Prefeitura vinculada";
}

function employeeName(item) {
  return item.name || item.fullName || [item.firstName, item.lastName].filter(Boolean).join(" ") || item.email || "Funcionario";
}

function occupationName(item) {
  return item.name || item.nome || "Cargo";
}

function levelLabel(level) {
  return { VIEW: "Visualizar", MANAGE: "Gerenciar", ADMIN: "Admin" }[level] || level;
}

export default function AccessControlPage() {
  const canConfigure = ["admin_cidade", "admin_equipe"].includes(getUserType(getStoredUser()));
  const [tools, setTools] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [occupations, setOccupations] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [permissionPage, setPermissionPage] = useState(0);
  const [policyTool, setPolicyTool] = useState("");
  const [policyMode, setPolicyMode] = useState("padrao");
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(canConfigure);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(null);

  const availableTools = useMemo(() => tools.filter((tool) => !ADMIN_ONLY.has(tool.id)).sort((a, b) => a.name.localeCompare(b.name, "pt-BR")), [tools]);
  const selectedPolicyTool = availableTools.find((tool) => tool.id === policyTool);
  const isReports = form.tool === "relatorios";
  const permissionPages = Math.max(1, Math.ceil(permissions.length / PAGE_SIZE));
  const currentPermissionPage = Math.min(permissionPage, permissionPages - 1);
  const visiblePermissions = permissions.slice(currentPermissionPage * PAGE_SIZE, (currentPermissionPage + 1) * PAGE_SIZE);

  useEffect(() => {
    if (!canConfigure) return undefined;
    let mounted = true;
    Promise.all([api.getTools(), api.getSectors(), api.getOccupations(), api.getEmployees(), api.getToolPermissions()])
      .then(([toolData, sectorData, occupationData, employeeData, permissionData]) => {
        if (!mounted) return;
        const toolItems = pageItems(toolData);
        setTools(toolItems);
        setSectors(pageItems(sectorData).filter((item) => item.active ?? true).map((item) => ({ id: item.id, name: item.name || item.nome })));
        setOccupations(pageItems(occupationData).filter((item) => item.active ?? item.status ?? true).map((item) => ({ id: item.id, name: occupationName(item), sectorId: item.sectorId?.id || item.sectorId || item.sector?.id || "" })));
        setEmployees(pageItems(employeeData).filter((item) => item.status ?? item.active ?? true).map((item) => ({ id: item.id, name: employeeName(item), email: item.email || "" })));
        setPermissions(permissionData || []);
        const firstTool = toolItems.filter((tool) => !ADMIN_ONLY.has(tool.id)).sort((a, b) => a.name.localeCompare(b.name, "pt-BR"))[0];
        setPolicyTool(firstTool?.id || "");
        setForm({ ...emptyForm, tool: firstTool?.id || "" });
      })
      .catch((error) => { if (mounted) setMessage({ type: "error", text: error.message || "Nao foi possivel carregar as permissoes." }); })
      .finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, [canConfigure]);

  useEffect(() => {
    setPolicyMode(selectedPolicyTool?.restricted ? "restrito" : "padrao");
  }, [selectedPolicyTool]);

  function changeForm(key, value) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function changeTool(value) {
    setForm((current) => ({ ...current, tool: value, dataScope: "ALL_SECTORS", visibleSectorIds: [] }));
  }

  function changeEmployee(value) {
    setForm((current) => ({ ...current, employee: value, sector: "", occupation: "" }));
  }

  function changeVisibleSectors(event) {
    changeForm("visibleSectorIds", Array.from(event.target.selectedOptions, (option) => option.value));
  }

  async function savePolicy(event) {
    event.preventDefault();
    if (!selectedPolicyTool) return;
    setBusy(true);
    try {
      const saved = await api.updateTool(selectedPolicyTool.id, { enabled: selectedPolicyTool.enabled, restricted: policyMode === "restrito" });
      setTools((current) => current.map((tool) => tool.id === saved.id ? saved : tool));
      setMessage({ type: "success", text: "Política de acesso atualizada." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel atualizar a política." });
    } finally {
      setBusy(false);
    }
  }

  async function addPermission(event) {
    event.preventDefault();
    if (!form.tool) return;
    setBusy(true);
    try {
      const saved = await api.createToolPermission({
        toolSlug: form.tool,
        sectorId: form.employee ? null : form.sector || null,
        occupationId: form.employee ? null : form.occupation || null,
        employeeId: form.employee || null,
        level: form.level,
        enabled: true,
        dataScope: isReports ? form.dataScope : "ALL_SECTORS",
        visibleSectorIds: isReports && form.dataScope === "SELECTED_SECTORS" ? form.visibleSectorIds : [],
      });
      setPermissions((current) => [saved, ...current]);
      setPermissionPage(0);
      setForm((current) => ({ ...emptyForm, tool: current.tool }));
      setMessage({ type: "success", text: "Permissão salva." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel salvar a permissão." });
    } finally {
      setBusy(false);
    }
  }

  async function removePermission(id) {
    setBusy(true);
    try {
      await api.deleteToolPermission(id);
      setPermissions((current) => current.filter((permission) => permission.id !== id));
      setPermissionPage(0);
      setMessage({ type: "success", text: "Permissão removida." });
    } catch (error) {
      setMessage({ type: "error", text: error.message || "Nao foi possivel remover a permissão." });
    } finally {
      setBusy(false);
    }
  }

  if (!canConfigure) {
    return <DashboardLayout styles={["/css/ferramentas.css", "/css/management.css"]}><main className="dashboard"><div className="container"><AccessDenied /></div></main></DashboardLayout>;
  }

  return <DashboardLayout styles={["/css/ferramentas.css", "/css/management.css"]}>
    <main className="dashboard"><div className="container">
      <PageHeader eyebrow={cityHallName()} title="Controle de Acesso" action={<Link to="/ferramentas" className="btn btn-primary"><i className="bi bi-grid-1x2-fill"></i> Ferramentas</Link>} />
      {message && <div className={`auth-message ${message.type} mb-3`}><i className="bi bi-info-circle-fill"></i> {message.text}</div>}

      <div className="ferr-permissoes-wrap">
        <div className="ferr-permissoes-header d-flex align-items-center justify-content-between"><div><p className="eyebrow dark mb-0">Permissões</p><h5 className="ferr-access-title mb-0">Acesso por setor, cargo e usuário</h5></div><span className="badge text-bg-light">{permissions.length} cadastrada{permissions.length === 1 ? "" : "s"}</span></div>
        <div className="ferr-permissoes-body">
          <form className="ferr-access-policy-form" onSubmit={savePolicy}>
            <div><label className="ferr-access-label" htmlFor="policy-tool">Ferramenta</label><select className="form-select" id="policy-tool" value={policyTool} onChange={(event) => setPolicyTool(event.target.value)} disabled={loading}><option value="">---------</option>{availableTools.map((tool) => <option value={tool.id} key={tool.id}>{tool.name}</option>)}</select></div>
            <div><label className="ferr-access-label" htmlFor="policy-mode">Política de acesso</label><select className="form-select" id="policy-mode" value={policyMode} onChange={(event) => setPolicyMode(event.target.value)}><option value="padrao">Padrão do sistema</option><option value="restrito">Somente regras cadastradas</option></select></div>
            <button type="submit" className="btn btn-primary" disabled={busy || !selectedPolicyTool}>Salvar política</button>
            <small className="text-muted">No modo restrito, usuários sem uma regra aplicável não acessam a ferramenta.</small>
          </form>

          <form className="row g-2 align-items-end" onSubmit={addPermission}>
            <div className="col-12 col-md-4"><label className="ferr-access-label" htmlFor="permission-tool">Ferramenta</label><select className="form-select" id="permission-tool" required value={form.tool} onChange={(event) => changeTool(event.target.value)}><option value="">---------</option>{availableTools.map((tool) => <option value={tool.id} key={tool.id}>{tool.name}</option>)}</select></div>
            <div className="col-12 col-md-5"><label className="ferr-access-label" htmlFor="permission-sector">Setor</label><select className="form-select" id="permission-sector" value={form.sector} disabled={Boolean(form.employee)} onChange={(event) => changeForm("sector", event.target.value)}><option value="">---------</option>{sectors.map((sector) => <option value={sector.id} key={sector.id}>{sector.name}</option>)}</select></div>
            <div className="col-12 col-md-5"><label className="ferr-access-label" htmlFor="permission-occupation">Cargo</label><select className="form-select" id="permission-occupation" value={form.occupation} disabled={Boolean(form.employee)} onChange={(event) => changeForm("occupation", event.target.value)}><option value="">---------</option>{occupations.map((job) => <option value={job.id} key={job.id}>{job.name}</option>)}</select></div>
            <div className="col-12 col-md-5"><label className="ferr-access-label" htmlFor="permission-employee">Usuário específico</label><select className="form-select" id="permission-employee" value={form.employee} onChange={(event) => changeEmployee(event.target.value)}><option value="">---------</option>{employees.map((employee) => <option value={employee.id} key={employee.id}>{employee.name}</option>)}</select></div>
            <div className="col-12 col-md-3"><label className="ferr-access-label" htmlFor="permission-level">Nível</label><select className="form-select" id="permission-level" value={form.level} onChange={(event) => changeForm("level", event.target.value)}><option value="VIEW">Visualizar</option><option value="MANAGE">Gerenciar</option><option value="ADMIN">Admin</option></select></div>
            <div className="col-12 col-md-4"><label className="ferr-access-label" htmlFor="permission-scope">Escopo da Gestão</label><select className="form-select" id="permission-scope" value={form.dataScope} onChange={(event) => changeForm("dataScope", event.target.value)}><option value="ALL_SECTORS">Todos os setores</option><option value="SELECTED_SECTORS">Somente setores selecionados</option></select><small className="text-muted">Aplicado somente à ferramenta Relatórios.</small></div>
            <div className="col-12 col-md-8"><label className="ferr-access-label" htmlFor="permission-visible-sectors">Setores permitidos na Gestão</label><select className="form-select" id="permission-visible-sectors" multiple size="5" value={form.visibleSectorIds} disabled={!isReports || form.dataScope !== "SELECTED_SECTORS"} onChange={changeVisibleSectors}>{sectors.map((sector) => <option value={sector.id} key={sector.id}>{sector.name}</option>)}</select><small className="text-muted">Use Ctrl/Cmd para selecionar mais de um setor.</small></div>
            <div className="col-12 col-md-auto"><button type="submit" className="btn btn-primary w-100" disabled={busy}><span className="d-flex align-items-center justify-content-center"><i className="bi bi-plus-lg me-1"></i>Adicionar</span></button></div>
          </form>
        </div>

        <div className="table-responsive"><table className="table align-middle mb-0"><thead><tr className="ferr-access-table-head"><th>Ferramenta</th><th>Política</th><th>Setor</th><th>Cargo</th><th>Usuário</th><th>Nível</th><th>Escopo da Gestão</th><th></th></tr></thead><tbody>
          {loading ? <tr><td colSpan="8"><EmptyState icon="bi-arrow-repeat">Carregando permissões...</EmptyState></td></tr> : visiblePermissions.map((permission) => <tr key={permission.id}><td>{availableTools.find((tool) => tool.id === permission.toolSlug)?.name || permission.toolSlug}</td><td><span className={`badge ${permission.accessRestricted ? "text-bg-warning" : "text-bg-light"}`}>{permission.accessRestricted ? "Restrita" : "Padrão"}</span></td><td>{permission.sectorName || "Todos"}</td><td>{permission.occupationName || "Todos"}</td><td>{permission.employeeName ? <>{permission.employeeName}<small className="d-block text-muted">{permission.employeeEmail}</small></> : "Todos"}</td><td><span className="badge text-bg-light">{levelLabel(permission.level)}</span></td><td>{permission.toolSlug === "relatorios" ? <><strong>{permission.dataScope === "SELECTED_SECTORS" ? "Somente setores selecionados" : "Todos os setores"}</strong>{permission.dataScope === "SELECTED_SECTORS" && <small className="d-block text-muted">{permission.visibleSectorNames?.length ? permission.visibleSectorNames.join(", ") : "Nenhum setor"}</small>}</> : <span className="text-muted">Não se aplica</span>}</td><td className="text-end pe-3"><IconButton icon="bi-trash" title="Remover" danger onClick={() => removePermission(permission.id)} /></td></tr>)}
          {!loading && permissions.length === 0 && <tr><td colSpan="8" className="ferr-access-empty text-center text-muted py-4">Nenhuma permissão específica cadastrada.</td></tr>}
        </tbody></table></div>
        {permissionPages > 1 && <nav className="pagination-shell ferr-access-pagination" aria-label="Paginação"><div className="pagination-actions"><button className="pagination-btn" type="button" disabled={currentPermissionPage === 0} onClick={() => setPermissionPage(currentPermissionPage - 1)} aria-label="Página anterior"><i className="bi bi-chevron-left"></i></button><span className="pagination-current">{currentPermissionPage + 1}/{permissionPages}</span><button className="pagination-btn" type="button" disabled={currentPermissionPage === permissionPages - 1} onClick={() => setPermissionPage(currentPermissionPage + 1)} aria-label="Próxima página"><i className="bi bi-chevron-right"></i></button></div></nav>}
      </div>
    </div></main>
  </DashboardLayout>;
}

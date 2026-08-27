import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { useRouter } from "../components/RouterContext.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";
import { canManageCityTools, useCityHallName } from "../services/mockupService.js";

const CATEGORY_ICONS = [
  ["bi-folder-fill", "Pasta"], ["bi-grid-fill", "Conjunto de ferramentas"],
  ["bi-building-gear", "Administração"], ["bi-people-fill", "Pessoas"],
  ["bi-heart-pulse-fill", "Saúde"], ["bi-diagram-3-fill", "Processos"],
  ["bi-database-fill-gear", "Dados"], ["bi-shield-lock-fill", "Segurança"],
];
const ADMIN_ONLY = new Set(["setores", "cargos", "controle-acesso"]);
const EMPTY_CATEGORY = { id: "uncategorized", name: "Ferramentas", icon: "bi-grid-fill", description: "", order: 0, active: true };

function pageItems(response) {
  return Array.isArray(response) ? response : response?.content || [];
}

function categoryPayload(category) {
  return { name: category.name, description: category.description || "", icon: category.icon || "bi-folder-fill", order: Number(category.order) || 0, active: Boolean(category.active) };
}

function PermissionModal({ tool, permissions, sectors, occupations, employees, onClose, onPolicy, onCreate, onDelete }) {
  const assignable = !ADMIN_ONLY.has(tool.id);
  const [restricted, setRestricted] = useState(tool.restricted);
  const [form, setForm] = useState({ sectorId: "", occupationId: "", employeeId: "", level: "VIEW" });
  const rules = permissions.filter((permission) => permission.toolSlug === tool.id);

  function submit(event) {
    event.preventDefault();
    onCreate(tool.id, {
      sectorId: form.employeeId ? null : form.sectorId || null,
      occupationId: form.employeeId ? null : form.occupationId || null,
      employeeId: form.employeeId || null,
      level: form.level,
      enabled: true,
    }).then(() => setForm({ sectorId: "", occupationId: "", employeeId: "", level: "VIEW" }));
  }

  return <div className="react-modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}>
    <div className="react-modal-card ferr-permission-modal" role="dialog" aria-modal="true" aria-label={`Controle de acesso de ${tool.name}`}>
      <div className="modal-header">
        <div><p className="eyebrow dark mb-1">Controle de acesso</p><h5 className="modal-title">{tool.name}</h5></div>
        <button type="button" className="btn-close" aria-label="Fechar" onClick={onClose}></button>
      </div>
      <div className="modal-body">
        {assignable ? <>
          <section className="ferr-permission-section">
            <div><h6>Política da ferramenta</h6><p className="text-muted">No modo restrito, somente as regras cadastradas abaixo concedem acesso.</p></div>
            <form className="ferr-permission-policy-row" onSubmit={(event) => { event.preventDefault(); onPolicy(tool, restricted); }}>
              <label className="ferr-access-label" htmlFor={`policy-${tool.id}`}>Modo de acesso</label>
              <select id={`policy-${tool.id}`} className="form-select" value={restricted ? "restricted" : "default"} onChange={(event) => setRestricted(event.target.value === "restricted")}>
                <option value="default">Padrão do sistema</option><option value="restricted">Somente regras cadastradas</option>
              </select>
              <button className="btn btn-primary" type="submit">Salvar política</button>
            </form>
          </section>
          <section className="ferr-permission-section">
            <div><h6>Adicionar regra de acesso</h6><p className="text-muted">Use setor e cargo para grupos ou selecione somente um usuário para uma regra individual.</p></div>
            <form className="ferr-permission-grid" onSubmit={submit}>
              <div><label className="ferr-access-label">Setor</label><select className="form-select" value={form.sectorId} disabled={Boolean(form.employeeId)} onChange={(event) => setForm({ ...form, sectorId: event.target.value })}><option value="">Todos os setores</option>{sectors.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select></div>
              <div><label className="ferr-access-label">Cargo</label><select className="form-select" value={form.occupationId} disabled={Boolean(form.employeeId)} onChange={(event) => setForm({ ...form, occupationId: event.target.value })}><option value="">Todos os cargos</option>{occupations.map((item) => <option value={item.id} key={item.id}>{item.sectorName ? `${item.sectorName} · ` : ""}{item.name}</option>)}</select></div>
              <div><label className="ferr-access-label">Usuário específico</label><select className="form-select" value={form.employeeId} onChange={(event) => setForm({ ...form, employeeId: event.target.value, sectorId: "", occupationId: "" })}><option value="">Nenhum usuário específico</option>{employees.map((item) => <option value={item.id} key={item.id}>{item.name} · {item.email}</option>)}</select></div>
              <div><label className="ferr-access-label">Nível</label><select className="form-select" value={form.level} onChange={(event) => setForm({ ...form, level: event.target.value })}><option value="VIEW">Visualizar</option><option value="MANAGE">Gerenciar</option><option value="ADMIN">Admin</option></select></div>
              <button className="btn btn-primary ferr-permission-submit" type="submit"><i className="bi bi-plus-lg"></i> Adicionar regra</button>
            </form>
          </section>
          <section className="ferr-permission-section">
            <div className="ferr-permission-list-header"><div><h6>Regras cadastradas</h6><p className="text-muted">Regras individuais têm precedência sobre cargo, setor e regra geral.</p></div><span className="badge text-bg-light">{rules.length}</span></div>
            <div className="ferr-permission-list">{rules.length ? rules.map((rule) => <article className="ferr-permission-rule" key={rule.id}><div><strong>{rule.employeeName || [rule.sectorName, rule.occupationName].filter(Boolean).join(" · ") || "Todos os funcionários"}</strong><small className="text-muted">{{ VIEW: "Visualizar", MANAGE: "Gerenciar", ADMIN: "Admin" }[rule.level]}{rule.employeeName ? ` · ${employees.find((item) => item.id === rule.employeeId)?.email || ""}` : ""}</small></div><button type="button" className="ferr-icon-button danger" title="Remover regra" aria-label="Remover regra" onClick={() => onDelete(rule.id)}><i className="bi bi-trash3"></i></button></article>) : <div className="ferr-permission-empty">Nenhuma regra cadastrada para esta ferramenta.</div>}</div>
          </section>
        </> : <div className="ferr-permission-fixed"><i className="bi bi-shield-lock-fill"></i><div><h6>Acesso administrativo fixo</h6><p className="text-muted mb-0">Esta ferramenta altera a estrutura ou as permissões da prefeitura e permanece exclusiva dos administradores municipais.</p></div></div>}
      </div>
    </div>
  </div>;
}

export default function ToolsPage() {
  const { navigate } = useRouter();
  const storedCityHallName = useCityHallName();
  const [cityHallName, setCityHallName] = useState(storedCityHallName);
  const canConfigure = canManageCityTools();
  const [tools, setTools] = useState([]);
  const [categories, setCategories] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [sectors, setSectors] = useState([]);
  const [occupations, setOccupations] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [search, setSearch] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [selectedTool, setSelectedTool] = useState("");
  const [managerOpen, setManagerOpen] = useState(false);
  const [permissionTool, setPermissionTool] = useState(null);
  const [message, setMessage] = useState(null);
  const [newCategory, setNewCategory] = useState({ name: "", description: "", icon: "bi-folder-fill", order: 0, active: true });

  async function load() {
    const [toolData, categoryData, dashboardData] = await Promise.all([api.getTools(), canConfigure ? api.getToolCategories() : Promise.resolve([]), api.getDashboard()]);
    setTools(toolData || []); setCategories(categoryData || []);
    if (dashboardData?.cityHallName) setCityHallName(dashboardData.cityHallName);
    if (canConfigure) {
      const [permissionData, sectorData, occupationData, employeeData] = await Promise.all([api.getToolPermissions(), api.getSectors(), api.getOccupations(), api.getEmployees()]);
      setPermissions(permissionData || []);
      setSectors(pageItems(sectorData).map((item) => ({ id: item.id, name: item.name || item.nome })));
      setOccupations(pageItems(occupationData).map((item) => ({ id: item.id, name: item.name || item.nome, sectorName: item.sectorName || item.sector?.name || item.sectorId?.name || "" })));
      setEmployees(pageItems(employeeData).map((item) => ({ id: item.id, name: item.fullName || [item.firstName, item.lastName].filter(Boolean).join(" ") || item.name, email: item.email || "" })));
    }
  }

  useEffect(() => { load().catch((error) => setMessage({ type: "error", text: error.message })); }, []);

  const activeCategories = useMemo(() => categories.filter((category) => category.active), [categories]);
  const groups = useMemo(() => {
    const base = [EMPTY_CATEGORY, ...activeCategories].map((category) => ({ ...category, tools: [] }));
    const byId = new Map(base.map((group) => [group.id, group]));
    tools.forEach((tool) => (byId.get(tool.categoryId) || byId.get("uncategorized")).tools.push(tool));
    return base.filter((group) => canConfigure || group.tools.length > 0).sort((a, b) => a.order - b.order || a.name.localeCompare(b.name));
  }, [tools, activeCategories, canConfigure]);
  const query = search.trim().toLocaleLowerCase("pt-BR");
  const visibleGroups = groups.filter((group) => selectedCategory === "all" || group.id === selectedCategory).map((group) => ({ ...group, tools: group.tools.filter((tool) => !query || tool.name.toLocaleLowerCase("pt-BR").includes(query)) })).filter((group) => !query || group.tools.length);

  function success(text) { setMessage({ type: "success", text }); }
  function failure(error) { setMessage({ type: "error", text: error.message || "Não foi possível concluir a operação." }); }

  async function toggleFavorite(tool) { try { const result = await api.toggleToolFavorite(tool.id); setTools((items) => items.map((item) => item.id === tool.id ? { ...item, favorite: result.favorite } : item)); } catch (error) { failure(error); } }
  async function toggleTool(tool) { try { const saved = await api.updateTool(tool.id, { enabled: !tool.enabled, restricted: tool.restricted }); setTools((items) => items.map((item) => item.id === tool.id ? saved : item)); success(`${tool.name} atualizada para esta prefeitura.`); } catch (error) { failure(error); } }
  async function moveTool(tool, categoryId) { try { const saved = await api.updateToolCategory(tool.id, categoryId || null); setTools((items) => items.map((item) => item.id === tool.id ? saved : item)); success(`Pasta de ${tool.name} atualizada.`); } catch (error) { failure(error); } }
  async function createCategory(event) { event.preventDefault(); try { await api.createToolCategory(categoryPayload(newCategory)); setNewCategory({ name: "", description: "", icon: "bi-folder-fill", order: 0, active: true }); await load(); success("Pasta criada."); } catch (error) { failure(error); } }
  async function saveCategory(category) { try { const saved = await api.updateToolCategoryFolder(category.id, categoryPayload(category)); setCategories((items) => items.map((item) => item.id === saved.id ? saved : item)); success("Pasta atualizada."); } catch (error) { failure(error); } }
  async function deleteCategory(category) { if (!window.confirm("Remover esta pasta? As ferramentas ficarão sem pasta.")) return; try { await api.deleteToolCategory(category.id); setCategories((items) => items.filter((item) => item.id !== category.id)); setTools((items) => items.map((item) => item.categoryId === category.id ? { ...item, categoryId: null } : item)); if (selectedCategory === category.id) setSelectedCategory("all"); success("Pasta removida. As ferramentas voltaram para sem pasta."); } catch (error) { failure(error); } }
  async function savePolicy(tool, restricted) { try { const saved = await api.updateTool(tool.id, { enabled: tool.enabled, restricted }); setTools((items) => items.map((item) => item.id === tool.id ? saved : item)); setPermissionTool(saved); success(`Acesso de ${tool.name} definido como ${restricted ? "restrito" : "padrão"}.`); } catch (error) { failure(error); } }
  async function createPermission(toolSlug, payload) { try { const saved = await api.createToolPermission({ toolSlug, ...payload }); setPermissions((items) => [...items, saved]); success("Permissão salva."); } catch (error) { failure(error); throw error; } }
  async function deletePermission(id) { try { await api.deleteToolPermission(id); setPermissions((items) => items.filter((item) => item.id !== id)); success("Permissão removida."); } catch (error) { failure(error); } }

  function focusTool(tool) { setSelectedTool(tool.id); document.getElementById(`ferr-card-${tool.id}`)?.scrollIntoView({ behavior: "smooth", block: "center" }); }
  function openTool(tool) { if ((tool.enabled || tool.mandatory) && tool.route) navigate(tool.route); }

  return <DashboardLayout styles={["/css/ferramentas.css"]}>
    <main className="dashboard"><div className="container">
      <PageHeader eyebrow={cityHallName} title="Ferramentas" />
      {message && <div className={`auth-message ${message.type} mb-3`} role="status"><i className={`bi ${message.type === "success" ? "bi-check-circle-fill" : "bi-exclamation-circle-fill"}`}></i>{message.text}</div>}
      <div className="ferramentas-layout">
        <nav className="ferramentas-nav" aria-label="Ferramentas">
          <div className="ferramentas-nav-header"><div className="ferr-busca-wrap"><i className="bi bi-search"></i><input className="ferr-busca" placeholder="Buscar ferramenta..." value={search} onChange={(event) => setSearch(event.target.value)} /></div></div>
          <div className="ferr-nav-section"><p className="ferr-nav-label">Pastas</p><button type="button" className={`ferr-item ${selectedCategory === "all" ? "active" : ""}`} onClick={() => setSelectedCategory("all")}><div className="ferr-item-icon"><i className="bi bi-grid-fill"></i></div><span className="ferr-item-nome">Todas as ferramentas</span></button>
            {groups.map((group) => <button type="button" className={`ferr-category-link ${selectedCategory === group.id ? "active" : ""}`} onClick={() => setSelectedCategory(group.id)} key={group.id}><span className="ferr-category-link-icon"><i className={`bi ${group.icon}`}></i></span><span>{group.name}</span><strong>{group.tools.length}</strong></button>)}
          </div>
          <div className="ferr-nav-section ferr-nav-tools"><p className="ferr-nav-label">Ferramentas</p>{visibleGroups.flatMap((group) => group.tools).map((tool) => <button type="button" className={`ferr-item ferr-tool-shortcut ${selectedTool === tool.id ? "active" : ""}`} onClick={() => focusTool(tool)} key={tool.id}><div className="ferr-item-icon"><i className={`bi ${tool.icon}`}></i></div><span className="ferr-item-nome">{tool.name}</span>{tool.mandatory ? <span className="ferr-badge obrigatoria">Obrig.</span> : !tool.enabled ? <span className="ferr-badge em-breve">Inativo</span> : null}</button>)}</div>
        </nav>
        <section className="ferramentas-conteudo">
          <div className="ferr-painel-header"><div><p className="eyebrow dark mb-1">{selectedCategory === "all" ? "Todas as" : "Categoria"}</p><h4 className="mb-1">Ferramentas disponíveis</h4><p className="text-muted mb-0">{canConfigure ? "Acompanhe as ferramentas da prefeitura e ajuste quais ficam disponíveis para os usuários." : "Acesse os módulos liberados para o seu perfil nesta prefeitura."}</p></div>{canConfigure && <button type="button" className="ferr-manage-button" aria-expanded={managerOpen} onClick={() => setManagerOpen(!managerOpen)}><i className="bi bi-sliders"></i>Configurar ferramentas</button>}</div>
          {canConfigure && managerOpen && <div className="ferr-manager-panel"><div className="ferr-manager-header"><div><p className="eyebrow dark mb-1">Administração</p><h4 className="mb-0">Configuração das ferramentas</h4></div><p className="text-muted mb-0">Crie pastas quando precisar separar ferramentas por setor ou rotina. Sem pasta, elas continuam aparecendo juntas.</p></div>
            <form className="ferr-category-form" onSubmit={createCategory}><div className="ferr-field ferr-field-name"><label>Nome</label><input className="form-control" required placeholder="Ex.: Secretaria de Saúde" value={newCategory.name} onChange={(event) => setNewCategory({ ...newCategory, name: event.target.value })} /></div><div className="ferr-field"><label>Ícone</label><select className="form-select" value={newCategory.icon} onChange={(event) => setNewCategory({ ...newCategory, icon: event.target.value })}>{CATEGORY_ICONS.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></div><div className="ferr-field ferr-field-small"><label>Ordem</label><input className="form-control" type="number" min="0" value={newCategory.order} onChange={(event) => setNewCategory({ ...newCategory, order: event.target.value })} /></div><label className="ferr-check"><input className="form-check-input" type="checkbox" checked={newCategory.active} onChange={(event) => setNewCategory({ ...newCategory, active: event.target.checked })} /><span>Ativa</span></label><button type="submit" className="btn-primary ferr-category-submit"><i className="bi bi-folder-plus"></i>Criar pasta</button></form>
            <div className="ferr-folder-manager"><div className="ferr-folder-manager-title"><p className="eyebrow dark mb-0">Pastas criadas</p></div>{categories.length ? categories.map((category) => <div className="ferr-folder-row" key={category.id}><div className="ferr-folder-edit-form"><div className="ferr-field ferr-folder-name"><label>Nome</label><input className="form-control" value={category.name} onChange={(event) => setCategories((items) => items.map((item) => item.id === category.id ? { ...item, name: event.target.value } : item))} /></div><div className="ferr-field"><label>Ícone</label><select className="form-select" value={category.icon} onChange={(event) => setCategories((items) => items.map((item) => item.id === category.id ? { ...item, icon: event.target.value } : item))}>{CATEGORY_ICONS.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></div><div className="ferr-field ferr-field-small"><label>Ordem</label><input className="form-control" type="number" min="0" value={category.order} onChange={(event) => setCategories((items) => items.map((item) => item.id === category.id ? { ...item, order: event.target.value } : item))} /></div><label className="ferr-check ferr-folder-active"><input className="form-check-input" type="checkbox" checked={category.active} onChange={(event) => setCategories((items) => items.map((item) => item.id === category.id ? { ...item, active: event.target.checked } : item))} /><span>Ativa</span></label><button type="button" className="ferr-icon-button" title="Salvar pasta" onClick={() => saveCategory(category)}><i className="bi bi-check2"></i></button></div><button type="button" className="ferr-icon-button danger" title="Remover pasta" onClick={() => deleteCategory(category)}><i className="bi bi-trash3"></i></button></div>) : <div className="ferr-folder-empty"><i className="bi bi-folder2-open"></i>Nenhuma pasta criada para esta prefeitura.</div>}</div>
          </div>}
          <div className="ferr-grade-wrap">{visibleGroups.map((group) => <section className="ferr-category-section" key={group.id}><div className="ferr-category-section-header"><div className="ferr-category-title"><span className="ferr-category-title-icon"><i className={`bi ${group.icon}`}></i></span><div><p className="eyebrow dark mb-1">{group.name}</p><h4 className="mb-0">{group.tools.length} ferramenta{group.tools.length === 1 ? "" : "s"}</h4></div></div>{group.description && <p className="text-muted mb-0">{group.description}</p>}</div><div className={`ferr-grade ${canConfigure ? "" : "ferr-grade-user"}`}>{group.tools.map((tool) => {
            const disabled = !tool.enabled && !tool.mandatory; return <article className={`ferr-card ${canConfigure ? "ferr-card-admin" : "ferr-card-user"} ${disabled || (!canConfigure && !tool.route) ? "disabled" : ""} ${selectedTool === tool.id ? "ferr-card-selected" : ""}`} id={`ferr-card-${tool.id}`} key={tool.id} onClick={(event) => { if (!event.target.closest(".ferr-card-config, .ferr-card-favorite")) openTool(tool); }}>
              {canConfigure && <span className={`ferr-card-tag ${tool.mandatory ? "obrigatoria" : tool.enabled ? "ativo" : "inativo"}`}>{tool.mandatory ? "Obrigatório" : tool.enabled ? "Ativo" : "Desativado"}</span>}
              <div className="ferr-card-icon"><i className={`bi ${tool.icon}`}></i></div><div className="ferr-card-copy"><span className="ferr-card-nome">{tool.name}</span>{!canConfigure && <p className="ferr-card-description">{tool.description || "Acesse esta ferramenta da prefeitura."}</p>}</div>
              {tool.route && tool.enabled && <div className="ferr-card-favorite"><button type="button" className={`btn-acao-favorito ${tool.favorite ? "active" : ""}`} title={tool.favorite ? "Remover dos favoritos" : "Adicionar aos favoritos"} onClick={() => toggleFavorite(tool)}><i className={`bi ${tool.favorite ? "bi-star-fill" : "bi-star"}`}></i></button></div>}
              {canConfigure ? <div className="ferr-card-config"><div className="ferr-tool-category-form"><label>Pasta</label><select className="form-select" value={tool.categoryId || ""} onChange={(event) => moveTool(tool, event.target.value)}><option value="">Sem pasta</option>{categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}</select></div><div className="ferr-tool-toggle-form"><div className="ferr-toggle-control"><button type="button" className={`ferr-toggle-button ${tool.enabled ? "success" : "danger"}`} disabled={tool.mandatory} onClick={() => toggleTool(tool)}><i className={`bi ${tool.enabled ? "bi-toggle-on" : "bi-toggle-off"}`}></i>{tool.enabled ? "Ativo" : "Desativado"}</button></div></div><button type="button" className="ferr-permission-button" onClick={() => setPermissionTool(tool)}><i className="bi bi-people-fill"></i>Administrar acessos</button></div> : <span className="ferr-card-open">{tool.route ? <>Abrir ferramenta <i className="bi bi-arrow-right"></i></> : "Indisponível"}</span>}
            </article>;
          })}{!group.tools.length && <div className="ferr-empty-category"><i className="bi bi-folder2-open"></i>Nenhuma ferramenta nesta categoria.</div>}</div></section>)}{!visibleGroups.length && <div className="p-5 text-center text-muted w-100">Nenhuma ferramenta disponível.</div>}</div>
        </section>
      </div>
    </div></main>
    {permissionTool && <PermissionModal tool={permissionTool} permissions={permissions} sectors={sectors} occupations={occupations} employees={employees} onClose={() => setPermissionTool(null)} onPolicy={savePolicy} onCreate={createPermission} onDelete={deletePermission} />}
  </DashboardLayout>;
}

import { useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { useRouter } from "../components/RouterContext.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import {
  canManageCityTools,
  toolColors,
  useCityHallName,
  useToolsState,
} from "../services/mockupService.js";

function groupTools(tools) {
  return tools.reduce((groups, tool) => {
    const key = tool.category || "Geral";
    if (!groups[key]) groups[key] = [];
    groups[key].push(tool);
    return groups;
  }, {});
}

function toolStatus(tool) {
  if (tool.mandatory) return { label: "Obrigat\u00f3ria", className: "obrigatoria" };
  if (tool.enabled) return { label: "Ativo", className: "ativo" };
  return { label: "Inativo", className: "inativo" };
}

export default function ToolsPage() {
  const { navigate } = useRouter();
  const cityHallName = useCityHallName();
  const canConfigure = canManageCityTools();
  const { tools, toggleTool, toggleFavorite } = useToolsState();
  const [search, setSearch] = useState("");
  const [activeTool, setActiveTool] = useState("all");

  const visibleTools = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return tools;
    return tools.filter((tool) => tool.name.toLowerCase().includes(query));
  }, [tools, search]);

  const groupedTools = groupTools(visibleTools);

  function openTool(tool) {
    if (!tool.enabled && !tool.mandatory) return;
    setActiveTool(tool.id);
    if (tool.route) navigate(tool.route);
  }

  return (
    <DashboardLayout styles={["/css/ferramentas.css"]}>
      <main className="dashboard">
        <div className="container">
          <PageHeader eyebrow={cityHallName} title="Ferramentas" />

          <div className="ferramentas-layout">
            <nav className="ferramentas-nav" aria-label="Ferramentas">
              <div className="ferramentas-nav-header">
                <div className="ferr-busca-wrap">
                  <i className="bi bi-search"></i>
                  <input
                    className="ferr-busca"
                    placeholder="Buscar ferramenta..."
                    type="text"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                  />
                </div>
              </div>

              <button
                className={`ferr-item ${activeTool === "all" ? "active" : ""}`}
                type="button"
                onClick={() => setActiveTool("all")}
              >
                <div className="ferr-item-icon">
                  <i className="bi bi-grid-fill"></i>
                </div>
                <span className="ferr-item-nome">Todas as ferramentas</span>
              </button>

              {Object.entries(groupedTools).map(([category, categoryTools]) => (
                <div key={category}>
                  {!search && <div className="ferr-categoria-label">{category}</div>}
                  {categoryTools.map((tool) => (
                    <button
                      className={`ferr-item ${activeTool === tool.id ? "active" : ""}`}
                      type="button"
                      key={tool.id}
                      onClick={() => {
                        setActiveTool(tool.id);
                        document.getElementById(`ferr-card-${tool.id}`)?.scrollIntoView({
                          behavior: "smooth",
                          block: "center",
                        });
                      }}
                    >
                      <div className="ferr-item-icon">
                        <i className={`bi ${tool.icon}`}></i>
                      </div>
                      <span className="ferr-item-nome">{tool.name}</span>
                      {tool.mandatory ? (
                        <span className="ferr-badge obrigatoria">Obrig.</span>
                      ) : !tool.enabled ? (
                        <span className="ferr-badge em-breve">Inativo</span>
                      ) : null}
                    </button>
                  ))}
                </div>
              ))}
            </nav>

            <section className="ferramentas-conteudo">
              <div className="ferr-painel-header">
                <p className="eyebrow dark mb-1">Todas as</p>
                <h4 className="fw-bold mb-1">Ferramentas dispon&iacute;veis</h4>
                <p className="text-muted mb-0">
                  Selecione uma ferramenta para come&ccedil;ar. Novas funcionalidades s&atilde;o adicionadas continuamente.
                </p>
              </div>

              <div className="ferr-grade-wrap">
                <div className="ferr-grade" id="ferrGrade">
                  {visibleTools.map((tool, index) => {
                    const color = toolColors[index % toolColors.length];
                    const status = toolStatus(tool);
                    const disabled = !tool.enabled && !tool.mandatory;

                    return (
                      <article
                        className={`ferr-card ${disabled ? "disabled" : ""} ${
                          activeTool === tool.id ? "ferr-card-selected" : ""
                        }`}
                        id={`ferr-card-${tool.id}`}
                        key={tool.id}
                        tabIndex={disabled ? -1 : 0}
                        role="button"
                        onClick={() => openTool(tool)}
                        onKeyDown={(event) => {
                          if (event.key === "Enter") openTool(tool);
                        }}
                      >
                        <span className={`ferr-card-tag ${status.className}`}>{status.label}</span>
                        <button className="ferr-favorite" type="button" title={tool.favorite ? "Remover dos favoritos" : "Adicionar aos favoritos"} onClick={(event) => { event.stopPropagation(); toggleFavorite(tool.id); }}><i className={`bi ${tool.favorite ? "bi-star-fill" : "bi-star"}`}></i></button>

                        <div className="ferr-card-icon" style={{ background: color.bg, color: color.fg }}>
                          <i className={`bi ${tool.icon}`}></i>
                        </div>

                        <span className="ferr-card-nome">{tool.name}</span>

                        {canConfigure && (
                          <div className="ferr-card-toggle" onClick={(event) => event.stopPropagation()}>
                            <div className="form-check form-switch mb-0 d-flex justify-content-center">
                              <input
                                className="form-check-input"
                                type="checkbox"
                                role="switch"
                                checked={tool.enabled}
                                disabled={tool.mandatory}
                                title={tool.mandatory ? "Ferramenta obrigat\u00f3ria" : "Ativar ou desativar ferramenta"}
                                aria-label={`${tool.name}: ${tool.enabled ? "ativa" : "inativa"}`}
                                onChange={(event) => toggleTool(tool.id, event.target.checked)}
                              />
                            </div>
                          </div>
                        )}
                      </article>
                    );
                  })}

                  {visibleTools.length === 0 && (
                    <div className="p-5 text-center text-muted w-100">Nenhuma ferramenta encontrada.</div>
                  )}
                </div>
              </div>
            </section>
          </div>
        </div>
      </main>
    </DashboardLayout>
  );
}

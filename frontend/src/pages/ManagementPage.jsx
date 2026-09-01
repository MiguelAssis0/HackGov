import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";
import { useCityHallName } from "../services/mockupService.js";

const periods = [["semana", "Semana"], ["mes", "Mês"], ["ano", "Ano"], ["personalizado", "Período personalizado"]];

function number(value) { return Number(value || 0).toLocaleString("pt-BR", { maximumFractionDigits: 2 }); }
function date(value) { return value ? new Date(`${value}T00:00:00`).toLocaleDateString("pt-BR") : "-"; }
function moneyless(value) { return `${number(value)}%`; }

function LineChart({ values = [], label }) {
  const width = 800, height = 260, padding = { top: 20, right: 20, bottom: 45, left: 42 };
  const max = Math.max(1, ...values.map((item) => Number(item.value) || 0));
  const x = (index) => padding.left + (index * (width - padding.left - padding.right)) / Math.max(1, values.length - 1);
  const y = (value) => height - padding.bottom - (value * (height - padding.top - padding.bottom)) / max;
  const points = values.map((item, index) => `${x(index)},${y(item.value)}`).join(" ");
  const step = Math.max(1, Math.ceil(values.length / 7));
  return <svg className="gestao-line-chart" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={label}>
    <line x1={padding.left} y1={height - padding.bottom} x2={width - padding.right} y2={height - padding.bottom} className="chart-axis" />
    {values.length > 0 && <polyline points={points} className="chart-line" />}
    {values.map((item, index) => <g key={`${item.label}-${index}`}>
      <circle cx={x(index)} cy={y(item.value)} r="4" className="chart-point" />
      {(index % step === 0 || index === values.length - 1) && <text x={x(index)} y={height - 17} className="chart-label" textAnchor="middle">{item.label}</text>}
      {item.value > 0 && <text x={x(index)} y={y(item.value) - 10} className="chart-value" textAnchor="middle">{item.value}</text>}
    </g>)}
  </svg>;
}

function Sparkline({ values = [], name }) {
  const width = 120, height = 34, max = Math.max(1, ...values);
  const points = values.map((value, index) => `${2 + (index * (width - 4)) / Math.max(1, values.length - 1)},${height - 3 - (value * (height - 6)) / max}`).join(" ");
  return <svg className="gestao-sparkline" viewBox={`0 0 ${width} ${height}`} role="img" aria-label={`Evolução de ${name}`}>
    {values.length && values.some(Boolean) ? <polyline points={points} className="sparkline-line" /> : <line x1="2" y1={height / 2} x2={width - 2} y2={height / 2} className="sparkline-empty" />}
  </svg>;
}

function Kpi({ icon, label, value }) { return <article className="gestao-kpi-card"><i className={`bi ${icon}`}></i><span>{label}</span><strong>{number(value)}</strong></article>; }

function Comparison({ items }) {
  if (!items.length) return <div className="gestao-empty"><i className="bi bi-graph-up"></i><strong>Sem dados para comparação</strong><span>Funcionários com entregas aparecerão neste gráfico.</span></div>;
  const maxTasks = Math.max(1, ...items.map((item) => item.tasks));
  const maxPoints = Math.max(1, ...items.map((item) => item.points));
  return <><div className="gestao-comparison-legend" aria-label="Legenda"><span><i className="is-tasks"></i> Entregas</span><span><i className="is-points"></i> Pontos</span></div><div className="gestao-comparison-list">{items.map((item) => <article className="gestao-comparison-row" key={`${item.name}-${item.sector}`}><div className="gestao-comparison-person"><strong>{item.name}</strong><span>{item.sector}</span></div><div className="gestao-comparison-metrics"><div className="gestao-comparison-metric"><span className="gestao-comparison-label">Entregas <strong>{item.tasks}</strong></span><div className="gestao-comparison-track"><span className="gestao-comparison-bar is-tasks" style={{ width: `${(item.tasks / maxTasks) * 100}%` }}></span></div></div><div className="gestao-comparison-metric"><span className="gestao-comparison-label">Pontos <strong>{item.points}</strong></span><div className="gestao-comparison-track"><span className="gestao-comparison-bar is-points" style={{ width: `${(item.points / maxPoints) * 100}%` }}></span></div></div></div></article>)}</div><small className="gestao-chart-note">As barras usam escalas independentes: entregas são comparadas com o maior volume do período e pontos com a maior pontuação. A visualização compara perfis, sem tratar volume como eficiência.</small></>;
}

export default function ManagementPage() {
  const fallbackCityHall = useCityHallName();
  const [data, setData] = useState(null);
  const [form, setForm] = useState({ period: "mes", sectorId: "", start: "", end: "" });
  const [query, setQuery] = useState({ period: "mes" });
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let mounted = true;
    api.getManagement(query).then((response) => { if (mounted) { setData(response); setLoadError(""); } }).catch((error) => { if (mounted) setLoadError(error.message); });
    return () => { mounted = false; };
  }, [query]);

  const applyFilters = (event) => {
    event.preventDefault();
    setQuery({ period: form.period, sectorId: form.sectorId, start: form.start, end: form.end });
  };
  const setField = (field, value) => setForm((current) => ({ ...current, [field]: value }));
  const current = data || { cityHallName: fallbackCityHall, filtersValid: true, filterErrors: [], period: { label: "Mês", start: "", end: "" }, sectorOptions: [], indicators: {}, employees: [], sectors: [], temporalSeries: [], comparison: [], tasksWithoutResponsible: 0 };
  const selectedSector = current.sectorOptions.find((item) => String(item.id) === String(current.selectedSectorId));
  const topEmployees = current.employees.slice(0, 5);
  const selectedSectorLabel = selectedSector?.name || "Todos os setores";

  return <DashboardLayout styles={["/css/gestao.css"]}><main className="dashboard"><div className="container-fluid gestao-page">
    <header className="gestao-header"><div><p className="eyebrow dark mb-0">{current.cityHallName || fallbackCityHall}</p><h1>Página de Gestão</h1><span>Indicadores de entregas e pontos de valor público calculados sobre tarefas concluídas.</span></div><button className="btn btn-outline-primary2 gestao-print-button" type="button" onClick={() => window.print()}><i className="bi bi-printer"></i> Imprimir relatório</button></header>

    <section className="gestao-filter-panel" aria-labelledby="filtros-title"><div className="gestao-section-heading"><div><p className="eyebrow dark mb-0">Fonte única de dados</p><h2 id="filtros-title">Filtros globais</h2></div><span className="gestao-period-summary"><i className="bi bi-calendar3"></i> {date(current.period.start)} a {date(current.period.end)}</span></div><form className="gestao-filters" onSubmit={applyFilters}><div className="gestao-field"><label htmlFor="management-period">Período</label><select id="management-period" className="form-select" value={form.period} onChange={(event) => setField("period", event.target.value)}>{periods.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></div><div className="gestao-field"><label htmlFor="management-sector">Setor</label><select id="management-sector" className="form-select" value={form.sectorId} onChange={(event) => setField("sectorId", event.target.value)}><option value="">Todos os setores</option>{current.sectorOptions.map((sector) => <option value={sector.id} key={sector.id}>{sector.name}</option>)}</select></div><div className="gestao-field gestao-custom-date" hidden={form.period !== "personalizado"}><label htmlFor="management-start">Data inicial</label><input id="management-start" className="form-control" type="date" value={form.start} onChange={(event) => setField("start", event.target.value)} /></div><div className="gestao-field gestao-custom-date" hidden={form.period !== "personalizado"}><label htmlFor="management-end">Data final</label><input id="management-end" className="form-control" type="date" value={form.end} onChange={(event) => setField("end", event.target.value)} /></div><button className="btn btn-primary gestao-filter-submit" type="submit"><i className="bi bi-funnel-fill"></i> Aplicar filtros</button></form>{loadError && <div className="gestao-filter-alert" role="alert"><i className="bi bi-exclamation-triangle"></i> {loadError}</div>}{current.filterErrors?.length > 0 && <div className="gestao-filter-alert" role="alert"><i className="bi bi-exclamation-triangle"></i> Os filtros informados são inválidos. Os indicadores abaixo usam o mês atual até que os campos sejam corrigidos.<ul>{current.filterErrors.map((error) => <li key={error}>{error}</li>)}</ul></div>}{current.restrictedScope && <div className="gestao-scope-notice" role="status"><i className="bi bi-shield-lock"></i> Seu acesso está limitado aos setores autorizados. Todos os indicadores e comparativos respeitam esse escopo.</div>}</section>

    <section aria-labelledby="indicadores-title"><div className="gestao-section-heading"><div><p className="eyebrow dark mb-0">Visão consolidada</p><h2 id="indicadores-title">Indicadores gerais</h2></div>{current.selectedSectorId && <span className="gestao-filter-chip"><i className="bi bi-building"></i> {selectedSectorLabel}</span>}</div><div className="gestao-kpi-grid"><Kpi icon="bi-check2-circle" label="Tarefas concluídas" value={current.indicators.totalTasks} /><Kpi icon="bi-gem" label="Pontuação total" value={current.indicators.totalPoints} /><Kpi icon="bi-person-check" label="Funcionários com entregas" value={current.indicators.employeesWithDeliveries} /><Kpi icon="bi-bar-chart" label="Média de tarefas por funcionário" value={current.indicators.averageTasksPerEmployee} /><Kpi icon="bi-stars" label="Média de pontos por funcionário" value={current.indicators.averagePointsPerEmployee} /><Kpi icon="bi-calculator" label="Pontuação média por tarefa" value={current.indicators.averagePointsPerTask} /></div></section>

    <div className="gestao-chart-grid"><section className="gestao-panel" aria-labelledby="evolucao-title"><div className="gestao-section-heading"><div><p className="eyebrow dark mb-0">Linha do tempo</p><h2 id="evolucao-title">Evolução das entregas</h2></div></div>{current.indicators.totalTasks ? <div className="gestao-chart-wrap"><LineChart values={current.temporalSeries} label="Evolução das tarefas concluídas" /></div> : <div className="gestao-empty"><i className="bi bi-clipboard-x"></i><strong>Nenhuma entrega no período</strong><span>Altere os filtros para consultar outro intervalo ou setor.</span></div>}</section><section className="gestao-panel" aria-labelledby="relacao-title"><div className="gestao-section-heading"><div><p className="eyebrow dark mb-0">Comparação</p><h2 id="relacao-title">Tarefas × pontuação</h2></div></div><Comparison items={current.comparison} /></section></div>

    <section className="gestao-panel" aria-labelledby="funcionarios-title"><div className="gestao-section-heading"><div><p className="eyebrow dark mb-0">Ranking comparativo</p><h2 id="funcionarios-title">Desempenho individual</h2></div><span>{current.employees.length} funcionários considerados</span></div><div className="gestao-table-wrap"><table className="gestao-table"><thead><tr><th>Posição</th><th>Funcionário</th><th>Entregas</th><th>Pontos</th><th>Média por tarefa</th><th>Participação</th><th>Evolução</th></tr></thead><tbody>{current.employees.length ? current.employees.map((employee) => <tr key={employee.id}><td><span className="gestao-rank">{employee.position}</span></td><td><strong>{employee.name}</strong><small>{employee.sector}</small></td><td>{employee.tasks}</td><td>{employee.points}</td><td>{number(employee.averagePoints)}</td><td>{moneyless(employee.participation)}</td><td><Sparkline values={employee.evolution} name={employee.name} /></td></tr>) : <tr><td colSpan="7"><div className="gestao-empty compact"><strong>Nenhum funcionário disponível.</strong></div></td></tr>}</tbody></table></div><small className="gestao-chart-note">Em tarefas colaborativas, a entrega e seus pontos aparecem para cada responsável. O total geral continua contando cada tarefa apenas uma vez; por isso participações individuais podem somar mais de 100%.</small></section>

    <section className="gestao-panel" aria-labelledby="setores-title"><div className="gestao-section-heading"><div><p className="eyebrow dark mb-0">Estrutura municipal</p><h2 id="setores-title">Desempenho por setor</h2></div></div><div className="gestao-sector-grid">{current.sectors.length ? current.sectors.map((sector) => <article className="gestao-sector-card" key={sector.id}><div className="gestao-sector-title"><h3>{sector.name}</h3><strong>{moneyless(sector.participation)}</strong></div><div className="gestao-progress"><span style={{ width: `${Math.min(100, Math.max(0, sector.participation))}%` }}></span></div><dl><div><dt>Entregas</dt><dd>{sector.tasks}</dd></div><div><dt>Pontos</dt><dd>{sector.points}</dd></div><div><dt>Tarefas / funcionário</dt><dd>{number(sector.averageTasks)}</dd></div><div><dt>Pontos / funcionário</dt><dd>{number(sector.averagePoints)}</dd></div></dl></article>) : <div className="gestao-empty"><strong>Nenhum setor cadastrado.</strong></div>}</div></section>

    <section className="gestao-report" id="relatorio-gestao" aria-labelledby="relatorio-title"><div className="gestao-report-header"><div><p className="eyebrow dark mb-0">Relatório dinâmico</p><h2 id="relatorio-title">Relatório de desempenho</h2><span>{current.period.label} · {date(current.period.start)} a {date(current.period.end)} · {selectedSectorLabel}</span></div><i className="bi bi-file-earmark-bar-graph"></i></div><div className="gestao-report-summary"><p>No período analisado foram concluídas <strong>{number(current.indicators.totalTasks)}</strong> tarefas, totalizando <strong>{number(current.indicators.totalPoints)}</strong> pontos de valor público, com participação de <strong>{number(current.indicators.employeesWithDeliveries)}</strong> funcionários.</p>{current.tasksWithoutResponsible > 0 && <p className="gestao-report-note"><i className="bi bi-info-circle"></i> {current.tasksWithoutResponsible} tarefa(s) concluída(s) sem responsável associado não aparece(m) no desempenho individual.</p>}</div><div className="gestao-report-kpis"><div><span>Média de tarefas / funcionário</span><strong>{number(current.indicators.averageTasksPerEmployee)}</strong></div><div><span>Média de pontos / funcionário</span><strong>{number(current.indicators.averagePointsPerEmployee)}</strong></div><div><span>Pontuação média / tarefa</span><strong>{number(current.indicators.averagePointsPerTask)}</strong></div></div><div className="gestao-report-evolution"><h3>Evolução das entregas</h3>{current.indicators.totalTasks ? <LineChart values={current.temporalSeries} label="Evolução das entregas no relatório" /> : <p>Não houve entregas no período analisado.</p>}</div><div className="gestao-report-columns"><div><h3>Funcionários em destaque</h3>{topEmployees.length ? topEmployees.map((employee) => <div className="gestao-report-row" key={employee.id}><span>{employee.name}</span><strong>{employee.tasks} entregas · {employee.points} pts</strong></div>) : <p>Sem entregas individuais.</p>}</div><div><h3>Setores</h3>{current.sectors.length ? current.sectors.map((sector) => <div className="gestao-report-row" key={sector.id}><span>{sector.name}</span><strong>{sector.tasks} entregas · {sector.points} pts</strong></div>) : <p>Sem setores disponíveis.</p>}</div></div><p className="gestao-report-footnote">Este relatório utiliza exatamente a mesma seleção de tarefas concluídas dos indicadores, rankings e gráficos exibidos acima, considerando a data real de conclusão.</p></section>
  </div></main></DashboardLayout>;
}

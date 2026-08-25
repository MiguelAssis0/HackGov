import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { FieldLabel, PageHeader } from "../components/DashboardShared.jsx";
import { api, getStoredUser, getUserType } from "../services/api.js";
import {
  useCityHallName,
  slugify,
} from "../services/mockupService.js";

const monthLabels = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun"];
const weekLabels = ["Seg", "Ter", "Qua", "Qui", "Sex"];

function canViewAllSectors(userType) {
  return userType === "admin_equipe" || userType === "admin_cidade";
}

function userSectorKey(user) {
  return slugify(user?.setor || user?.sector || user?.sectorName || "");
}

function MetricCard({ icon, label, value, helper, tone = "blue" }) {
  return (
    <article className={`gestao-metric-card ${tone}`}>
      <div className="gestao-metric-icon">
        <i className={`bi ${icon}`}></i>
      </div>
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
        {helper && <small>{helper}</small>}
      </div>
    </article>
  );
}

function MonthlyChart({ values }) {
  const maxValue = Math.max(...values, 100);

  return (
    <div className="gestao-chart-bars" aria-label="Evolucao mensal de produtividade">
      {values.map((value, index) => (
        <div className="gestao-chart-column" key={`${monthLabels[index]}-${value}`}>
          <div className="gestao-bar-track">
            <div
              className="gestao-bar-fill"
              style={{ height: `${Math.max(12, (value / maxValue) * 100)}%` }}
              title={`${monthLabels[index]}: ${value}%`}
            ></div>
          </div>
          <span>{monthLabels[index]}</span>
        </div>
      ))}
    </div>
  );
}

function WeeklyChart({ values }) {
  const businessDayValues = values.slice(0, weekLabels.length);
  const maxValue = Math.max(...businessDayValues, 1);

  return (
    <div className="gestao-week-chart" aria-label="Demandas por dia da semana">
      {businessDayValues.map((value, index) => (
        <div className="gestao-week-row" key={`${weekLabels[index]}-${value}`}>
          <span>{weekLabels[index]}</span>
          <div className="gestao-week-track">
            <div style={{ width: `${Math.max(8, (value / maxValue) * 100)}%` }}></div>
          </div>
          <strong>{value}</strong>
        </div>
      ))}
    </div>
  );
}

function DemandDonut({ completed, active, overdue }) {
  const total = Math.max(completed + active + overdue, 1);
  const completedPercent = Math.round((completed / total) * 100);
  const activePercent = Math.round((active / total) * 100);
  const overduePercent = Math.max(0, 100 - completedPercent - activePercent);
  const activeEnd = completedPercent + activePercent;

  return (
    <div className="gestao-donut-wrap">
      <div
        className="gestao-donut"
        style={{
          background: `conic-gradient(#2e8bff 0 ${completedPercent}%, #f59e0b ${completedPercent}% ${activeEnd}%, #ff5f57 ${activeEnd}% 100%)`,
        }}
        aria-label={`${completedPercent}% conclu\u00eddas, ${activePercent}% em andamento, ${overduePercent}% atrasadas`}
      >
        <div>
          <strong>{completedPercent}%</strong>
          <span>conclu&iacute;das</span>
        </div>
      </div>

      <div className="gestao-donut-legend">
      <span><i className="blue"></i>Conclu&iacute;das {completed}</span>
        <span><i className="yellow"></i>Em andamento {active}</span>
        <span><i className="red"></i>Atrasadas {overdue}</span>
      </div>
    </div>
  );
}

export default function ManagementPage() {
  const cityHallName = useCityHallName();
  const user = getStoredUser() || {};
  const userType = getUserType(user);
  const showAllSectors = canViewAllSectors(userType);
  const [sectorPerformance, setSectorPerformance] = useState([]);
  const [loadError, setLoadError] = useState("");
  const [selectedSectorId, setSelectedSectorId] = useState("");

  useEffect(() => {
    let active = true;
    api.getSectorPerformance().then((response) => { if (active) { setSectorPerformance(response); setLoadError(""); } })
      .catch((error) => { if (active) setLoadError(error.message); });
    return () => { active = false; };
  }, []);

  const visibleSectors = useMemo(() => {
    if (showAllSectors) return sectorPerformance;

    const sectorKey = userSectorKey(user);
    const userSector = sectorPerformance.find((sector) => slugify(sector.name) === sectorKey || sector.slug === sectorKey);
    return userSector ? [userSector] : sectorPerformance.slice(0, 1);
  }, [sectorPerformance, showAllSectors, user]);

  useEffect(() => {
    if (!visibleSectors.length) return;
    if (!selectedSectorId || !visibleSectors.some((sector) => String(sector.id) === String(selectedSectorId))) {
      setSelectedSectorId(visibleSectors[0].id);
    }
  }, [visibleSectors, selectedSectorId]);

  const selectedSector =
    visibleSectors.find((sector) => String(sector.id) === String(selectedSectorId)) ||
    visibleSectors[0] ||
    null;

  const rankedSectors = useMemo(
    () => [...sectorPerformance].sort((a, b) => b.productivity - a.productivity).slice(0, 5),
    [sectorPerformance],
  );

  if (!selectedSector) {
    return (
      <DashboardLayout styles={["/css/management.css", "/css/gestao.css"]}>
        <main className="dashboard">
          <div className="container">
            <PageHeader eyebrow={cityHallName} title={"Gest\u00e3o"} />
            {loadError && <div className="auth-message danger mb-3">{loadError}</div>}
            <section className="panel gestao-empty">
              <i className="bi bi-graph-up-arrow"></i>
              <p>Nenhum setor dispon&iacute;vel para gerar indicadores.</p>
            </section>
          </div>
        </main>
      </DashboardLayout>
    );
  }

  const productivityDelta = selectedSector.trend >= 0 ? `+${selectedSector.trend}%` : `${selectedSector.trend}%`;

  return (
    <DashboardLayout styles={["/css/management.css", "/css/gestao.css"]}>
      <main className="dashboard gestao-page">
        <div className="container">
          <PageHeader
            eyebrow={cityHallName}
            title={"Gest\u00e3o"}
            action={
              showAllSectors ? (
                <div className="gestao-sector-select">
                  <FieldLabel htmlFor="sectorPerformance">Setor analisado</FieldLabel>
                  <select
                    id="sectorPerformance"
                    className="form-select"
                    value={selectedSector.id}
                    onChange={(event) => setSelectedSectorId(event.target.value)}
                  >
                    {visibleSectors.map((sector) => (
                      <option value={sector.id} key={sector.id}>
                        {sector.name}
                      </option>
                    ))}
                  </select>
                </div>
              ) : null
            }
          />

          <section className="gestao-hero-row">
            <article className="gestao-hero panel">
              <p className="eyebrow dark mb-2">Desempenho do setor</p>
              <h2>{selectedSector.name}</h2>
              <p>
                Acompanhe produtividade, prazos e volume de entregas para apoiar decis&otilde;es do gestor do setor.
              </p>
            </article>

            <article className="gestao-score-card">
              <span>Produtividade atual</span>
              <strong>{selectedSector.productivity}%</strong>
              <div className="gestao-progress">
                <div style={{ width: `${selectedSector.productivity}%` }}></div>
              </div>
              <small>{productivityDelta} nos &uacute;ltimos meses</small>
            </article>
          </section>

          <section className="gestao-metrics-grid" aria-label="Resumo de desempenho">
            <MetricCard
              icon="bi-check2-circle"
              label={"Demandas conclu\u00eddas"}
              value={selectedSector.completed}
              helper={`${selectedSector.completionRate}% do volume total`}
              tone="green"
            />
            <MetricCard
              icon="bi-play-circle-fill"
              label="Em andamento"
              value={selectedSector.activeTasks}
              helper="Demandas acompanhadas agora"
              tone="blue"
            />
            <MetricCard
              icon="bi-exclamation-circle-fill"
              label="Atrasadas"
              value={selectedSector.overdue}
              helper={"Pontos de aten\u00e7\u00e3o"}
              tone="red"
            />
            <MetricCard
              icon="bi-people-fill"
              label="Equipe"
              value={selectedSector.employees}
              helper="Funcionarios vinculados"
              tone="purple"
            />
          </section>

          <div className="gestao-grid">
            <section className="panel gestao-card gestao-main-chart">
              <div className="gestao-card-header">
                <div>
                  <p className="eyebrow dark mb-1">Produtividade</p>
                  <h3>Evolu&ccedil;&atilde;o mensal</h3>
                </div>
                <span className="gestao-goal">Meta {selectedSector.goal}%</span>
              </div>
              <MonthlyChart values={selectedSector.monthly} />
            </section>

            <section className="panel gestao-card">
              <div className="gestao-card-header">
                <div>
                  <p className="eyebrow dark mb-1">Demandas</p>
                  <h3>Distribui&ccedil;&atilde;o</h3>
                </div>
              </div>
              <DemandDonut
                completed={selectedSector.completed}
                active={selectedSector.activeTasks}
                overdue={selectedSector.overdue}
              />
            </section>

            <section className="panel gestao-card">
              <div className="gestao-card-header">
                <div>
                  <p className="eyebrow dark mb-1">Semana atual</p>
                  <h3>Entregas por dia</h3>
                </div>
              </div>
              <WeeklyChart values={selectedSector.weekly} />
            </section>

            <section className="panel gestao-card">
              <div className="gestao-card-header">
                <div>
                  <p className="eyebrow dark mb-1">Indicadores</p>
                  <h3>Sa&uacute;de operacional</h3>
                </div>
              </div>

              <div className="gestao-indicators">
                <div>
                  <span>Tempo medio de resposta</span>
                  <strong>{selectedSector.averageResponseHours.toFixed(1)}h</strong>
                </div>
                <div>
                  <span>Qualidade das entregas</span>
                  <strong>{selectedSector.quality}%</strong>
                </div>
                <div>
                  <span>Volume total</span>
                  <strong>{selectedSector.totalTasks}</strong>
                </div>
              </div>
            </section>

            {showAllSectors && (
              <section className="panel gestao-card gestao-ranking">
                <div className="gestao-card-header">
                  <div>
                    <p className="eyebrow dark mb-1">Comparativo</p>
                    <h3>Setores em destaque</h3>
                  </div>
                </div>

                <div className="gestao-ranking-list">
                  {rankedSectors.map((sector, index) => (
                    <button
                      type="button"
                      className={`gestao-ranking-item ${sector.id === selectedSector.id ? "active" : ""}`}
                      key={sector.id}
                      onClick={() => setSelectedSectorId(sector.id)}
                    >
                      <span>{index + 1}</span>
                      <div>
                        <strong>{sector.name}</strong>
                        <small>{sector.completed} demandas conclu&iacute;das</small>
                      </div>
                      <b>{sector.productivity}%</b>
                    </button>
                  ))}
                </div>
              </section>
            )}
          </div>
        </div>
      </main>
    </DashboardLayout>
  );
}

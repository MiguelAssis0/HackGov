import { useEffect, useMemo, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { api, getSelectedCityHall } from "../services/api.js";

const TARGETS = {
  employees: {
    label: "Servidores",
    fields: [
      ["nome", "Nome", true], ["email", "E-mail", true], ["cpf", "CPF"],
      ["numero_de_registro", "Numero de registro"], ["setor", "Setor"],
      ["cargo", "Cargo"], ["carga_horaria", "Carga horaria"],
      ["salario", "Salario"], ["admissao", "Admissao"],
    ],
  },
  departments: {
    label: "Setores",
    fields: [["nome", "Nome", true], ["slug", "Identificador"], ["descricao", "Descricao"], ["ativo", "Ativo"]],
  },
};
const TARGET_OPTIONS = Object.entries(TARGETS);
const STATUS_LABELS = {
  UPLOADED: "Enviado", VALIDATED: "Validado", VALIDATION_FAILED: "Validacao com falhas",
  IMPORTED: "Importado", IMPORT_FAILED: "Importado com falhas",
};

function number(value) {
  return Number(value || 0).toLocaleString("pt-BR", { maximumFractionDigits: 2 });
}
function dateTime(value) {
  return value ? new Date(value).toLocaleString("pt-BR") : "-";
}
function parseNumber(value) {
  const text = String(value || "").trim().replace(/\s/g, "").replace(/\.(?=\d{3}(?:\D|$))/g, "").replace(",", ".");
  return text && Number.isFinite(Number(text)) ? Number(text) : null;
}
function buildInsights(headers, rows) {
  const numericSummaries = [];
  const categoricalSummaries = [];
  headers.forEach((header, index) => {
    const values = rows.map((row) => String(row.values?.[index] || "").trim()).filter(Boolean);
    if (!values.length) return;
    const numbers = values.map(parseNumber);
    if (numbers.every((value) => value !== null)) {
      const sum = numbers.reduce((total, value) => total + value, 0);
      numericSummaries.push({ column: header, min: Math.min(...numbers), max: Math.max(...numbers), average: sum / numbers.length, sum });
      return;
    }
    const counts = Object.entries(values.reduce((result, value) => ({ ...result, [value]: (result[value] || 0) + 1 }), {}))
      .sort((a, b) => b[1] - a[1]).slice(0, 5);
    categoricalSummaries.push({ column: header, topValues: counts.map(([value, count]) => ({ value, count })) });
  });
  const signatures = rows.map((row) => (row.values || []).join("\u0001")).filter(Boolean);
  const duplicates = signatures.filter((signature, index) => signatures.indexOf(signature) !== index);
  return {
    totalRows: rows.length,
    emptyFieldsCount: rows.reduce((total, row) => total + (row.values || []).filter((value) => !String(value || "").trim()).length, 0),
    duplicateCount: new Set(duplicates).size + duplicates.length,
    numericSummaries,
    categoricalSummaries,
  };
}

function Header({ cityHallName, preview, historyOnly }) {
  const title = historyOnly ? "Historico de importacoes" : "Importacao de planilhas";
  return (
    <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
      <div>
        <p className="eyebrow dark mb-0">{cityHallName}</p>
        <h3 className="mb-0 fw-bold spreadsheet-title">{title}</h3>
      </div>
      {historyOnly && <a className="btn btn-primary" href="/importacao"><i className="bi bi-file-earmark-spreadsheet-fill"></i> Nova importacao</a>}
      {!historyOnly && preview && <button className="btn btn-outline-secondary" type="button" onClick={preview.clear}><i className="bi bi-x-circle"></i> Limpar previa</button>}
      {!historyOnly && !preview && <a className="btn btn-outline-secondary" href="/importacao/historico"><i className="bi bi-clock-history"></i> Historico</a>}
    </div>
  );
}
function PanelHeading({ icon, title, description }) {
  return (
    <div className="panel-heading">
      <div>
        <h4 className="mb-0"><i className={`bi ${icon} me-2 text-primary`}></i>{title}</h4>
        {description && <p className="spreadsheet-muted">{description}</p>}
      </div>
    </div>
  );
}
function Templates() {
  return (
    <>
      <section className="panel template-panel">
        <PanelHeading icon="bi-file-earmark-excel-fill" title="Modelos de importacao" description="Baixe um modelo com colunas corretas, campos obrigatorios e uma linha de exemplo." />
        <div className="template-grid">
          {TARGET_OPTIONS.map(([slug, target]) => <button className="template-card" type="button" key={slug} onClick={() => api.downloadImportTemplate(slug)}><i className="bi bi-download"></i><span>{target.label}</span></button>)}
        </div>
      </section>
      <section className="panel template-panel">
        <PanelHeading icon="bi-table" title="Exportar dados atuais" description="Baixe os dados existentes, edite no Excel e envie novamente para sincronizar alteracoes." />
        <div className="template-grid">
          {TARGET_OPTIONS.map(([slug, target]) => <button className="template-card" type="button" key={slug} onClick={() => api.downloadImportExport(slug)}><i className="bi bi-file-earmark-arrow-down"></i><span>{target.label}</span></button>)}
        </div>
      </section>
    </>
  );
}
function UploadPanel({ target, setTarget, file, setFile, busy, upload }) {
  return (
    <section className="panel spreadsheet-upload">
      <form className="upload-form-grid" onSubmit={upload}>
        <div className="upload-field"><label className="form-label">Modulo ou entidade</label><select className="form-select" value={target} onChange={(event) => setTarget(event.target.value)}>{TARGET_OPTIONS.map(([slug, item]) => <option value={slug} key={slug}>{item.label}</option>)}</select></div>
        <div className="upload-field"><label className="form-label">Planilha .xlsx ou .csv</label><input className="form-control" type="file" accept=".csv,.xlsx" required onChange={(event) => setFile(event.target.files?.[0] || null)} /><div className="form-text">Limite de 5 MB. A primeira aba do XLSX sera usada.</div></div>
        <div className="upload-action"><button className="btn btn-primary w-100" type="submit" disabled={busy}><i className="bi bi-cloud-arrow-up-fill"></i> {busy ? "Processando..." : "Carregar previa"}</button></div>
      </form>
    </section>
  );
}
function PreviewTable({ preview, errors = false }) {
  const rows = errors ? preview : preview.previewRows;
  const headers = errors ? ["Linha", "Campo", "Valor", "Erro"] : preview.headers;
  return (
    <div className="table-responsive spreadsheet-table-wrap">
      <table className="table table-sm align-middle spreadsheet-table">
        <thead><tr>{headers.map((header) => <th key={header}>{header}</th>)}</tr></thead>
        <tbody>
          {errors ? rows.map((error, index) => <tr key={`${error.rowNumber}-${index}`}><td>{error.rowNumber || "-"}</td><td>{error.field}</td><td>{error.value || "-"}</td><td>{error.message}</td></tr>) : rows.length ? rows.map((row) => <tr key={row.number}>{row.values.map((value, index) => <td key={`${row.number}-${index}`}>{value || "-"}</td>)}</tr>) : <tr><td colSpan={headers.length}>Nenhuma linha de dados encontrada apos o cabecalho.</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
function Insights({ insights }) {
  return (
    <section className="panel spreadsheet-insights">
      <PanelHeading icon="bi-bar-chart-fill" title="Insights da planilha" description="Resumo automatico calculado antes da importacao." />
      <div className="quality-summary insight-summary"><div><span>Total de linhas</span><strong>{insights.totalRows}</strong></div><div><span>Campos vazios</span><strong>{insights.emptyFieldsCount}</strong></div><div><span>Duplicadas</span><strong>{insights.duplicateCount}</strong></div></div>
      {insights.numericSummaries.length > 0 && <div className="insight-block"><strong>Colunas numericas</strong><div className="table-responsive"><table className="table table-sm align-middle spreadsheet-table"><thead><tr><th>Coluna</th><th>Minimo</th><th>Maximo</th><th>Media</th><th>Soma</th></tr></thead><tbody>{insights.numericSummaries.map((item) => <tr key={item.column}><td>{item.column}</td><td>{number(item.min)}</td><td>{number(item.max)}</td><td>{number(item.average)}</td><td>{number(item.sum)}</td></tr>)}</tbody></table></div></div>}
      {insights.categoricalSummaries.length > 0 && <div className="insight-block"><strong>Valores mais frequentes</strong><div className="insight-category-grid">{insights.categoricalSummaries.map((item) => <div className="insight-category" key={item.column}><span>{item.column}</span>{item.topValues.map((value) => <div key={value.value}>{value.value} <strong>{value.count}</strong></div>)}</div>)}</div></div>}
      {!insights.numericSummaries.length && !insights.categoricalSummaries.length && <div className="empty-state spreadsheet-empty compact">Nenhum padrao adicional encontrado nesta planilha.</div>}
    </section>
  );
}
function PreviewPage({ preview, targetInfo, mapping, setMapping, report, valid, busy, mode, setMode, confirmed, setConfirmed, importResult, validate, execute, insights, map }) {
  return (
    <>
      <section className="spreadsheet-summary"><div><span>Arquivo</span><strong>{preview.filename}</strong></div><div><span>Aba</span><strong>Dados</strong></div><div><span>Destino</span><strong>{targetInfo.label}</strong></div><div><span>Linhas lidas</span><strong>{preview.totalRows}</strong></div></section>
      <div className="d-flex justify-content-end mb-3"><a className="btn btn-outline-secondary" href="/importacao/historico"><i className="bi bi-clock-history"></i> Ver historico</a></div>
      <Insights insights={insights} />
      <section className="panel"><PanelHeading icon="bi-table" title="Previa dos dados" description="Cabecalhos detectados automaticamente e primeiras linhas da planilha." /><PreviewTable preview={preview} /></section>
      <section className="panel"><PanelHeading icon="bi-diagram-2-fill" title="Mapeamento de campos" description="As sugestoes usam nomes de colunas semelhantes. Esta fase nao salva registros." /><form onSubmit={validate}><div className="mapping-grid">{targetInfo.fields.map(([key, label, required]) => <div className="mapping-row" key={key}><label htmlFor={`mapping-${key}`}>{label}{required && <span>Obrigatorio</span>}</label><select id={`mapping-${key}`} className="form-select" value={mapping[key] || ""} onChange={(event) => setMapping({ ...mapping, [key]: event.target.value })}><option value="">Ignorar</option>{preview.headers.map((header) => <option value={header} key={header}>{header}</option>)}</select></div>)}</div><div className="d-flex justify-content-end gap-2 mt-3 flex-wrap"><button className="btn btn-outline-secondary" type="button" onClick={map}><i className="bi bi-check2-circle"></i> Validar mapeamento</button><button className="btn btn-primary" type="submit" disabled={busy}><i className="bi bi-shield-check"></i> {busy ? "Validando..." : "Validar planilha"}</button></div></form></section>
      {report && <section className="panel quality-report"><PanelHeading icon="bi-clipboard-data-fill" title="Relatorio de qualidade" description="Resultado da validacao da planilha inteira. Nenhum registro foi salvo." /><div className="quality-summary"><div><span>Total de linhas</span><strong>{report.totalRows}</strong></div><div><span>Linhas validas</span><strong>{report.validRows}</strong></div><div><span>Linhas invalidas</span><strong>{report.invalidRows}</strong></div><div><span>Duplicadas</span><strong>{report.duplicatedRows}</strong></div><div><span>Ignoradas</span><strong>{report.ignoredRows}</strong></div></div>{report.unmappedRequiredFields?.length > 0 && <div className="quality-list warning"><strong>Campos obrigatorios nao mapeados</strong><div>{report.unmappedRequiredFields.map((field) => <span key={field}>{field}</span>)}</div></div>}{report.unknownColumns?.length > 0 && <div className="quality-list"><strong>Colunas desconhecidas ou ignoradas</strong><div>{report.unknownColumns.map((column) => <span key={column}>{column}</span>)}</div></div>}<PreviewTable preview={report.errors || []} errors /></section>}
      {valid && <section className="panel sync-diff"><PanelHeading icon="bi-arrow-left-right" title="Diferencas detectadas" description="Confira as mudancas antes de confirmar a atualizacao." /><div className="table-responsive spreadsheet-table-wrap"><table className="table table-sm align-middle spreadsheet-table"><thead><tr><th>Linha</th><th>Acao</th><th>Registro</th><th>Campo</th><th>Valor atual</th><th>Novo valor</th></tr></thead><tbody><tr><td colSpan="6">Nenhuma diferenca encontrada para registros existentes.</td></tr></tbody></table></div></section>}
      <section className="panel import-confirmation"><PanelHeading icon="bi-database-fill-check" title="Salvar dados no sistema" description="A importacao fica disponivel depois que a planilha for validada sem erros." />{valid ? <form className="row g-3 align-items-end" onSubmit={execute}><div className="col-lg-5"><label className="form-label">Modo de importacao</label><select className="form-select" value={mode} onChange={(event) => setMode(event.target.value)}><option value="CREATE">Criar apenas</option><option value="UPDATE">Atualizar existentes</option><option value="UPSERT">Criar ou atualizar</option></select></div><div className="col-lg-4"><label className="form-check import-confirm-check"><input className="form-check-input" type="checkbox" checked={confirmed} onChange={(event) => setConfirmed(event.target.checked)} /><span className="form-check-label">Confirmo a importacao dos dados validados.</span></label></div><div className="col-lg-3"><button className="btn btn-primary w-100" type="submit" disabled={busy || !confirmed}><i className="bi bi-database-fill-up"></i> {busy ? "Importando..." : "Salvar dados"}</button></div></form> : <><div className="alert alert-info mb-3">{report ? "Corrija as pendencias do relatorio de qualidade e valide novamente para liberar a importacao." : <>Clique em <strong>Validar planilha</strong> para liberar o salvamento dos dados.</>}</div><div className="d-flex justify-content-end"><button className="btn btn-primary" type="button" disabled><i className="bi bi-database-fill-up"></i> Salvar dados</button></div></>}</section>
      {importResult && <section className="panel import-result"><PanelHeading icon="bi-clipboard-check-fill" title="Resultado da importacao" description="Resultado final da execucao. Linhas com falha nao foram salvas." /><div className="quality-summary import-summary"><div><span>Criados</span><strong>{importResult.createdRecords}</strong></div><div><span>Atualizados</span><strong>{importResult.updatedRecords}</strong></div><div><span>Falhas</span><strong>{importResult.failedRows}</strong></div><div><span>Ignorados</span><strong>{importResult.ignoredRows}</strong></div></div></section>}
      <section className="panel"><PanelHeading icon="bi-clipboard-check-fill" title="Mapeamento selecionado" /><div className="selected-mapping">{Object.entries(mapping).map(([field, header]) => <div key={field}><span>{field}</span><strong>{header || "Ignorar"}</strong></div>)}</div><div className="alert alert-info mb-0 mt-3">O mapeamento sera usado na validacao e na importacao confirmada.</div></section>
    </>
  );
}
function HistoryPage({ history, cityHallName }) {
  const selectedId = new URLSearchParams(window.location.search).get("batch");
  const selected = history.find((item) => String(item.id) === String(selectedId));
  return <DashboardLayout styles={["/css/spreadsheet-import.css"]}><main className="dashboard"><div className="container spreadsheet-page"><Header cityHallName={cityHallName} historyOnly /><section className="panel"><div className="table-responsive"><table className="table table-sm align-middle spreadsheet-table"><thead><tr><th>Data</th><th>Arquivo</th><th>Usuario</th><th>Destino</th><th>Modo</th><th>Situacao</th><th>Linhas</th><th>Resultado</th><th></th></tr></thead><tbody>{history.length ? history.map((batch) => <tr key={batch.id}><td>{dateTime(batch.createdAt)}</td><td>{batch.filename}</td><td>{batch.uploadedBy || "-"}</td><td>{TARGETS[batch.target]?.label || batch.target}</td><td>{batch.mode || "-"}</td><td>{STATUS_LABELS[batch.status] || batch.status}</td><td>{batch.totalRows}</td><td>{batch.createdRecords} criados / {batch.updatedRecords} atualizados / {batch.failedRows} falhas</td><td className="text-end"><a className="btn btn-sm btn-outline-secondary" href={`/importacao/historico?batch=${batch.id}`}>Detalhes</a></td></tr>) : <tr><td colSpan="9">Nenhuma importacao registrada.</td></tr>}</tbody></table></div></section>{selected && <section className="panel import-result"><PanelHeading icon="bi-journal-text" title={`Lote #${selected.id}`} description={`${selected.filename} importado por ${selected.uploadedBy || "-"}`} /><div className="quality-summary import-summary"><div><span>Total</span><strong>{selected.totalRows}</strong></div><div><span>Sucesso</span><strong>{selected.successfulRows}</strong></div><div><span>Falhas</span><strong>{selected.failedRows}</strong></div><div><span>Ignoradas</span><strong>{selected.ignoredRows}</strong></div></div><div className="d-flex justify-content-end"><button className="btn btn-outline-secondary btn-sm" type="button" onClick={() => api.downloadImport(selected.id)}><i className="bi bi-download"></i> Baixar arquivo original</button></div></section>}</div></main></DashboardLayout>;
}
export default function SpreadsheetImportPage() {
  const historyOnly = window.location.pathname.endsWith("/historico");
  const cityHallName = getSelectedCityHall()?.name || "Prefeitura";
  const [target, setTarget] = useState("departments");
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [mapping, setMapping] = useState({});
  const [mode, setMode] = useState("UPSERT");
  const [report, setReport] = useState(null);
  const [importResult, setImportResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [message, setMessage] = useState(null);
  const [busy, setBusy] = useState(false);
  const [confirmed, setConfirmed] = useState(false);
  useEffect(() => { api.getImportHistory().then(setHistory).catch(() => setHistory([])); }, []);
  async function upload(event) { event.preventDefault(); if (!file) return; setBusy(true); try { const result = await api.previewImport(target, file); setPreview(result); setMapping(result.suggestedMapping || {}); setReport(null); setImportResult(null); setConfirmed(false); setMessage({ type: "success", text: "Arquivo lido. Confira a previa e ajuste o mapeamento." }); } catch (error) { setMessage({ type: "danger", text: error.message }); } finally { setBusy(false); } }
  async function validate(event) { event.preventDefault(); setBusy(true); try { const result = await api.validateImport(preview.id, { mode, mapping }); setReport(result); setImportResult(null); setConfirmed(false); setMessage({ type: result.invalidRows || result.unmappedRequiredFields?.length ? "warning" : "success", text: result.invalidRows || result.unmappedRequiredFields?.length ? "Validacao concluida com pendencias. Nenhum registro foi salvo." : "Validacao concluida sem erros. Nenhum registro foi salvo." }); api.getImportHistory().then(setHistory).catch(() => {}); } catch (error) { setMessage({ type: "danger", text: error.message }); } finally { setBusy(false); } }
  async function execute(event) { event.preventDefault(); if (!confirmed) return; setBusy(true); try { const result = await api.executeImport(preview.id); setImportResult(result); setMessage({ type: result.failedRows ? "danger" : "success", text: result.failedRows ? "Importacao concluida com falhas em algumas linhas." : "Importacao concluida com sucesso." }); api.getImportHistory().then(setHistory).catch(() => {}); } catch (error) { setMessage({ type: "danger", text: error.message }); } finally { setBusy(false); } }
  const targetInfo = TARGETS[target];
  const insights = useMemo(() => preview ? buildInsights(preview.headers, preview.previewRows || []) : null, [preview]);
  const valid = Boolean(report && report.invalidRows === 0 && !report.unmappedRequiredFields?.length);
  const clearPreview = () => { setPreview(null); setReport(null); setImportResult(null); setFile(null); setMessage(null); setConfirmed(false); };
  const map = () => setMessage({ type: "success", text: "Mapeamento validado. Nenhum registro foi salvo nesta fase." });
  if (historyOnly) return <HistoryPage history={history} cityHallName={cityHallName} />;
  return <DashboardLayout styles={["/css/spreadsheet-import.css"]}><main className="dashboard"><div className="container spreadsheet-page"><Header cityHallName={cityHallName} preview={preview ? { clear: clearPreview } : null} />{message && <div className={`auth-message ${message.type} mb-3`}>{message.text}<button type="button" className="document-message-close" onClick={() => setMessage(null)}>×</button></div>}{!preview ? <><Templates /><UploadPanel target={target} setTarget={setTarget} file={file} setFile={setFile} busy={busy} upload={upload} /><section className="panel empty-state spreadsheet-empty"><i className="bi bi-file-earmark-spreadsheet-fill"></i><h2>Nenhuma planilha carregada</h2><p>O sistema vai validar o arquivo, detectar cabecalhos e abrir o mapeamento manual.</p></section></> : <PreviewPage preview={preview} targetInfo={targetInfo} mapping={mapping} setMapping={setMapping} report={report} valid={valid} busy={busy} mode={mode} setMode={setMode} confirmed={confirmed} setConfirmed={setConfirmed} importResult={importResult} validate={validate} execute={execute} insights={insights} map={map} />}</div></main></DashboardLayout>;
}

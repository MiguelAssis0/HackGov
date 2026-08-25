import { useEffect, useState } from "react";
import { DashboardLayout } from "../components/DashboardLayout.jsx";
import { PageHeader } from "../components/DashboardShared.jsx";
import { api } from "../services/api.js";

const emptyUpload = { title: "", documentType: "OTHER", description: "", visibility: "PERSONAL", file: null };
const emptyGenerated = { title: "", documentType: "OFICIO", description: "", visibility: "SECTOR", content: "", destinationIds: [] };

export default function DocumentsPage() {
  const [documents, setDocuments] = useState([]); const [query, setQuery] = useState("");
  const [modal, setModal] = useState(null); const [upload, setUpload] = useState(emptyUpload);
  const [generated, setGenerated] = useState(emptyGenerated); const [message, setMessage] = useState(null);

  async function load(search = query) { try { setDocuments(await api.getDocuments({ query: search })); } catch (error) { setMessage({ type: "danger", text: error.message }); } }
  useEffect(() => { const timer = window.setTimeout(() => load(query), 250); return () => window.clearTimeout(timer); }, [query]);
  async function submitUpload(event) { event.preventDefault(); try { await api.uploadDocument(upload); setModal(null); setUpload(emptyUpload); await load(); setMessage({ type: "success", text: "Documento armazenado com sucesso." }); } catch (error) { setMessage({ type: "danger", text: error.message }); } }
  async function submitGenerated(event) { event.preventDefault(); try { await api.createGeneratedDocument(generated); setModal(null); setGenerated(emptyGenerated); await load(); setMessage({ type: "success", text: "Documento gerado com sucesso." }); } catch (error) { setMessage({ type: "danger", text: error.message }); } }
  async function sign(document) { try { await api.signDocumentHomologation(document.id); await load(); setMessage({ type: "warning", text: "Assinatura registrada em HOMOLOGACAO - SEM VALIDADE JURIDICA." }); } catch (error) { setMessage({ type: "danger", text: error.message }); } }
  async function remove(document) { if (!window.confirm(`Excluir ${document.title}?`)) return; try { await api.deleteDocument(document.id); await load(); } catch (error) { setMessage({ type: "danger", text: error.message }); } }

  return <DashboardLayout styles={["/css/documentos.css"]}><main className="dashboard"><div className="container">
    <PageHeader eyebrow="Acervo e circulacao interna" title="Documentos" action={<div className="d-flex gap-2"><button className="btn btn-outline-primary" onClick={() => setModal("generated")}>Gerar documento</button><button className="btn btn-primary" onClick={() => setModal("upload")}><i className="bi bi-cloud-arrow-up"></i> Enviar arquivo</button></div>} />
    {message && <div className={`auth-message ${message.type} mb-3`}>{message.text}</div>}
    <div className="document-toolbar"><i className="bi bi-search"></i><input className="field-input" placeholder="Buscar titulo, descricao ou arquivo" value={query} onChange={(event) => setQuery(event.target.value)} /></div>
    <section className="document-grid">{documents.map((document) => <article className="document-card" key={document.id}>
      <div className="document-icon"><i className={document.contentType === "application/pdf" ? "bi bi-file-earmark-pdf" : "bi bi-file-earmark-text"}></i></div>
      <div><span className="document-type">{document.documentType}</span><h3>{document.title}</h3><p>{document.description || document.originalName}</p><small>{document.ownerName} · {(document.sizeBytes / 1024).toFixed(1)} KB · {new Date(document.createdAt).toLocaleString("pt-BR")}</small>{document.signatureStatus === "HOMOLOGATION" && <div className="homologation-badge">HOMOLOGACAO - SEM VALIDADE JURIDICA</div>}</div>
      <div className="document-actions"><button title="Baixar" onClick={() => api.downloadDocument(document)}><i className="bi bi-download"></i></button>{document.contentType === "application/pdf" && document.signatureStatus === "NONE" && <button title="Assinar em homologacao" onClick={() => sign(document)}><i className="bi bi-pen"></i></button>}<button title="Excluir" onClick={() => remove(document)}><i className="bi bi-trash"></i></button></div>
    </article>)}{documents.length === 0 && <div className="empty-state">Nenhum documento encontrado.</div>}</section>
  </div></main>
  {modal === "upload" && <Modal title="Enviar arquivo" close={() => setModal(null)}><form className="document-form" onSubmit={submitUpload}><Fields value={upload} setValue={setUpload} /><label className="field-label">Arquivo (ate 15 MB)</label><input className="field-input" type="file" required onChange={(event) => setUpload({ ...upload, file: event.target.files[0] })} /><button className="btn btn-primary">Armazenar</button></form></Modal>}
  {modal === "generated" && <Modal title="Gerar documento estruturado" close={() => setModal(null)}><form className="document-form" onSubmit={submitGenerated}><Fields value={generated} setValue={setGenerated} /><label className="field-label">Conteudo</label><textarea className="field-input" rows="10" required maxLength="100000" value={generated.content} onChange={(event) => setGenerated({ ...generated, content: event.target.value })}></textarea><button className="btn btn-primary">Gerar e armazenar</button></form></Modal>}
  </DashboardLayout>;
}

function Fields({ value, setValue }) { return <><label className="field-label">Titulo</label><input className="field-input" required maxLength="180" value={value.title} onChange={(event) => setValue({ ...value, title: event.target.value })} /><div className="document-form-row"><div><label className="field-label">Tipo</label><input className="field-input" required maxLength="60" value={value.documentType} onChange={(event) => setValue({ ...value, documentType: event.target.value })} /></div><div><label className="field-label">Visibilidade</label><select className="field-input" value={value.visibility} onChange={(event) => setValue({ ...value, visibility: event.target.value })}><option value="PERSONAL">Pessoal</option><option value="SECTOR">Setor</option><option value="CITY_HALL">Prefeitura</option></select></div></div><label className="field-label">Descricao</label><textarea className="field-input" rows="2" maxLength="2000" value={value.description} onChange={(event) => setValue({ ...value, description: event.target.value })}></textarea></>; }
function Modal({ title, close, children }) { return <div className="react-modal-backdrop" role="dialog" aria-modal="true"><div className="react-modal-card document-modal"><div className="task-modal-header"><h3>{title}</h3><button className="btn-acao" onClick={close}><i className="bi bi-x-lg"></i></button></div>{children}</div></div>; }

import { useEffect } from "react";

export function PageHeader({ eyebrow, title, action }) {
  return (
    <div className="d-flex align-items-center justify-content-between mb-3 flex-wrap gap-2">
      <div>
        <p className="eyebrow dark mb-0">{eyebrow}</p>
        <h3 className="mb-0 fw-bold management-title">{title}</h3>
      </div>
      {action}
    </div>
  );
}

export function StatusBadge({ active, children }) {
  return (
    <span className={`badge ${active ? "text-bg-success" : "text-bg-secondary"}`}>
      {children || (active ? "Ativo" : "Inativo")}
    </span>
  );
}

export function ProfileBadge({ variant = "light", children }) {
  return <span className={`badge text-bg-${variant}`}>{children}</span>;
}

export function IconButton({ icon, title, danger = false, onClick, type = "button" }) {
  return (
    <button
      className={`icon-btn ${danger ? "danger" : ""}`}
      type={type}
      title={title}
      aria-label={title}
      onClick={onClick}
    >
      <i className={`bi ${icon}`}></i>
    </button>
  );
}

export function Modal({ open, title, children, footer, onClose, size = "" }) {
  useEffect(() => {
    document.body.classList.toggle("modal-open", open);
    return () => document.body.classList.remove("modal-open");
  }, [open]);

  if (!open) return null;

  return (
    <div className="management-modal-layer" role="presentation" onMouseDown={onClose}>
      <div
        className={`management-modal ${size ? `management-modal-${size}` : ""}`}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="management-modal-header">
          <h5 className="modal-title">{title}</h5>
          <button className="btn-close" type="button" aria-label="Fechar" onClick={onClose}></button>
        </div>
        <div className="management-modal-body">{children}</div>
        {footer && <div className="management-modal-footer">{footer}</div>}
      </div>
    </div>
  );
}

export function EmptyState({ icon, children }) {
  return (
    <div className="empty-state setores-empty">
      <i className={`bi ${icon}`}></i>
      {children}
    </div>
  );
}

export function FieldLabel({ children, htmlFor }) {
  return (
    <label className="management-field-label" htmlFor={htmlFor}>
      {children}
    </label>
  );
}

export function AccessDenied() {
  return (
    <section className="panel access-denied">
      <i className="bi bi-shield-lock-fill"></i>
      <h4>Acesso restrito</h4>
      <p>Esta tela esta disponivel somente para admins da equipe Integra Brasil.</p>
    </section>
  );
}


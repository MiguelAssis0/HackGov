import { usePageStyles } from "../hooks/usePageStyles.js";
import Chatbot from "./Chatbot.jsx";

export default function Messages({
  styles = [],
  chatOpen,
  setChatOpen,
  activeTab,
  setActiveTab,
}) {
  usePageStyles(styles);

  return (
    <section className="chat-float d-none d-lg-flex" id="chatFloat">
      <div className={`chat-widget ${chatOpen ? "" : "is-collapsed"}`} id="chatWidget">
        <div className="chat-widget-header">
          <div className="chat-widget-title-wrap">
            <h5 className="chat-widget-title">
              <i className="bi bi-chat-dots-fill me-1"></i> Mensagens
            </h5>
          </div>
          <div className="chat-widget-actions">
            <button
              type="button"
              className={`chat-action-btn ${chatOpen ? "is-expanded" : ""}`}
              title="Recolher"
              aria-expanded={chatOpen}
              onClick={() => setChatOpen((value) => !value)}
            >
              <i className="bi bi-chevron-up chat-chevron-icon"></i>
            </button>
          </div>
        </div>

        <div className="chat-widget-body">
          <div className="chat-widget-fixed">
            {activeTab !== "ai" && (
              <div className="msg-busca-wrap">
                <i className="bi bi-search"></i>
                <input
                  type="text"
                  className="msg-busca"
                  placeholder="Buscar mensagens..."
                />
              </div>
            )}
            <div className="msg-tabs">
              {[
                ["chats", "bi-chat", "Chats"],
                ["files", "bi-file-earmark", "Arquivos"],
                ["contacts", "bi-person-lines-fill", "Contatos"],
                ["ai", "bi-robot", "IA"],
              ].map(([id, icon, label]) => (
                <button
                  key={id}
                  type="button"
                  className={`msg-tab ${activeTab === id ? "active" : ""}`}
                  onClick={() => setActiveTab(id)}
                >
                  <i className={`bi ${icon}`}></i> {label}
                </button>
              ))}
            </div>
          </div>

          <div className="chat-widget-scroll">
            {activeTab === "ai" ? (
              <Chatbot />
            ) : (
              <div className="d-flex flex-column">
                {[
                  ["FS", "Fulano da Silva (Setor de Obras)", "Enviou um documento", "bi-file-earmark-text"],
                  ["BM", "Beltrano Matos (Secretaria da Fazenda)", "Enviou uma imagem", "bi-image"],
                  ["FC", "Fulana Costa (Educação)", "Pode revisar o processo?", ""],
                  ["CL", "Ciclano Lima (Saúde)", "Reunião confirmada para amanhã", ""],
                ].map(([avatar, sender, preview, icon]) => (
                  <div className="msg-item" key={sender}>
                    <div className="msg-avatar">{avatar}</div>
                    <div className="msg-info">
                      <div className="msg-sender">{sender}</div>
                      <div className="msg-preview">
                        {icon && <i className={`bi ${icon}`}></i>} {preview}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
import { useState, useRef, useEffect } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { api } from "../services/api";
import "../../public/css/messages.css";

const markdownComponents = {
  a: ({ node, href, children, ...props }) => (
    <a href={href} target="_blank" rel="noreferrer" {...props}>
      {children}
    </a>
  ),
};

function MessageText({ message }) {
  if (message.from === "user") {
    return <p className="chat-plain-text">{message.text}</p>;
  }

  return (
    <div className="chat-markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={markdownComponents}
      >
        {message.text}
      </ReactMarkdown>
    </div>
  );
}

export default function Chatbot() {
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([
    {
      id: 1,
      from: "bot",
      text: "Olá! Sou o assistente do Integra Brasil. Como posso te ajudar hoje?",
      time: new Date().toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }),
    },
  ]);
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!message.trim() || loading) return;

    const userMessage = {
      id: Date.now(),
      from: "user",
      text: message.trim(),
      time: new Date().toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }),
    };

    setMessages((prev) => [...prev, userMessage]);
    setMessage("");
    setLoading(true);

    try {
      const result = await api.requestAI(userMessage.text);
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          from: "bot",
          text: result?.message ?? "Não consegui processar sua solicitação.",
          time: new Date().toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }),
        },
      ]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          from: "bot",
          text: "Erro ao comunicar com a IA. Tente novamente.",
          time: new Date().toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }),
        },
      ]);
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="chat-wrapper">
      <div className="chat-header">
        <div className="chat-header-avatar">IA</div>
        <div className="chat-header-info">
          <span className="chat-header-name">Assistente Integra Brasil</span>
          <span className="chat-header-status">
            <span className="status-dot" />
            Online
          </span>
        </div>
      </div>

      <div className="chat-messages">
        {messages.map((msg) => (
          <div key={msg.id} className={`chat-bubble-row ${msg.from === "user" ? "row-user" : "row-bot"}`}>
            {msg.from === "bot" && <div className="bubble-avatar">IA</div>}
            <div className={`chat-bubble ${msg.from === "user" ? "bubble-user" : "bubble-bot"}`}>
              <MessageText message={msg} />
              <span className="bubble-time">{msg.time}</span>
            </div>
          </div>
        ))}

        {loading && (
          <div className="chat-bubble-row row-bot">
            <div className="bubble-avatar">IA</div>
            <div className="chat-bubble bubble-bot bubble-typing">
              <span /><span /><span />
            </div>
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      <form className="chat-input-bar" onSubmit={handleSubmit}>
        <textarea
          ref={inputRef}
          rows="1"
          placeholder="Digite sua mensagem..."
          aria-label="Mensagem para a IA"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={loading}
        ></textarea>
        <button type="submit" disabled={loading || !message.trim()} aria-label="Enviar">
          <i className="bi bi-send-fill"></i>
        </button>
      </form>
    </div>
  );
}

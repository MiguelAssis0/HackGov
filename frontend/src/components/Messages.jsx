import { lazy, Suspense, useEffect, useMemo, useRef, useState } from "react";
import { usePageStyles } from "../hooks/usePageStyles.js";
import { api } from "../services/api.js";
import {
  createChatSocket,
  sendChatMessage,
  subscribeToChat,
} from "../services/chatSocket.js";

const Chatbot = lazy(() => import("./Chatbot.jsx"));

function pageItems(payload) {
  if (Array.isArray(payload)) return payload;
  return payload?.content || payload?.items || [];
}

function employeeName(employee) {
  return (
    [employee?.firstName, employee?.lastName].filter(Boolean).join(" ") ||
    employee?.fullName ||
    employee?.email ||
    "Funcionário"
  );
}

function initials(name) {
  return String(name || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

function privateChatParticipant(chat, currentEmployeeId) {
  return chat?.participants?.find(
    (participant) => String(participant.employeeId) !== String(currentEmployeeId),
  );
}

function chatName(chat, currentEmployeeId) {
  if (chat?.type === "PRIVATE") {
    return privateChatParticipant(chat, currentEmployeeId)?.fullName || "Conversa privada";
  }

  return chat?.title || "Conversa em grupo";
}

function formatTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return new Intl.DateTimeFormat("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function sortMessages(items) {
  return [...items].sort((left, right) => new Date(left.sentAt) - new Date(right.sentAt));
}

function mergeMessages(current, incoming) {
  const byId = new Map(current.map((message) => [String(message.id), message]));

  incoming.forEach((message) => {
    byId.set(String(message.id), message);
  });

  const merged = sortMessages([...byId.values()]);
  const unchanged =
    merged.length === current.length &&
    merged.every((message, index) => String(message.id) === String(current[index]?.id));

  return unchanged ? current : merged;
}

export default function Messages({
  styles = [],
  chatOpen,
  setChatOpen,
  activeTab,
  setActiveTab,
}) {
  const stylesReady = usePageStyles(styles, "widget");
  const messagesEndRef = useRef(null);
  const attachmentInputRef = useRef(null);
  const socketClientRef = useRef(null);
  const socketSubscriptionRef = useRef(null);
  const [search, setSearch] = useState("");
  const [currentEmployee, setCurrentEmployee] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [chats, setChats] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState("");
  const [attachmentFile, setAttachmentFile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [creatingChatId, setCreatingChatId] = useState(null);
  const [creatingGroup, setCreatingGroup] = useState(false);
  const [groupFormOpen, setGroupFormOpen] = useState(false);
  const [groupTitle, setGroupTitle] = useState("");
  const [groupParticipantIds, setGroupParticipantIds] = useState([]);
  const [groupSearch, setGroupSearch] = useState("");
  const [sending, setSending] = useState(false);
  const [socketConnected, setSocketConnected] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;

    async function loadData() {
      setLoading(true);
      setError("");

      const [detailsResult, employeesResult, chatsResult] = await Promise.allSettled([
        api.getEmployeeDetails(),
        api.getChatContacts(),
        api.getChats(),
      ]);

      if (!mounted) return;

      const details = detailsResult.status === "fulfilled" ? detailsResult.value : null;
      const employeeItems = employeesResult.status === "fulfilled" ? employeesResult.value : [];

      setCurrentEmployee(details);
      setEmployees(
        employeeItems.filter(
          (employee) => !details?.id || String(employee.id) !== String(details.id),
        ),
      );
      setChats(chatsResult.status === "fulfilled" ? pageItems(chatsResult.value) : []);

      if (
        detailsResult.status === "rejected" ||
        employeesResult.status === "rejected" ||
        chatsResult.status === "rejected"
      ) {
        setError("Não foi possível carregar todos os dados de mensagens.");
      }

      setLoading(false);
    }

    loadData();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (activeTab !== "chats") {
      setSelectedChat(null);
      setMessages([]);
      setMessageText("");
      setAttachmentFile(null);
      setGroupFormOpen(false);
      setGroupTitle("");
      setGroupParticipantIds([]);
      setGroupSearch("");
    }
  }, [activeTab]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ block: "end" });
  }, [messages, loadingMessages]);

  useEffect(() => {
    const client = createChatSocket({
      onConnect: () => setSocketConnected(true),
      onDisconnect: () => setSocketConnected(false),
      onError: (message) => setError(message),
    });
    socketClientRef.current = client;

    return () => {
      socketSubscriptionRef.current?.unsubscribe();
      socketSubscriptionRef.current = null;
      socketClientRef.current = null;
      setSocketConnected(false);
      client?.deactivate();
    };
  }, []);

  useEffect(() => {
    socketSubscriptionRef.current?.unsubscribe();
    socketSubscriptionRef.current = null;

    const client = socketClientRef.current;
    if (!socketConnected || !client?.connected || !selectedChat?.id) return undefined;

    const subscription = subscribeToChat(client, selectedChat.id, (message) => {
      setMessages((current) => mergeMessages(current, [message]));
    });
    socketSubscriptionRef.current = subscription;

    return () => {
      subscription.unsubscribe();
      if (socketSubscriptionRef.current === subscription) {
        socketSubscriptionRef.current = null;
      }
    };
  }, [selectedChat?.id, socketConnected]);

  const filteredEmployees = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return employees;

    return employees.filter((employee) =>
      [
        employeeName(employee),
        employee.email,
        employee.sectorName,
        employee.occupationName,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(query),
    );
  }, [employees, search]);

  const filteredChats = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) return chats;
    return chats.filter((chat) =>
      chatName(chat, currentEmployee?.id).toLowerCase().includes(query),
    );
  }, [chats, currentEmployee?.id, search]);

  const groupEmployees = useMemo(() => {
    const query = groupSearch.trim().toLowerCase();
    if (!query) return employees;

    return employees.filter((employee) =>
      [employeeName(employee), employee.occupationName, employee.sectorName]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(query),
    );
  }, [employees, groupSearch]);

  async function openChat(chat) {
    setSelectedChat(chat);
    setLoadingMessages(true);
    setError("");

    try {
      const response = await api.getChatMessages(chat.id);
      const items = pageItems(response);
      setMessages(sortMessages(items));
    } catch (requestError) {
      setMessages([]);
      setError(requestError.message || "Não foi possível carregar a conversa.");
    } finally {
      setLoadingMessages(false);
    }
  }

  async function startPrivateChat(employee) {
    if (!currentEmployee?.id) {
      setError("Não foi possível identificar o usuário autenticado.");
      return;
    }

    if (String(employee.id) === String(currentEmployee.id)) {
      setError("Você não pode criar uma conversa consigo mesmo.");
      return;
    }

    setCreatingChatId(employee.id);
    setError("");

    try {
      const chat = await api.createPrivateChat(employee.id);
      setChats((current) => {
        const exists = current.some((item) => String(item.id) === String(chat.id));
        return exists ? current : [chat, ...current];
      });
      setActiveTab("chats");
      await openChat(chat);
    } catch (requestError) {
      setError(requestError.message || "Não foi possível iniciar a conversa.");
    } finally {
      setCreatingChatId(null);
    }
  }

  function closeGroupForm() {
    setGroupFormOpen(false);
    setGroupTitle("");
    setGroupParticipantIds([]);
    setGroupSearch("");
  }

  function toggleGroupParticipant(employeeId) {
    setGroupParticipantIds((current) =>
      current.some((id) => String(id) === String(employeeId))
        ? current.filter((id) => String(id) !== String(employeeId))
        : [...current, employeeId],
    );
  }

  async function handleCreateGroup(event) {
    event.preventDefault();
    const title = groupTitle.trim();

    if (!title) {
      setError("Informe um nome para o grupo.");
      return;
    }

    if (!groupParticipantIds.length) {
      setError("Selecione ao menos um participante.");
      return;
    }

    setCreatingGroup(true);
    setError("");

    try {
      const chat = await api.createGroupChat(title, groupParticipantIds);
      setChats((current) => [chat, ...current.filter((item) => String(item.id) !== String(chat.id))]);
      closeGroupForm();
      setActiveTab("chats");
      await openChat(chat);
    } catch (requestError) {
      setError(requestError.message || "Não foi possível criar o grupo.");
    } finally {
      setCreatingGroup(false);
    }
  }

  async function handleSend(event) {
    event.preventDefault();
    const content = messageText.trim();
    if ((!content && !attachmentFile) || !selectedChat || sending) return;

    setSending(true);
    setError("");

    try {
      const client = socketClientRef.current;

      if (attachmentFile) {
        const sentMessage = await api.sendMessageAttachment(
          selectedChat.id,
          content,
          attachmentFile,
        );
        setMessages((current) => mergeMessages(current, [sentMessage]));
      } else if (client?.connected) {
        sendChatMessage(client, selectedChat.id, content);
      } else {
        const sentMessage = await api.sendMessage(selectedChat.id, content);
        setMessages((current) => mergeMessages(current, [sentMessage]));
      }

      setMessageText("");
      setAttachmentFile(null);
      if (attachmentInputRef.current) attachmentInputRef.current.value = "";
    } catch (requestError) {
      setError(requestError.message || "Não foi possível enviar a mensagem.");
    } finally {
      setSending(false);
    }
  }

  function renderChatList() {
    if (loading) return <div className="msg-state">Carregando conversas...</div>;

    return (
      <>
        <button
          type="button"
          className="msg-new-group-button"
          onClick={() => {
            setError("");
            setGroupFormOpen(true);
          }}
        >
          <i className="bi bi-people-fill"></i>
          Criar grupo
        </button>

        {!filteredChats.length ? (
          <div className="msg-state">Nenhuma conversa encontrada.</div>
        ) : (
          filteredChats.map((chat) => {
            const name = chatName(chat, currentEmployee?.id);
            return (
              <button type="button" className="msg-item msg-item-button" key={chat.id} onClick={() => openChat(chat)}>
                <div className="msg-avatar">{initials(name)}</div>
                <div className="msg-info">
                  <div className="msg-sender">{name}</div>
                  <div className="msg-preview">
                    {chat.type === "GROUP" ? `${chat.participants?.length || 0} participantes` : "Conversa privada"}
                  </div>
                </div>
                <i className="bi bi-chevron-right msg-item-arrow"></i>
              </button>
            );
          })
        )}
      </>
    );
  }

  function renderGroupForm() {
    return (
      <form className="msg-group-form" onSubmit={handleCreateGroup}>
        <div className="msg-group-form-header">
          <button type="button" className="msg-back-button" onClick={closeGroupForm}>
            <i className="bi bi-arrow-left"></i>
          </button>
          <div>
            <div className="msg-sender">Novo grupo</div>
            <div className="msg-preview">{groupParticipantIds.length} selecionado(s)</div>
          </div>
        </div>

        {error && <div className="msg-alert">{error}</div>}

        <label className="msg-group-label" htmlFor="groupTitle">Nome do grupo</label>
        <input
          id="groupTitle"
          className="msg-group-input"
          type="text"
          maxLength="100"
          placeholder="Ex.: Equipe de planejamento"
          value={groupTitle}
          onChange={(event) => setGroupTitle(event.target.value)}
        />

        <div className="msg-busca-wrap msg-group-search">
          <i className="bi bi-search"></i>
          <input
            type="text"
            className="msg-busca"
            placeholder="Buscar participantes..."
            value={groupSearch}
            onChange={(event) => setGroupSearch(event.target.value)}
          />
        </div>

        <div className="msg-group-contacts">
          {groupEmployees.map((employee) => {
            const name = employeeName(employee);
            const selected = groupParticipantIds.some(
              (id) => String(id) === String(employee.id),
            );
            return (
              <label className={`msg-group-contact ${selected ? "is-selected" : ""}`} key={employee.id}>
                <input
                  type="checkbox"
                  checked={selected}
                  onChange={() => toggleGroupParticipant(employee.id)}
                />
                <div className="msg-avatar">{initials(name)}</div>
                <div className="msg-info">
                  <div className="msg-sender">{name}</div>
                  <div className="msg-preview">
                    {[employee.occupationName, employee.sectorName].filter(Boolean).join(" · ")}
                  </div>
                </div>
              </label>
            );
          })}
          {!groupEmployees.length && (
            <div className="msg-state">Nenhum funcionário encontrado.</div>
          )}
        </div>

        <button
          type="submit"
          className="msg-create-group-submit"
          disabled={creatingGroup || !groupTitle.trim() || !groupParticipantIds.length}
        >
          <i className={`bi ${creatingGroup ? "bi-hourglass-split" : "bi-people-fill"}`}></i>
          {creatingGroup ? "Criando..." : "Criar grupo"}
        </button>
      </form>
    );
  }

  function renderContacts() {
    if (loading) return <div className="msg-state">Carregando contatos...</div>;
    if (!filteredEmployees.length) {
      return <div className="msg-state">Nenhum funcionário encontrado.</div>;
    }

    return filteredEmployees.map((employee) => {
      const name = employeeName(employee);
      const isCreating = String(creatingChatId) === String(employee.id);
      return (
        <button
          type="button"
          className="msg-item msg-item-button"
          key={employee.id}
          disabled={isCreating}
          onClick={() => startPrivateChat(employee)}
        >
          <div className="msg-avatar">{initials(name)}</div>
          <div className="msg-info">
            <div className="msg-sender">{name}</div>
            <div className="msg-preview">
              {[employee.occupationName, employee.sectorName].filter(Boolean).join(" · ") ||
                employee.email}
            </div>
          </div>
          <i className={`bi ${isCreating ? "bi-hourglass-split" : "bi-chat-dots"} msg-item-arrow`}></i>
        </button>
      );
    });
  }

  if (!stylesReady) return null;

  return (
    <section
      className={`chat-float d-none d-lg-flex ${activeTab === "ai" ? "is-ai-mode" : ""}`}
      id="chatFloat"
    >
      <div
        className={`chat-widget ${chatOpen ? "" : "is-collapsed"} ${activeTab === "ai" ? "chat-widget-ai" : ""}`}
        id="chatWidget"
      >
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

        <div className={`chat-widget-body ${activeTab === "ai" ? "chat-widget-body-ai" : ""}`}>
          <div className="chat-widget-fixed">
            {activeTab !== "ai" && !selectedChat && !groupFormOpen && (
              <div className="msg-busca-wrap">
                <i className="bi bi-search"></i>
                <input
                  type="text"
                  className="msg-busca"
                  placeholder={activeTab === "contacts" ? "Buscar funcionários..." : "Buscar conversas..."}
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </div>
            )}
            {!selectedChat && !groupFormOpen && <div className="msg-tabs">
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
                  aria-pressed={activeTab === id}
                >
                  <i className={`bi ${icon}`}></i> {label}
                </button>
              ))}
            </div>}
          </div>

          <div className="chat-widget-scroll">
            {activeTab === "ai" ? (
              <Suspense fallback={<div className="chatbot-loading">Carregando assistente...</div>}>
                <Chatbot />
              </Suspense>
            ) : groupFormOpen ? (
              renderGroupForm()
            ) : selectedChat ? (
              <div className="msg-conversation">
                <div className="msg-conversation-header">
                  <button
                    type="button"
                    className="msg-back-button"
                    title="Voltar para conversas"
                    onClick={() => {
                      setSelectedChat(null);
                      setMessages([]);
                    }}
                  >
                    <i className="bi bi-arrow-left"></i>
                  </button>
                  <div className="msg-avatar">
                    {initials(chatName(selectedChat, currentEmployee?.id))}
                  </div>
                  <div className="msg-info">
                    <div className="msg-sender">
                      {chatName(selectedChat, currentEmployee?.id)}
                    </div>
                    <div className="msg-preview">
                      {socketConnected
                        ? selectedChat.type === "GROUP"
                          ? "Conversa em grupo · online"
                          : "Conversa privada · online"
                        : "Reconectando mensagens..."}
                    </div>
                  </div>
                </div>

                <div className="msg-conversation-body">
                  {loadingMessages ? (
                    <div className="msg-state">Carregando mensagens...</div>
                  ) : messages.length ? (
                    messages.map((message) => {
                      const mine = String(message.senderId) === String(currentEmployee?.id);
                      return (
                        <div className={`msg-bubble-row ${mine ? "is-mine" : ""}`} key={message.id}>
                          <div className={`msg-bubble ${mine ? "is-mine" : ""}`}>
                            {!mine && selectedChat.type === "GROUP" && (
                              <strong className="msg-bubble-sender">{message.senderName}</strong>
                            )}
                            {message.content && <span>{message.content}</span>}
                            {message.attachmentId && (
                              <button
                                type="button"
                                className="msg-attachment"
                                onClick={async () => {
                                  try {
                                    const blob = await api.downloadMessageAttachment(
                                      selectedChat.id,
                                      message.attachmentId,
                                    );
                                    const url = URL.createObjectURL(blob);
                                    const anchor = document.createElement("a");
                                    anchor.href = url;
                                    anchor.download = message.attachmentName || "anexo";
                                    anchor.click();
                                    URL.revokeObjectURL(url);
                                  } catch (requestError) {
                                    setError(requestError.message || "Não foi possível baixar o anexo.");
                                  }
                                }}
                              >
                                <i className="bi bi-paperclip"></i>
                                <span>{message.attachmentName}</span>
                                <small>
                                  {message.attachmentSize
                                    ? `${Math.ceil(message.attachmentSize / 1024)} KB`
                                    : "Arquivo"}
                                </small>
                              </button>
                            )}
                            <time>{formatTime(message.sentAt)}</time>
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="msg-state">Envie a primeira mensagem desta conversa.</div>
                  )}
                  <div ref={messagesEndRef}></div>
                </div>

                <form className="msg-compose" onSubmit={handleSend}>
                  <input
                    ref={attachmentInputRef}
                    className="msg-attachment-input"
                    type="file"
                    aria-label="Selecionar anexo"
                    onChange={(event) => setAttachmentFile(event.target.files?.[0] || null)}
                  />
                  <button
                    type="button"
                    className={attachmentFile ? "is-selected" : ""}
                    disabled={sending}
                    title={attachmentFile ? attachmentFile.name : "Anexar arquivo"}
                    onClick={() => attachmentInputRef.current?.click()}
                  >
                    <i className="bi bi-paperclip"></i>
                  </button>
                  <textarea
                    rows="1"
                    maxLength="2000"
                    placeholder="Digite uma mensagem..."
                    value={messageText}
                    disabled={sending}
                    onChange={(event) => setMessageText(event.target.value)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter" && !event.shiftKey) {
                        event.preventDefault();
                        event.currentTarget.form?.requestSubmit();
                      }
                    }}
                  />
                  <button
                    type="submit"
                    disabled={sending || (!messageText.trim() && !attachmentFile)}
                    title="Enviar mensagem"
                  >
                    <i className={`bi ${sending ? "bi-hourglass-split" : "bi-send-fill"}`}></i>
                  </button>
                </form>
              </div>
            ) : (
              <div className="d-flex flex-column">
                {error && <div className="msg-alert">{error}</div>}
                {activeTab === "chats" && renderChatList()}
                {activeTab === "contacts" && renderContacts()}
                {activeTab === "files" && (
                  <div className="msg-state">Não há arquivos compartilhados para exibir.</div>
                )}
              </div>
            )}
          </div>
          {selectedChat && error && <div className="msg-alert msg-alert-footer">{error}</div>}
        </div>
      </div>
    </section>
  );
}

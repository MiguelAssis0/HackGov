const API_BASE_URL =
  import.meta.env.VITE_API_URL || "/api";
const API_ROOT_URL = API_BASE_URL.replace(/\/api\/?$/, "");

/**
 * Recupera token do storage
 */
function getToken() {
  return localStorage.getItem("hackgov.accessToken");
}

/**
 * Cliente HTTP único da aplicação
 */
async function requestFrom(baseUrl, path, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (!(options.body instanceof FormData) && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  const token = getToken();
  const isAuth = path.startsWith("/auth/") || path.includes("/auth/login") || path.includes("/auth/refresh");
  if (token && !isAuth) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  });

  // 204 No Content
  if (response.status === 204) return null;

  if (options.responseType === "blob") {
    if (!response.ok) {
      const error = new Error(`Erro ${response.status}`);
      error.status = response.status;
      throw error;
    }
    return response.blob();
  }

  const text = await response.text();

  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    const message =
      data?.message ||
      data?.detail ||
      data?.error ||
      `Erro ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

async function request(path, options = {}) {
  return requestFrom(API_BASE_URL, path, options);
}

async function requestTaskPath(path, options = {}) {
  try {
    return await requestFrom(API_ROOT_URL, path, options);
  } catch (error) {
    if (error.status === 404) {
      return request(path, options);
    }

    throw error;
  }
}

/**
 * API centralizada
 */
export const api = {
  getDashboard: () => request("/dashboard"),

  // AUTH
  login: (email, password) =>
    request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  verifyTwoFactor: (email, code) =>
    request("/auth/2fa/verify", {
      method: "POST",
      body: JSON.stringify({ email, code }),
    }),

  resendTwoFactor: (email) =>
    request("/auth/2fa/resend", {
      method: "POST",
      body: JSON.stringify({ email }),
    }),

  logout: () =>
    request("/auth/logout", {
      method: "POST",
    }),

  // PROFILE — 2FA toggle (Mailpit em dev, SMTP em prod via SPRING_EMAIL_*)
  toggleTwoFactor: (enabled) =>
    request("/profile/two-factor", {
      method: "PUT",
      body: JSON.stringify({ enabled }),
    }),

  updateProfile: (payload) =>
    request("/profile", {
      method: "PUT",
      body: JSON.stringify(payload),
    }),

  getProfileSettings: () => request("/profile/settings"),
  updateProfileSettings: (payload) => request("/profile/settings", { method: "PUT", body: JSON.stringify(payload) }),
  updateAccessibility: (payload) => request("/profile/accessibility", { method: "PUT", body: JSON.stringify(payload) }),

  // EMPLOYEES
  getEmployees: (params = {}) => {
    if (typeof params === "string" || Array.isArray(params)) return request("/employee?size=100&sort=firstName,asc");
    const q = params.q || params.query || "";
    const setorId = params.setorId || params.setor || "";
    const page = params.page ?? 0;
    const size = params.size ?? 100;
    const qs = new URLSearchParams({ page: String(page), size: String(size), sort: "firstName,asc" });
    if (q) qs.set("q", q);
    if (setorId) qs.set("setor", setorId);
    return request(`/employee?${qs}`);
  },

  updateEmployee: (id, payload) => request(`/employee/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  toggleEmployee: (id) => request(`/employee/${id}/toggle`, { method: "POST" }),

  getEmployeeDetails: () => request("/employee/details"),
  getSessions: () => request("/sessions"),
  revokeSession: (id) => request(`/sessions/${id}`, { method: "DELETE" }),

  // MESSAGES
  getChats: () => request("/chats"),

  getChatContacts: () => request("/chats/contacts"),

  getChatMessages: (chatId, page = 0, size = 100) =>
    request(`/chats/chat/${chatId}?page=${page}&size=${size}&sort=sentAt,desc`),

  createPrivateChat: (employeeId) =>
    request("/chats/private", {
      method: "POST",
      body: JSON.stringify({ employeeId }),
    }),

  createGroupChat: (title, participantIds) =>
    request("/chats/group", {
      method: "POST",
      body: JSON.stringify({ title, participantIds }),
    }),

  sendMessage: (chatId, content) =>
    request("/messages", {
      method: "POST",
      body: JSON.stringify({ chatId, content }),
    }),

  sendMessageAttachment: (chatId, content, file) => {
    const formData = new FormData();
    formData.append("chatId", chatId);
    formData.append("content", content || "");
    formData.append("file", file);
    return request("/messages/attachment", { method: "POST", body: formData });
  },

  downloadMessageAttachment: (chatId, attachmentId) =>
    request(`/messages/chats/${chatId}/attachments/${attachmentId}`, {
      responseType: "blob",
    }),

  getOccupations: () => request("/occupations?size=1000&sort=name,asc"),

  createOccupation: (payload) =>
    request("/occupations", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  updateOccupation: (id, payload) =>
    request(`/occupations/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),

  toggleOccupation: (id) =>
    request(`/occupations/${id}/toggle`, {
      method: "PATCH",
    }),

  createEmployee: (payload) =>
    request("/employee", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  // SECTORS
  getSectors: () => request("/sectors?size=1000&sort=name,asc"),

  createSector: (payload) =>
    request("/sectors", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  updateSector: (id, payload) =>
    request(`/sectors/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),

  toggleSector: (id) =>
    request(`/sectors/${id}/toggle`, {
      method: "PATCH",
    }),

  // STATES
  getStates: () => request("/state?size=100&sort=name,asc"),

  // CITY HALLS
  getCityHalls: () => request("/cityhall?size=100&sort=name,asc"),

  createCityHall: (payload) =>
    request("/cityhall", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  // TASK BOARDS
  getBoards: () => request("/boards?size=100&sort=name,asc"),

  createBoard: (payload) =>
    request("/boards", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getTasks: () => request("/tasks?size=100&sort=title,asc"),

  createTask: (payload) =>
    request("/tasks", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  // TASKS
  getTasks: () => request("/tasks?size=100&sort=title,asc"),

  updateTask: (id, payload) =>
    request(`/tasks/${id}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),

  deleteTask: (id) =>
    request(`/tasks/${id}`, {
      method: "DELETE",
    }),
  getTaskRequests: () => request("/task-requests"),
  createTaskRequest: (payload) => request("/task-requests", { method: "POST", body: JSON.stringify(payload) }),
  acceptTaskRequest: (id, feedback = "") => request(`/task-requests/${id}/accept`, { method: "POST", body: JSON.stringify({ feedback }) }),
  rejectTaskRequest: (id, feedback) => request(`/task-requests/${id}/reject`, { method: "POST", body: JSON.stringify({ feedback }) }),

  // AGENDA
  getAgendaEvents: (month, taskId = "") =>
    request(`/agenda/events?month=${encodeURIComponent(month)}${taskId ? `&taskId=${encodeURIComponent(taskId)}` : ""}`),

  getAgendaTasks: (month, taskId = "") =>
    request(`/agenda/events/tasks?month=${encodeURIComponent(month)}${taskId ? `&taskId=${encodeURIComponent(taskId)}` : ""}`),

  getAgendaTaskOptions: () => request("/agenda/events/task-options"),

  getAgendaAccess: () => request("/agenda/events/access"),

  getUpcomingAgendaEvents: (limit = 5) =>
    request(`/agenda/events/upcoming?limit=${limit}`),

  createAgendaEvent: (payload) =>
    request("/agenda/events", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  updateAgendaEvent: (id, payload) =>
    request(`/agenda/events/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload),
    }),

  deleteAgendaEvent: (id) =>
    request(`/agenda/events/${id}`, { method: "DELETE" }),

  // CAIXA DE ENTRADA — Django parity: caixa (pessoal/setor), leitura, q, setor, page 8
  getInbox: ({ page = 0, status = "", type = "", unreadOnly = false, query = "", caixa = "", leitura = "", setor = "", q = "" } = {}) => {
    const params = new URLSearchParams({ page: String(page), size: "8", sort: "createdAt,desc" });
    if (status) params.set("status", status);
    if (type) params.set("type", type);
    if (unreadOnly) params.set("unreadOnly", "true");
    const search = q || query;
    if (search) params.set("query", search);
    if (caixa) params.set("caixa", caixa);
    if (leitura) params.set("leitura", leitura);
    if (setor) params.set("setor", setor);
    return request(`/inbox?${params}`);
  },
  getInboxCounts: ({ setor = "", query = "" } = {}) => {
    const params = new URLSearchParams();
    if (setor) params.set("setor", setor);
    if (query) params.set("query", query);
    const qs = params.toString();
    return request(`/inbox/counts${qs ? `?${qs}` : ""}`);
  },
  getInboxEntry: (id) => request(`/inbox/${id}`),
  readInboxEntry: (id) => request(`/inbox/${id}/read`, { method: "PATCH" }),
  claimInboxEntry: (id) => request(`/inbox/${id}/claim`, { method: "PATCH" }),
  releaseInboxEntry: (id) => request(`/inbox/${id}/release`, { method: "PATCH" }),
  completeInboxEntry: (id) => request(`/inbox/${id}/complete`, { method: "PATCH" }),
  reopenInboxEntry: (id) => request(`/inbox/${id}/reopen`, { method: "PATCH" }),
  getSectorsInbox: () => request("/sectors?size=100"),

  // DETALHES DA TAREFA
  getTaskDetails: (taskId) => request(`/tasks/${taskId}/details`),
  addTaskComment: (taskId, text) => request(`/tasks/${taskId}/comments`, { method: "POST", body: JSON.stringify({ text }) }),
  updateTaskComment: (taskId, id, text) => request(`/tasks/${taskId}/comments/${id}`, { method: "PUT", body: JSON.stringify({ text }) }),
  deleteTaskComment: (taskId, id) => request(`/tasks/${taskId}/comments/${id}`, { method: "DELETE" }),
  addTaskChecklist: (taskId, title) => request(`/tasks/${taskId}/checklist`, { method: "POST", body: JSON.stringify({ title }) }),
  updateTaskChecklist: (taskId, id, title) => request(`/tasks/${taskId}/checklist/${id}`, { method: "PUT", body: JSON.stringify({ title }) }),
  toggleTaskChecklist: (taskId, id) => request(`/tasks/${taskId}/checklist/${id}/toggle`, { method: "PATCH" }),
  deleteTaskChecklist: (taskId, id) => request(`/tasks/${taskId}/checklist/${id}`, { method: "DELETE" }),
  startTaskTimer: (taskId) => request(`/tasks/${taskId}/timer/start`, { method: "POST" }),
  pauseTaskTimer: (taskId) => request(`/tasks/${taskId}/timer/pause`, { method: "POST" }),
  addTaskManualTime: (taskId, payload) => request(`/tasks/${taskId}/time-entries`, { method: "POST", body: JSON.stringify(payload) }),
  deleteTaskTime: (taskId, id) => request(`/tasks/${taskId}/time-entries/${id}`, { method: "DELETE" }),
  addTaskAttachment: (taskId, file) => {
    const body = new FormData();
    body.append("file", file);
    return request(`/tasks/${taskId}/attachments`, { method: "POST", body });
  },
  downloadTaskAttachment: async (taskId, attachment) => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}/attachments/${attachment.id}`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    });
    if (!response.ok) throw new Error(`Erro ${response.status} ao baixar anexo`);
    const url = URL.createObjectURL(await response.blob());
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = attachment.originalName || "anexo";
    anchor.click();
    URL.revokeObjectURL(url);
  },

  // CLIENTES MUNICIPAIS
  getClients: (query = "", page = 0) => request(`/clients?size=15&page=${page}&sort=fullName,asc&query=${encodeURIComponent(query)}`),
  getClientCapabilities: () => request("/clients/capabilities"),
  getClient: (id) => request(`/clients/${id}`),
  createClient: (payload) => request("/clients", { method: "POST", body: JSON.stringify(payload) }),
  updateClient: (id, payload) => request(`/clients/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  addClientService: (id, payload) => request(`/clients/${id}/services`, { method: "POST", body: JSON.stringify(payload) }),

  // PATRULHA AGRICOLA — Django 1:1: q/status com paginacao 15
  getAgricultureCatalog: () => request("/agriculture/catalog"),
  addAgricultureCatalog: (kind, payload) => request(`/agriculture/catalog/${kind}`, { method: "POST", body: JSON.stringify(payload) }),
  getAgricultureServices: (params = "") => {
    if (typeof params === "string") return request(`/agriculture/services?size=100&query=${encodeURIComponent(params)}`);
    const q = params.query ?? params.q ?? "";
    const page = params.page ?? 0;
    const size = params.size ?? 100;
    const status = params.status ?? "";
    const qs = new URLSearchParams({ page: String(page), size: String(size), sort: "scheduledDate,desc" });
    if (q) qs.set("query", q);
    if (status) qs.set("status", status);
    return request(`/agriculture/services?${qs}`);
  },
  createAgricultureService: (payload) => request("/agriculture/services", { method: "POST", body: JSON.stringify(payload) }),
  updateAgricultureService: (id, payload) => request(`/agriculture/services/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  updateAgricultureControl: (id, payload) => request(`/agriculture/services/${id}/control`, { method: "PUT", body: JSON.stringify(payload) }),
  uploadAgricultureProof: (id, file) => { const body = new FormData(); body.append("file", file); return request(`/agriculture/services/${id}/proof`, { method: "POST", body }); },

  // DOCUMENTOS
  getDocuments: ({ query = "", type = "", number = "", year = "", dateStart = "", dateEnd = "", related = "", tags = "" } = {}) => request(`/documents?query=${encodeURIComponent(query)}&type=${encodeURIComponent(type)}&number=${encodeURIComponent(number)}&year=${encodeURIComponent(year)}&dateStart=${encodeURIComponent(dateStart)}&dateEnd=${encodeURIComponent(dateEnd)}&related=${encodeURIComponent(related)}&tags=${encodeURIComponent(tags)}`),
  uploadDocument: (payload) => {
    const body = new FormData();
    body.append("title", payload.title); body.append("documentType", payload.documentType);
    body.append("description", payload.description || ""); body.append("visibility", payload.visibility);
    body.append("kind", payload.kind || "SEND");
    if (payload.number) body.append("number", payload.number);
    if (payload.year) body.append("year", payload.year);
    if (payload.documentDate) body.append("documentDate", payload.documentDate);
    if (payload.purpose) body.append("purpose", payload.purpose);
    if (payload.keywords) body.append("keywords", payload.keywords);
    if (payload.tags) body.append("tags", payload.tags);
    (payload.destinationIds || []).forEach((id) => body.append("destinationIds", id));
    body.append("file", payload.file);
    return request("/documents", { method: "POST", body });
  },
  createGeneratedDocument: (payload) => {
    const { file, ...data } = payload;
    if (!file) return request("/documents/generated", { method: "POST", body: JSON.stringify(data) });
    const body = new FormData();
    body.append("payload", new Blob([JSON.stringify(data)], { type: "application/json" }));
    body.append("file", file);
    return request("/documents/generated", { method: "POST", body });
  },
  forwardDocument: (id, destinationIds) => request(`/documents/${id}/forward`, { method: "POST", body: JSON.stringify({ destinationIds }) }),
  signDocumentHomologation: (id, consentimento) => request(`/documents/${id}/sign-homologation`, { method: "POST", body: JSON.stringify({ consentimento }) }),
  deleteDocument: (id) => request(`/documents/${id}`, { method: "DELETE" }),
  downloadDocument: async (document) => {
    const response = await fetch(`${API_BASE_URL}/documents/${document.id}/download`, { headers: { Authorization: `Bearer ${getToken()}` } });
    if (!response.ok) throw new Error(`Erro ${response.status} ao baixar documento`);
    const url = URL.createObjectURL(await response.blob()); const anchor = window.document.createElement("a");
    anchor.href = url; anchor.download = document.originalName || "documento"; anchor.click(); URL.revokeObjectURL(url);
  },
  getRequisitionDocuments: (requisitionId) => request(`/requisitions/${requisitionId}/documents`),
  uploadRequisitionDocument: (requisitionId, payload) => {
    const body = new FormData();
    body.append("title", payload.title);
    body.append("documentType", payload.documentType || "PROCESS");
    body.append("description", payload.description || "");
    body.append("file", payload.file);
    return request(`/requisitions/${requisitionId}/documents`, { method: "POST", body });
  },

  // AUDITORIA
  getAuditEvents: (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") query.set(key, value);
    });
    return request(`/audit${query.toString() ? `?${query}` : ""}`);
  },
  exportAudit: async (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (key !== "page" && value !== undefined && value !== null && value !== "") query.set(key, value);
    });
    const blob = await request(`/audit/export${query.toString() ? `?${query}` : ""}`, { responseType: "blob" });
    const url = URL.createObjectURL(blob);
    const anchor = window.document.createElement("a");
    anchor.href = url; anchor.download = "auditoria.csv"; anchor.click(); URL.revokeObjectURL(url);
  },
  verifyAuditChain: () => request("/audit/verify"),

  // GESTAO
  getManagement: (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => { if (value !== undefined && value !== null && value !== "") query.set(key, value); });
    return request(`/management${query.toString() ? `?${query}` : ""}`);
  },

  // FERRAMENTAS E FAVORITOS
  getTools: () => request("/tools"),
  updateTool: (slug, payload) => request(`/tools/${slug}`, { method: "PATCH", body: JSON.stringify(payload) }),
  updateToolCategory: (slug, categoryId) => request(`/tools/${slug}/category`, { method: "PATCH", body: JSON.stringify({ categoryId }) }),
  toggleToolFavorite: (slug) => request(`/tools/${slug}/favorite`, { method: "POST" }),
  getToolCategories: () => request("/tool-categories"),
  createToolCategory: (payload) => request("/tool-categories", { method: "POST", body: JSON.stringify(payload) }),
  updateToolCategoryFolder: (id, payload) => request(`/tool-categories/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteToolCategory: (id) => request(`/tool-categories/${id}`, { method: "DELETE" }),
  getToolPermissions: () => request("/tool-permissions"),
  createToolPermission: (payload) => request("/tool-permissions", { method: "POST", body: JSON.stringify(payload) }),
  deleteToolPermission: (id) => request(`/tool-permissions/${id}`, { method: "DELETE" }),

  // IMPORTACAO DE PLANILHAS
  previewImport: (target, file) => { const body = new FormData(); body.append("target", target); body.append("file", file); return request("/imports/preview", { method: "POST", body }); },
  validateImport: (id, payload) => request(`/imports/${id}/validate`, { method: "POST", body: JSON.stringify(payload) }),
  executeImport: (id) => request(`/imports/${id}/execute`, { method: "POST" }),
  getImportHistory: () => request("/imports/history"),

  // AI (integrado no mesmo client)
  requestAI: (message) =>
    request("/ai", {
      method: "POST",
      body: JSON.stringify({ message }),
    }),
};

/**
 * Salva sessão do usuário
 */
export async function saveSession(loginResponse, email) {
  const accessToken =
    loginResponse.accessToken || loginResponse.token;

  const refreshToken = loginResponse.refreshToken;

  if (accessToken) {
    localStorage.setItem("hackgov.accessToken", accessToken);
  }

  if (refreshToken) {
    localStorage.setItem("hackgov.refreshToken", refreshToken);
  }

  const tokenPayload = decodeJwtPayload(accessToken);
  const role = loginResponse.role || tokenPayload?.role || "";
  const cityHall = loginResponse.cityHall || loginResponse.prefeitura || "";
  const cityHallId =
    loginResponse.cityHallId ||
    loginResponse.prefeituraId ||
    cityHall?.id ||
    tokenPayload?.cityHallId ||
    "";
  const cityHallName =
    cityHall?.name ||
    loginResponse.cityHallName ||
    loginResponse.prefeituraNome ||
    (typeof cityHall === "string" ? cityHall : "");

  // ponytail: LoginResponseDTO só tem tokens, id 1 quebra UUID em tarefas (assumir/delegar) — tenta buscar UUID real via /employee/details
  let realId = loginResponse.id || loginResponse.userId || loginResponse.employeeId || "";
  let setor = loginResponse.setor || "";
  let cargo = loginResponse.cargo || role || "Servidor";
  try {
    if (accessToken && !realId) {
      const details = await request("/employee/details").catch(()=>null);
      const emp = details?.employee || details?.user || details;
      if (emp?.id) realId = emp.id;
      if (emp?.sectorName) setor = emp.sectorName;
      if (emp?.sector) setor = emp.sector;
      if (emp?.occupationName) cargo = emp.occupationName;
      else if (emp?.cargo) cargo = emp.cargo;
    }
  } catch {}

  localStorage.setItem(
    "hackgov.user",
    JSON.stringify({
      id: realId || 1,
      nome:
        loginResponse.nome ||
        loginResponse.name ||
        email,
      email: loginResponse.email || email,
      cargo,
      setor,
      prefeitura: cityHallName,
      cityHall: cityHallName ? { id: cityHallId, name: cityHallName } : cityHall || null,
      cityHallId,
      role,
      tipoUsuario: normalizeUserType(loginResponse.tipoUsuario || loginResponse.userType || role, email),
    }),
  );
}

/**
 * Remove sessão
 */
export function clearSession() {
  localStorage.removeItem("hackgov.accessToken");
  localStorage.removeItem("hackgov.refreshToken");
  localStorage.removeItem("hackgov.user");
  localStorage.removeItem("hackgov.selectedCityHall");
}

/**
 * Recupera usuário logado
 */
function isEmail(value) {
  return typeof value === "string" && value.includes("@");
}

function formatName(value) {
  if (!value) return "";
  return value
    .replace(/[_.-]+/g, " ")
    .trim()
    .split(/\s+/)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function decodeJwtPayload(token) {
  if (!token || typeof token !== "string" || !token.includes(".")) return null;

  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const paddedPayload = payload.padEnd(payload.length + ((4 - (payload.length % 4)) % 4), "=");
    return JSON.parse(globalThis.atob(paddedPayload));
  } catch {
    return null;
  }
}

export function normalizeUserType(value, email = "") {
  const raw = String(value || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/^role_/, "")
    .replace(/[\s-]+/g, "_");
  const normalizedEmail = String(email || "").toLowerCase();

  if (
    raw.includes("admin_equipe") ||
    raw.includes("team_admin") ||
    raw.includes("platform_admin") ||
    raw.includes("super_admin") ||
    normalizedEmail === "admin@admin.com"
  ) {
    return "admin_equipe";
  }

  if (raw.includes("admin_cidade") || raw.includes("city_admin") || raw === "admin") {
    return "admin_cidade";
  }

  return "usuario_comum";
}

export function getUserType(user) {
  return normalizeUserType(user?.tipoUsuario || user?.userType || user?.role || user?.cargo, user?.email);
}

export function getUserTypeLabel(type) {
  return {
    usuario_comum: "Usuario",
    admin_cidade: "Administrador da cidade",
    admin_equipe: "Admin da equipe",
  }[type || "usuario_comum"];
}

export function getSelectedCityHall() {
  try {
    return JSON.parse(localStorage.getItem("hackgov.selectedCityHall")) || null;
  } catch {
    return null;
  }
}

export function saveSelectedCityHall(cityHall) {
  if (!cityHall) {
    localStorage.removeItem("hackgov.selectedCityHall");
    window.dispatchEvent(new Event("hackgov:selectedCityHall"));
    return;
  }

  localStorage.setItem(
    "hackgov.selectedCityHall",
    JSON.stringify({
      id: cityHall.id || "",
      name: cityHall.name || cityHall.nome || "Prefeitura sem nome",
      cnpj: cityHall.cnpj || "",
    }),
  );
  window.dispatchEvent(new Event("hackgov:selectedCityHall"));
}

export function getUserDisplayName(user, fallback = "Usuário") {
  let profile = null;

  try {
    profile = JSON.parse(localStorage.getItem("hackgov.profile")) || null;
  } catch {
    profile = null;
  }

  const name = [profile?.nome, user?.nome, user?.name, user?.username].find(
    (value) => value && !isEmail(value),
  );

  if (name) return formatName(name);

  const email = profile?.email || user?.email || user?.nome || user?.name;
  if (isEmail(email)) return formatName(email.split("@")[0]);

  return fallback;
}

export function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("hackgov.user")) || null;
  } catch {
    return null;
  }
}

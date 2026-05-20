const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8080/api";
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
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  });

  // 204 No Content
  if (response.status === 204) return null;

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
  // AUTH
  login: (email, password) =>
    request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  logout: () =>
    request("/auth/logout", {
      method: "POST",
    }),

  // EMPLOYEES
  getEmployees: () => request("/employee?size=100&sort=firstName,asc"),

  // SECTORS
  getSectors: () => request("/sectors?size=100"),

  // TASK BOARDS
  getBoards: () => requestTaskPath("/boards?size=100&sort=name,asc"),

  createBoard: (payload) =>
    requestTaskPath("/boards", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  // TASKS
  getTasks: () => requestTaskPath("/tasks?size=100&sort=title,asc"),

  createTask: (payload) =>
    requestTaskPath("/tasks", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  updateTask: (id, payload) =>
    requestTaskPath(`/tasks/${id}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),

  deleteTask: (id) =>
    requestTaskPath(`/tasks/${id}`, {
      method: "DELETE",
    }),

  // REQUISITIONS
  getRequisitions: () => request("/requisitions"),

  createRequisition: (payload) =>
    request("/requisitions", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

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
export function saveSession(loginResponse, email) {
  const accessToken =
    loginResponse.accessToken || loginResponse.token;

  const refreshToken = loginResponse.refreshToken;

  if (accessToken) {
    localStorage.setItem("hackgov.accessToken", accessToken);
  }

  if (refreshToken) {
    localStorage.setItem("hackgov.refreshToken", refreshToken);
  }

  localStorage.setItem(
    "hackgov.user",
    JSON.stringify({
      id: loginResponse.id || loginResponse.userId || 1,
      nome:
        loginResponse.nome ||
        loginResponse.name ||
        email,
      email: loginResponse.email || email,
      cargo:
        loginResponse.cargo ||
        loginResponse.role ||
        "Servidor",
      setor: loginResponse.setor || "",
      prefeitura: loginResponse.prefeitura || loginResponse.cityHall || "",
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

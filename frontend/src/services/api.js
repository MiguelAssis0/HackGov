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

  getEmployeeDetails: () => request("/employee/details"),

  getOccupations: () => request("/occupations?size=100&sort=name,asc"),

  createOccupation: (payload) =>
    request("/occupations", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  createEmployee: (payload) =>
    request("/employee", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  // SECTORS
  getSectors: () => request("/sectors?size=100"),

  createSector: (payload) =>
    request("/sectors", {
      method: "POST",
      body: JSON.stringify(payload),
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
        role ||
        "Servidor",
      setor: loginResponse.setor || "",
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

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

function getToken() {
  return localStorage.getItem("hackgov.accessToken");
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let message = `Erro ${response.status}`;
    const responseCopy = response.clone();
    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch {
      const text = await responseCopy.text();
      if (text) message = text;
    }
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  async login(email, password) {
    return request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
  },

  async logout() {
    return request("/auth/logout", { method: "POST" });
  },

  async getEmployees() {
    return request("/employee");
  },

  async getRequisitions() {
    return request("/requisitions");
  },

  async createRequisition(payload) {
    return request("/requisitions", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },
};

export function saveSession(loginResponse, email) {
  const accessToken = loginResponse.accessToken || loginResponse.token;
  const refreshToken = loginResponse.refreshToken;

  if (accessToken) localStorage.setItem("hackgov.accessToken", accessToken);
  if (refreshToken) localStorage.setItem("hackgov.refreshToken", refreshToken);

  localStorage.setItem(
    "hackgov.user",
    JSON.stringify({
      nome: loginResponse.nome || loginResponse.name || email,
      cargo: loginResponse.cargo || loginResponse.role || "Servidor",
      setor: loginResponse.setor || "",
    }),
  );
}

export function clearSession() {
  localStorage.removeItem("hackgov.accessToken");
  localStorage.removeItem("hackgov.refreshToken");
  localStorage.removeItem("hackgov.user");
}

export function getStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("hackgov.user")) || null;
  } catch {
    return null;
  }
}

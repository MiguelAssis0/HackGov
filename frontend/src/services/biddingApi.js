const API_BASE_URL = import.meta.env.VITE_API_URL || "/api";

function getToken() {
  return localStorage.getItem("hackgov.accessToken");
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 204) return null;

  const text = await response.text();
  let data;

  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    const message = data?.message || data?.detail || data?.error || `Erro ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const biddingApi = {
  getRequisitions: (page = 0, size = 10) =>
    request(`/requisitions?page=${page}&size=${size}&sort=createdAt,desc`),

  getRequisitionHistory: (id) =>
    request(`/requisitions/${id}/history`),

  getProcurementEmployees: (id) =>
    request(`/requisitions/${id}/procurement-employees`),

  assignProcurementResponsible: (id, employeeId) =>
    request(`/requisitions/${id}/procurement-responsible`, {
      method: "PATCH",
      body: JSON.stringify({ employeeId }),
    }),

  getLicitationProcessByRequisition: (requisitionId) =>
    request(`/licitation-processes/requisition/${requisitionId}`),

  getLicitationHistory: (id) =>
    request(`/licitation-processes/${id}/history`),

  createLicitationProcess: (payload) =>
    request("/licitation-processes", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  publishLicitationResult: (id, payload) =>
    request(`/licitation-processes/${id}/result`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),

  getExecutionOrderByRequisition: (requisitionId) =>
    request(`/execution-orders/requisition/${requisitionId}`),

  createExecutionOrderForRequisition: (requisitionId, payload) =>
    request(`/execution-orders/requisition/${requisitionId}`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getCommitmentByRequisition: (requisitionId) =>
    request(`/commitments/requisition/${requisitionId}`),

  createCommitmentForRequisition: (requisitionId, payload) =>
    request(`/commitments/requisition/${requisitionId}`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getPaymentDeclarationByRequisition: (requisitionId) =>
    request(`/payment-declarations/requisition/${requisitionId}`),

  createPaymentDeclarationForRequisition: (requisitionId, payload) =>
    request(`/payment-declarations/requisition/${requisitionId}`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getPaymentByRequisition: (requisitionId) =>
    request(`/payments/requisition/${requisitionId}`),

  createPaymentForRequisition: (requisitionId, payload) =>
    request(`/payments/requisition/${requisitionId}`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getAccountabilityReportByRequisition: (requisitionId) =>
    request(`/accountability-reports/requisition/${requisitionId}`),

  getAccountabilityEmployees: (requisitionId) =>
    request(`/accountability-reports/requisition/${requisitionId}/employees`),

  assignAccountabilityResponsible: (requisitionId, employeeId) =>
    request(`/accountability-reports/requisition/${requisitionId}`, {
      method: "POST",
      body: JSON.stringify({ employeeId }),
    }),

  createRequisition: (payload) =>
    request("/requisitions", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  advanceRequisitionStage: (id, payload) =>
    request(`/requisitions/${id}/advance-stage`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),

  getPendingApproval: (requisitionId) =>
    request(`/approvals/pending/requisition/${requisitionId}`),

  processApproval: (id, payload) =>
    request(`/approvals/${id}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),

  getPendingAnalysis: (requisitionId) =>
    request(`/analyses/pending/requisition/${requisitionId}`),

  processAnalysis: (id, payload) =>
    request(`/analyses/${id}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    }),
};

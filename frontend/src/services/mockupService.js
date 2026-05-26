import { useEffect, useMemo, useState } from "react";
import {
  api,
  getSelectedCityHall,
  getStoredUser,
  getUserType,
  saveSelectedCityHall,
} from "./api.js";

export const mockupStorageKeys = {
  toolsState: "hackgov.toolsState",
  jobs: "hackgov.jobs",
  permissions: "hackgov.permissions",
};

export const toolColors = [
  { bg: "#e8f2ff", fg: "var(--azul)" },
  { bg: "#f3e8ff", fg: "#7c3aed" },
  { bg: "#fef3c7", fg: "#d97706" },
  { bg: "#fce7f3", fg: "#be185d" },
  { bg: "#dcfce7", fg: "#16a34a" },
  { bg: "#e0f2fe", fg: "#0284c7" },
  { bg: "#fef3c7", fg: "#d97706" },
  { bg: "#fee2e2", fg: "#dc2626" },
  { bg: "#dbeafe", fg: "var(--azul)" },
  { bg: "#f3e8ff", fg: "#7c3aed" },
  { bg: "#fef3c7", fg: "#d97706" },
];

export const mockupTools = [
  {
    id: "backup-exportacao",
    name: "Backup e Exporta\u00e7\u00e3o",
    category: "Dados",
    icon: "bi-file-earmark-arrow-down-fill",
    description: "Gere copias de seguranca e exporte dados da prefeitura.",
    mandatory: false,
    enabled: false,
  },
  {
    id: "importacao-dados",
    name: "Importa\u00e7\u00e3o de Dados",
    category: "Dados",
    icon: "bi-cloud-arrow-up-fill",
    description: "Importe bases externas para acelerar a configuracao inicial.",
    mandatory: false,
    enabled: false,
  },
  {
    id: "cargos",
    name: "Cargos",
    category: "Gestao",
    icon: "bi-person-badge-fill",
    description: "Cadastro e organizacao dos cargos por setor da prefeitura.",
    mandatory: true,
    enabled: true,
    route: "/cargos",
  },
  {
    id: "setores",
    name: "Setores",
    category: "Gestao",
    icon: "bi-building-gear",
    description: "Estruture secretarias, departamentos e areas internas.",
    mandatory: true,
    enabled: true,
    route: "/setores",
  },
  {
    id: "gestao",
    name: "Gest\u00e3o",
    category: "Gestao",
    icon: "bi-graph-up-arrow",
    description: "Acompanhe produtividade, prazos e desempenho por setor.",
    mandatory: true,
    enabled: true,
    route: "/gestao",
  },
  {
    id: "tarefas",
    name: "Tarefas",
    category: "Gestao",
    icon: "bi-check2-square",
    description: "Atribuicao, acompanhamento e gerenciamento de tarefas entre setores.",
    mandatory: true,
    enabled: true,
    route: "/tarefas",
  },
  {
    id: "compras-licitacoes",
    name: "Compras e Licita\u00e7\u00f5es",
    category: "Processos",
    icon: "bi-bag-check-fill",
    description: "Fluxos de compras, licitacoes e acompanhamento de requisicoes.",
    mandatory: true,
    enabled: true,
    route: "/processos",
  },
  {
    id: "caixa-entrada",
    name: "Caixa de Entrada",
    category: "Usuarios",
    icon: "bi-inbox-fill",
    description: "Centralize entradas, avisos e encaminhamentos dos setores.",
    mandatory: true,
    enabled: true,
  },
  {
    id: "controle-acesso",
    name: "Controle de Acesso",
    category: "Usuarios",
    icon: "bi-shield-lock-fill",
    description: "Configure permissoes de acesso por setor e cargo.",
    mandatory: true,
    enabled: true,
    route: "/controle-acesso",
  },
  {
    id: "funcionarios",
    name: "Funcion\u00e1rios",
    category: "Usuarios",
    icon: "bi-people-fill",
    description: "Gerencie servidores, perfis, setores e cargos vinculados.",
    mandatory: true,
    enabled: true,
    route: "/funcionarios",
  },
];

export const mockupSectors = [
  { id: "administracao", name: "Administra\u00e7\u00e3o", slug: "administracao", description: "Sem descricao cadastrada.", active: true },
  { id: "agricultura", name: "Agricultura", slug: "agricultura", description: "Setor de agricultura da Prefeitura Demo", active: true },
  { id: "assistencia-social", name: "Assist\u00eancia Social", slug: "assistencia-social", description: "Sem descricao cadastrada.", active: true },
  { id: "compras", name: "Compras", slug: "compras", description: "Sem descricao cadastrada.", active: true },
  { id: "cultura", name: "Cultura", slug: "cultura", description: "Sem descricao cadastrada.", active: true },
  { id: "educacao", name: "Educa\u00e7\u00e3o", slug: "educacao", description: "Sem descricao cadastrada.", active: true },
  { id: "fazenda", name: "Fazenda", slug: "fazenda", description: "Sem descricao cadastrada.", active: true },
  { id: "meio-ambiente", name: "Meio Ambiente", slug: "meio-ambiente", description: "Sem descricao cadastrada.", active: true },
  { id: "obras", name: "Obras", slug: "obras", description: "Sem descricao cadastrada.", active: true },
  { id: "recursos-humanos", name: "Recursos Humanos", slug: "recursos-humanos", description: "Sem descricao cadastrada.", active: true },
  { id: "saude", name: "Sa\u00fade", slug: "saude", description: "Sem descricao cadastrada.", active: true },
  { id: "transporte", name: "Transporte", slug: "transporte", description: "Sem descricao cadastrada.", active: true },
];

export const mockupJobs = [
  { id: "administrador-municipal", name: "Administrador Municipal", slug: "administrador-municipal", sector: "Administra\u00e7\u00e3o", active: true },
  { id: "assistente-administrativo", name: "Assistente Administrativo", slug: "assistente-administrativo", sector: "Administra\u00e7\u00e3o", active: true },
  { id: "procurador", name: "Procurador", slug: "procurador", sector: "Administra\u00e7\u00e3o", active: true },
  { id: "assistente-social", name: "Assistente Social", slug: "assistente-social", sector: "Assist\u00eancia Social", active: true },
  { id: "secretario-assistencia-social", name: "Secret\u00e1rio de Assist\u00eancia Social", slug: "secretario-de-assistencia-social", sector: "Assist\u00eancia Social", active: true },
  { id: "agente-contratacao", name: "Agente de Contrata\u00e7\u00e3o", slug: "agente-de-contratacao", sector: "Compras", active: true },
  { id: "analista-compras", name: "Analista de Compras", slug: "analista-de-compras", sector: "Compras", active: true },
  { id: "pregoeiro", name: "Pregoeiro", slug: "pregoeiro", sector: "Compras", active: true },
  { id: "produtor-cultural", name: "Produtor Cultural", slug: "produtor-cultural", sector: "Cultura", active: true },
  { id: "secretario-cultura", name: "Secretario de Cultura", slug: "secretario-cultura", sector: "Cultura", active: true },
  { id: "coordenador-pedagogico", name: "Coordenador Pedag\u00f3gico", slug: "coordenador-pedagogico", sector: "Educa\u00e7\u00e3o", active: true },
  { id: "secretario-educacao", name: "Secret\u00e1rio de Educa\u00e7\u00e3o", slug: "secretario-de-educacao", sector: "Educa\u00e7\u00e3o", active: true },
  { id: "servidor-educacao", name: "Servidor da Educa\u00e7\u00e3o", slug: "servidor-da-educacao", sector: "Educa\u00e7\u00e3o", active: true },
  { id: "analista-financeiro", name: "Analista Financeiro", slug: "analista-financeiro", sector: "Fazenda", active: true },
  { id: "contador", name: "Contador", slug: "contador", sector: "Fazenda", active: true },
  { id: "secretario-fazenda", name: "Secret\u00e1rio da Fazenda", slug: "secretario-da-fazenda", sector: "Fazenda", active: true },
  { id: "tesoureiro", name: "Tesoureiro", slug: "tesoureiro", sector: "Fazenda", active: true },
  { id: "gestor-rh", name: "Gestor de RH", slug: "gestor-de-rh", sector: "Recursos Humanos", active: true },
  { id: "analista-rh", name: "Analista de RH", slug: "analista-de-rh", sector: "Recursos Humanos", active: true },
  { id: "secretario-saude", name: "Secret\u00e1rio de Sa\u00fade", slug: "secretario-de-saude", sector: "Sa\u00fade", active: true },
  { id: "servidor-saude", name: "Servidor da Sa\u00fade", slug: "servidor-da-saude", sector: "Sa\u00fade", active: true },
  { id: "fiscal-sanitario", name: "Fiscal Sanit\u00e1rio", slug: "fiscal-sanitario", sector: "Sa\u00fade", active: true },
  { id: "engenheiro-civil", name: "Engenheiro Civil", slug: "engenheiro-civil", sector: "Obras", active: true },
  { id: "fiscal-obras", name: "Fiscal de Obras", slug: "fiscal-de-obras", sector: "Obras", active: true },
  { id: "motorista", name: "Motorista", slug: "motorista", sector: "Transporte", active: true },
  { id: "coordenador-transporte", name: "Coordenador de Transporte", slug: "coordenador-de-transporte", sector: "Transporte", active: true },
  { id: "tecnico-agricola", name: "T\u00e9cnico Agr\u00edcola", slug: "tecnico-agricola", sector: "Agricultura", active: true },
  { id: "fiscal-ambiental", name: "Fiscal Ambiental", slug: "fiscal-ambiental", sector: "Meio Ambiente", active: true },
  { id: "coordenador-meio-ambiente", name: "Coordenador de Meio Ambiente", slug: "coordenador-de-meio-ambiente", sector: "Meio Ambiente", active: true },
  { id: "assistente-cultura", name: "Assistente de Cultura", slug: "assistente-de-cultura", sector: "Cultura", active: true },
  { id: "procurador-municipal", name: "Procurador Municipal", slug: "procurador-municipal", sector: "Administra\u00e7\u00e3o", active: true },
];

export const mockupEmployees = [
  {
    id: "admin-cidade",
    name: "Admin da Cidade",
    email: "admin.cidade@integrabrasil.local",
    sector: "-",
    job: "-",
    profile: "Admin cidade",
    active: true,
  },
  {
    id: "compraldo",
    name: "Compraldo da Silva",
    email: "compraldo@gmail.com",
    sector: "Compras",
    job: "Analista de Compras",
    profile: "Servidor",
    active: true,
  },
  {
    id: "michel",
    name: "Michel",
    email: "michel@gmail.com",
    sector: "Fazenda",
    job: "Secretario da Fazenda",
    profile: "Servidor",
    active: true,
  },
  {
    id: "miguel",
    name: "Miguel Lula Inacio da Silva",
    email: "miguellula@lula.com",
    sector: "Sa\u00fade",
    job: "Secret\u00e1rio de Sa\u00fade",
    profile: "Servidor",
    active: true,
  },
  {
    id: "rodrigo",
    name: "Rodrigo Rodrigaldo Machado",
    email: "rodrigo@gmail.com",
    sector: "Recursos Humanos",
    job: "Gestor de RH",
    profile: "Servidor",
    active: false,
  },
  {
    id: "teste",
    name: "Teste da Silva",
    email: "teste@gmail.com",
    sector: "Saude",
    job: "Servidor da Saude",
    profile: "Servidor",
    active: true,
  },
];

export const mockupPermissions = [
  {
    id: "compras-analista-gerenciar",
    tool: "Compras e Licita\u00e7\u00f5es",
    sector: "Compras",
    job: "Analista de Compras",
    level: "Gerenciar",
  },
];

export const mockupStates = [
  { id: "sp", name: "S\u00e3o Paulo", uf: "SP" },
  { id: "rj", name: "Rio de Janeiro", uf: "RJ" },
  { id: "mg", name: "Minas Gerais", uf: "MG" },
  { id: "pr", name: "Paran\u00e1", uf: "PR" },
  { id: "sc", name: "Santa Catarina", uf: "SC" },
];

export const mockupDashboardCalendarDays = [
  ["30", "outro-mes"],
  ["31", "outro-mes"],
  ["1", ""],
  ["2", ""],
  ["3", ""],
  ["4", ""],
  ["5", ""],
  ["6", ""],
  ["7", ""],
  ["8", ""],
  ["9", ""],
  ["10", ""],
  ["11", ""],
  ["12", ""],
  ["13", ""],
  ["14", ""],
  ["15", ""],
  ["16", ""],
  ["17", ""],
  ["18", "has-event"],
  ["19", ""],
  ["20", "has-event"],
  ["21", "has-event hoje"],
  ["22", ""],
  ["23", ""],
  ["24", ""],
  ["25", ""],
  ["26", ""],
  ["27", ""],
  ["28", ""],
  ["29", ""],
  ["30", ""],
  ["31", ""],
  ["1", "outro-mes"],
  ["2", "outro-mes"],
];

export const mockupDashboardTaskPreview = [
  { name: "Entrega 1", color: "amarelo", meta: "At\u00e9 dia 14 de Abril, 13:00h" },
  { name: "Reuni\u00e3o", color: "primary", meta: "Dia 17 de Abril, 13:30h" },
  { name: "Entrega 2", color: "vermelho", meta: "At\u00e9 dia 31 de Abril, 14:00h" },
];

export const mockupTaskBoards = [
  { id: "demo-administracao", name: "Administracao Geral", sector: { name: "Administracao Geral" } },
  { id: "demo-obras", name: "Secretaria de Obras", sector: { name: "Secretaria de Obras" } },
  { id: "demo-fazenda", name: "Secretaria da Fazenda", sector: { name: "Secretaria da Fazenda" } },
];

export const mockupTaskEmployees = [
  { id: "demo-ana", firstName: "Ana", lastName: "Souza", email: "ana@prefeitura.local" },
  { id: "demo-maria", firstName: "Maria", lastName: "Oliveira", email: "maria@prefeitura.local" },
  { id: "demo-carlos", firstName: "Carlos", lastName: "Mendes", email: "carlos@prefeitura.local" },
];

export const mockupTasks = [
  {
    id: "demo-task-1",
    title: "Validar documentos da requisicao",
    description: "Conferir anexos enviados pelo setor de Obras antes de encaminhar para compras.",
    responsible: mockupTaskEmployees[0],
    board: mockupTaskBoards[0],
    startDate: "2026-05-20T09:00:00",
    endDate: "2026-05-22T17:00:00",
  },
  {
    id: "demo-task-2",
    title: "Solicitar parecer tecnico",
    description: "Atribuir analise tecnica ao setor de Obras e acompanhar retorno.",
    responsible: mockupTaskEmployees[1],
    board: mockupTaskBoards[1],
    startDate: "2026-05-18T10:00:00",
    endDate: "2026-05-21T16:00:00",
  },
  {
    id: "demo-task-3",
    title: "Conferir dotacao orcamentaria",
    description: "Validar disponibilidade orcamentaria para o processo em andamento.",
    responsible: mockupTaskEmployees[2],
    board: mockupTaskBoards[2],
    startDate: "2026-05-23T08:30:00",
    endDate: "2026-05-24T12:00:00",
  },
];

export const mockupSectorPerformance = [
  {
    sectorId: "administracao",
    productivity: 86,
    completed: 58,
    inProgress: 12,
    overdue: 3,
    averageResponseHours: 5.4,
    goal: 82,
    quality: 91,
    monthly: [72, 76, 80, 83, 86, 89],
    weekly: [14, 18, 17, 21, 19],
  },
  {
    sectorId: "agricultura",
    productivity: 78,
    completed: 31,
    inProgress: 8,
    overdue: 4,
    averageResponseHours: 8.2,
    goal: 75,
    quality: 84,
    monthly: [65, 68, 70, 74, 78, 80],
    weekly: [8, 9, 11, 13, 12],
  },
  {
    sectorId: "assistencia-social",
    productivity: 81,
    completed: 44,
    inProgress: 10,
    overdue: 5,
    averageResponseHours: 7.1,
    goal: 78,
    quality: 87,
    monthly: [70, 72, 76, 78, 81, 83],
    weekly: [10, 12, 14, 16, 15],
  },
  {
    sectorId: "compras",
    productivity: 92,
    completed: 73,
    inProgress: 9,
    overdue: 2,
    averageResponseHours: 4.8,
    goal: 85,
    quality: 94,
    monthly: [78, 82, 85, 88, 92, 94],
    weekly: [18, 21, 23, 25, 24],
  },
  {
    sectorId: "cultura",
    productivity: 74,
    completed: 26,
    inProgress: 7,
    overdue: 4,
    averageResponseHours: 9.5,
    goal: 74,
    quality: 82,
    monthly: [62, 66, 69, 72, 74, 77],
    weekly: [6, 7, 9, 10, 9],
  },
  {
    sectorId: "educacao",
    productivity: 88,
    completed: 67,
    inProgress: 14,
    overdue: 4,
    averageResponseHours: 6.0,
    goal: 84,
    quality: 90,
    monthly: [76, 79, 82, 84, 88, 90],
    weekly: [17, 18, 20, 24, 23],
  },
  {
    sectorId: "fazenda",
    productivity: 84,
    completed: 49,
    inProgress: 11,
    overdue: 3,
    averageResponseHours: 5.9,
    goal: 82,
    quality: 89,
    monthly: [73, 76, 78, 81, 84, 86],
    weekly: [12, 15, 16, 19, 18],
  },
  {
    sectorId: "meio-ambiente",
    productivity: 79,
    completed: 33,
    inProgress: 8,
    overdue: 4,
    averageResponseHours: 8.6,
    goal: 76,
    quality: 85,
    monthly: [66, 69, 72, 75, 79, 81],
    weekly: [7, 10, 11, 13, 12],
  },
  {
    sectorId: "obras",
    productivity: 76,
    completed: 39,
    inProgress: 16,
    overdue: 7,
    averageResponseHours: 10.4,
    goal: 80,
    quality: 79,
    monthly: [68, 70, 71, 74, 76, 78],
    weekly: [9, 13, 12, 14, 16],
  },
  {
    sectorId: "recursos-humanos",
    productivity: 83,
    completed: 41,
    inProgress: 8,
    overdue: 2,
    averageResponseHours: 5.7,
    goal: 80,
    quality: 88,
    monthly: [71, 74, 77, 80, 83, 85],
    weekly: [11, 12, 15, 17, 16],
  },
  {
    sectorId: "saude",
    productivity: 89,
    completed: 82,
    inProgress: 18,
    overdue: 5,
    averageResponseHours: 6.3,
    goal: 86,
    quality: 92,
    monthly: [79, 81, 84, 87, 89, 91],
    weekly: [20, 23, 25, 28, 27],
  },
  {
    sectorId: "transporte",
    productivity: 77,
    completed: 35,
    inProgress: 10,
    overdue: 5,
    averageResponseHours: 9.1,
    goal: 76,
    quality: 83,
    monthly: [64, 68, 71, 74, 77, 79],
    weekly: [8, 10, 12, 13, 14],
  },
];

export const pageItems = (payload) => {
  if (Array.isArray(payload)) return payload;
  return payload?.content || payload?.items || [];
};

function readStoredCollection(key, fallback) {
  try {
    const value = JSON.parse(localStorage.getItem(key));
    return Array.isArray(value) ? value : fallback;
  } catch {
    return fallback;
  }
}

function writeStoredCollection(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

function useManagedCollection(key, fallback) {
  const [items, setItems] = useState(() => readStoredCollection(key, fallback));

  function updateItems(nextItems) {
    setItems((current) => {
      const resolvedItems = typeof nextItems === "function" ? nextItems(current) : nextItems;
      writeStoredCollection(key, resolvedItems);
      return resolvedItems;
    });
  }

  return [items, updateItems];
}

export function slugify(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

export function initials(name) {
  return String(name || "")
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

export function normalizeName(value, fallback = "") {
  return value?.name || value?.nome || value?.title || fallback;
}

export function normalizeSector(item) {
  const name = normalizeName(item, "");
  if (!name) return null;
  return {
    id: item.id || slugify(name),
    name,
    slug: item.slug || slugify(name),
    description: item.description || item.descricao || "Sem descricao cadastrada.",
    active: item.active ?? item.ativo ?? item.status ?? true,
  };
}

export function normalizeEmployee(item) {
  const name =
    item.name ||
    item.nome ||
    [item.firstName, item.lastName].filter(Boolean).join(" ") ||
    "Funcionario";

  return {
    id: item.id || item.email || slugify(name),
    name,
    email: item.email || "",
    sector: item.sector || item.setor || item.sectorName || "-",
    job: item.job || item.cargo || item.occupation || item.occupationName || "-",
    profile: item.profile || item.perfil || (String(item.role || "").includes("ADMIN") ? "Admin cidade" : "Servidor"),
    active: item.active ?? item.ativo ?? item.status ?? true,
  };
}

export function getActiveCityHall() {
  const storedUser = getStoredUser() || {};
  const selected = getSelectedCityHall();
  const fromUser = storedUser.cityHall || storedUser.prefeitura || storedUser.cityHallName;

  if (selected?.name) return selected;
  if (typeof fromUser === "string" && fromUser.trim()) return { id: fromUser, name: fromUser };
  if (fromUser?.name) return fromUser;

  return { id: "prefeitura-demo", name: "Prefeitura Demo" };
}

export function useCityHallName() {
  const [cityHall, setCityHall] = useState(() => getActiveCityHall());

  useEffect(() => {
    function syncCityHall() {
      setCityHall(getActiveCityHall());
    }

    window.addEventListener("storage", syncCityHall);
    window.addEventListener("hackgov:selectedCityHall", syncCityHall);
    return () => {
      window.removeEventListener("storage", syncCityHall);
      window.removeEventListener("hackgov:selectedCityHall", syncCityHall);
    };
  }, []);

  return cityHall?.name || "Prefeitura Demo";
}

export function useSectors() {
  const [sectors, setSectors] = useState(mockupSectors);

  useEffect(() => {
    let mounted = true;

    api
      .getSectors()
      .then((response) => {
        if (!mounted) return;
        const next = pageItems(response).map(normalizeSector).filter(Boolean);
        if (next.length) setSectors(next);
      })
      .catch(() => {});

    return () => {
      mounted = false;
    };
  }, []);

  return [sectors, setSectors];
}

export function useEmployees() {
  const [employees, setEmployees] = useState(mockupEmployees);

  useEffect(() => {
    let mounted = true;

    api
      .getEmployees()
      .then((response) => {
        if (!mounted) return;
        const next = pageItems(response).map(normalizeEmployee);
        if (next.length) setEmployees(next);
      })
      .catch(() => {});

    return () => {
      mounted = false;
    };
  }, []);

  return [employees, setEmployees];
}

export function useStates() {
  const [states, setStates] = useState(mockupStates);

  useEffect(() => {
    let mounted = true;

    api
      .getStates()
      .then((response) => {
        if (!mounted) return;
        const next = pageItems(response)
          .map((state) => ({
            id: state.id || state.uf || state.name,
            name: state.name || state.nome || state.uf,
            uf: state.uf || "",
          }))
          .filter((state) => state.id && state.name);
        if (next.length) setStates(next);
      })
      .catch(() => {});

    return () => {
      mounted = false;
    };
  }, []);

  return states;
}

export function useToolsState() {
  const [overrides, setOverrides] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(mockupStorageKeys.toolsState)) || {};
    } catch {
      return {};
    }
  });

  const tools = useMemo(
    () =>
      mockupTools.map((tool) => ({
        ...tool,
        enabled: tool.mandatory ? true : (overrides[tool.id] ?? tool.enabled),
      })),
    [overrides],
  );

  function toggleTool(toolId, enabled) {
    setOverrides((current) => {
      const next = { ...current, [toolId]: enabled };
      localStorage.setItem(mockupStorageKeys.toolsState, JSON.stringify(next));
      return next;
    });
  }

  return { tools, toggleTool };
}

export function useAvailableTools() {
  const { tools } = useToolsState();
  return tools.filter((tool) => tool.mandatory || tool.enabled);
}

export function useJobs() {
  return useManagedCollection(mockupStorageKeys.jobs, mockupJobs);
}

export function usePermissions() {
  return useManagedCollection(mockupStorageKeys.permissions, mockupPermissions);
}

export function canManageCityTools() {
  const userType = getUserType(getStoredUser());
  return userType === "admin_equipe" || userType === "admin_cidade";
}

export function isTeamAdmin() {
  return getUserType(getStoredUser()) === "admin_equipe";
}

export function useCityHallSelection() {
  const [cityHall, setCityHall] = useState(() => getActiveCityHall());

  function updateCityHall(nextCityHall) {
    setCityHall(nextCityHall);
    saveSelectedCityHall(nextCityHall);
  }

  return [cityHall, updateCityHall];
}

function normalizeComparable(value) {
  return slugify(value)
    .replace(/^secretaria-/, "")
    .replace(/-geral$/, "")
    .replace(/^(da|de|do)-/, "");
}

function taskSectorName(task) {
  return (
    task?.sector?.name ||
    task?.sectorName ||
    task?.board?.sector?.name ||
    task?.board?.name ||
    task?.boardId?.name ||
    ""
  );
}

function taskMatchesSector(task, sector) {
  const taskKey = normalizeComparable(taskSectorName(task));
  const sectorKey = normalizeComparable(sector.name || sector.slug || sector.id);
  if (!taskKey || !sectorKey) return false;
  return taskKey === sectorKey || taskKey.includes(sectorKey) || sectorKey.includes(taskKey);
}

function performanceFallback(sector, index) {
  const productivity = Math.max(68, Math.min(92, 76 + ((index * 7) % 18)));
  return {
    sectorId: sector.id,
    productivity,
    completed: 28 + index * 3,
    inProgress: 7 + (index % 5),
    overdue: 2 + (index % 4),
    averageResponseHours: 5.5 + (index % 6),
    goal: 78,
    quality: Math.min(94, productivity + 4),
    monthly: [
      Math.max(45, productivity - 15),
      Math.max(48, productivity - 11),
      Math.max(52, productivity - 8),
      Math.max(55, productivity - 4),
      productivity,
      Math.min(96, productivity + 2),
    ],
    weekly: [8, 10, 12, 13, 15].map((value) => value + (index % 4)),
  };
}

export function useSectorPerformance() {
  const sectors = mockupSectors;
  const [employees] = useEmployees();
  const [tasks, setTasks] = useState(mockupTasks);

  useEffect(() => {
    let mounted = true;

    api
      .getTasks()
      .then((response) => {
        if (!mounted) return;
        const next = pageItems(response);
        if (next.length) setTasks(next);
      })
      .catch(() => {});

    return () => {
      mounted = false;
    };
  }, []);

  return useMemo(
    () =>
      sectors.map((sector, index) => {
        const profile =
          mockupSectorPerformance.find((item) => item.sectorId === sector.id || item.sectorId === sector.slug) ||
          performanceFallback(sector, index);
        const sectorEmployees = employees.filter(
          (employee) => normalizeComparable(employee.sector) === normalizeComparable(sector.name),
        );
        const sectorTasks = tasks.filter((task) => taskMatchesSector(task, sector));
        const taskBoost = sectorTasks.length ? sectorTasks.length : profile.inProgress;
        const totalTasks = profile.completed + taskBoost + profile.overdue;
        const completionRate = totalTasks ? Math.round((profile.completed / totalTasks) * 100) : profile.productivity;

        return {
          ...sector,
          ...profile,
          employees: sectorEmployees.length,
          activeTasks: taskBoost,
          totalTasks,
          completionRate,
          trend: profile.monthly.at(-1) - profile.monthly[0],
        };
      }),
    [sectors, employees, tasks],
  );
}

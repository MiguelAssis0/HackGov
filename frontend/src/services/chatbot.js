import { api } from "./api.js";

const API_URL = import.meta.env?.VITE_API_URL || "http://localhost:8080/api";

const CORE_SERVICES = new Set(["dashboard", "ferramentas", "perfil"]);

// Para adicionar um serviço: inclua uma entrada aqui e o slug correspondente em ToolConfigurationService.
const serviceCatalog = [
  { keys: ["analise preditiva", "análise preditiva", "previsao", "previsão", "recomendacao da ia", "recomendação da ia"], toolSlug: "relatorios", title: "Análise preditiva", description: "Projeções, sinais e recomendações na Gestão.", path: "/gestao?aba=preditiva", icon: "bi-stars" },
  { keys: ["dashboard", "inicio", "início", "painel"], toolSlug: "dashboard", title: "Início / Dashboard", description: "Resumo da prefeitura e atalhos do sistema.", path: "/dashboard", icon: "bi-house-door" },
  { keys: ["ferramentas", "servicos", "serviços", "catalogo", "catálogo"], toolSlug: "ferramentas", title: "Ferramentas e serviços", description: "Catálogo de serviços disponíveis para você.", path: "/ferramentas", icon: "bi-grid" },
  { keys: ["processos", "compras", "licitacoes", "licitações", "modulo de licitacoes", "módulo de licitações"], toolSlug: "compras-licitacoes", title: "Compras e Licitações", description: "Acompanhe compras, licitações e processos.", path: "/processos", icon: "bi-bag-check" },
  { keys: ["tarefas", "atividade", "atividades"], toolSlug: "tarefas", title: "Tarefas", description: "Consulte e acompanhe suas tarefas.", path: "/tarefas", icon: "bi-check2-square" },
  { keys: ["agenda", "calendario", "calendário", "eventos"], toolSlug: "agenda", title: "Agenda", description: "Visualize compromissos e eventos municipais.", path: "/agenda", icon: "bi-calendar3" },
  { keys: ["caixa de entrada", "notificacoes", "notificações"], toolSlug: "caixa-entrada", title: "Caixa de entrada", description: "Veja mensagens e solicitações recebidas.", path: "/caixa-entrada", icon: "bi-inbox" },
  { keys: ["clientes", "municipe", "munícipe"], toolSlug: "clientes-gerais", title: "Clientes", description: "Gerencie o cadastro de clientes e munícipes.", path: "/clientes", icon: "bi-people" },
  { keys: ["patrulha agricola", "patrulha agrícola", "agricultura"], toolSlug: "patrulha-agricola", title: "Patrulha agrícola", description: "Acesse os registros da patrulha agrícola.", path: "/patrulha-agricola", icon: "bi-tree" },
  { keys: ["documentos", "documento"], toolSlug: "documentos", title: "Documentos", description: "Consulte e organize documentos.", path: "/documentos", icon: "bi-file-earmark-text" },
  { keys: ["auditoria", "logs"], toolSlug: "auditoria", title: "Auditoria", description: "Consulte o histórico de ações do sistema.", path: "/auditoria", icon: "bi-shield-check" },
  { keys: ["importacao", "importação", "planilha", "excel"], toolSlug: "spreadsheet-import", title: "Importação", description: "Importe dados por planilha.", path: "/importacao", icon: "bi-upload" },
  { keys: ["perfil", "minha conta", "conta"], toolSlug: "perfil", title: "Meu perfil", description: "Atualize seus dados e preferências.", path: "/perfil", icon: "bi-person-circle" },
  { keys: ["setores", "setor"], toolSlug: "setores", title: "Setores", description: "Consulte os setores cadastrados.", path: "/setores", icon: "bi-building" },
  { keys: ["cargos", "cargo"], toolSlug: "cargos", title: "Cargos", description: "Consulte os cargos cadastrados.", path: "/cargos", icon: "bi-briefcase" },
  { keys: ["funcionarios", "funcionários", "servidores"], toolSlug: "funcionarios", title: "Funcionários", description: "Gerencie os funcionários da prefeitura.", path: "/funcionarios", icon: "bi-person-badge" },
  { keys: ["gestao", "gestão", "relatorios", "relatórios", "indicadores", "desempenho"], toolSlug: "relatorios", title: "Gestão", description: "Indicadores, gráficos e desempenho.", path: "/gestao", icon: "bi-bar-chart-line" },
  { keys: ["controle de acesso", "permissoes", "permissões", "acessos"], toolSlug: "controle-acesso", title: "Controle de acesso", description: "Configure permissões das ferramentas.", path: "/controle-acesso", icon: "bi-shield-lock" },
];

let accessPromise;
let accessToken = "";

export function getChatbotAccess() {
  const currentToken = localStorage.getItem("hackgov.accessToken") || "";
  if (!accessPromise || accessToken !== currentToken) {
    accessToken = currentToken;
    accessPromise = api.getTools()
      .then((tools) => new Set([...CORE_SERVICES, ...(tools || []).map((tool) => tool.id).filter(Boolean)]))
      .catch(() => CORE_SERVICES);
  }
  return accessPromise;
}

const navigationWords = /\b(abrir|acessar|acesso|aonde|caminho|encontro|ir|menu|navegar|onde|pagina|página|rota|entrar|ver|consultar)\b/;

function normalize(value) {
  return String(value || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().replace(/[^a-z0-9\s]/g, " ").replace(/\s+/g, " ").trim();
}

function typoDistance(left, right) {
  const matrix = Array.from({ length: left.length + 1 }, (_, row) =>
    Array.from({ length: right.length + 1 }, (_, column) => row === 0 ? column : column === 0 ? row : 0));
  for (let row = 1; row <= left.length; row += 1) {
    for (let column = 1; column <= right.length; column += 1) {
      const substitution = matrix[row - 1][column - 1] + Number(left[row - 1] !== right[column - 1]);
      matrix[row][column] = Math.min(matrix[row - 1][column] + 1, matrix[row][column - 1] + 1, substitution);
      if (row > 1 && column > 1 && left[row - 1] === right[column - 2] && left[row - 2] === right[column - 1]) {
        matrix[row][column] = Math.min(matrix[row][column], matrix[row - 2][column - 2] + 1);
      }
    }
  }
  return matrix[left.length][right.length];
}

function matchesKeyword(message, keyword) {
  const normalizedKeyword = normalize(keyword);
  if (message.includes(normalizedKeyword)) return true;
  const keywordWords = normalizedKeyword.split(/\s+/);
  const messageWords = message.split(/\s+/);
  return messageWords.some((_, start) => keywordWords.every((word, offset) => {
    const candidate = messageWords[start + offset];
    return candidate && (word.length < 5 ? candidate === word : candidate.length >= 5 && typoDistance(candidate, word) <= 1);
  }));
}

export function getLocalChatbotResponse(message, accessibleServices = CORE_SERVICES) {
  const normalized = normalize(message);
  const hasKnownServiceName = serviceCatalog.some((route) => route.keys.some((key) => normalized.includes(normalize(key))));
  const isNavigationQuestion = navigationWords.test(normalized) || normalized.trim().split(/\s+/).length <= 2 || hasKnownServiceName;
  if (!isNavigationQuestion) return null;

  const matches = serviceCatalog.filter((route) => route.keys.some((key) => matchesKeyword(normalized, key)));
  const cards = matches.filter((route) => accessibleServices.has(route.toolSlug)).slice(0, 3);
  if (!cards.length && matches.length) {
    return { text: "Não encontrei esse serviço entre as áreas liberadas para o seu perfil." };
  }
  if (!cards.length) return null;
  return {
    text: cards.length === 1 ? "Você pode acessar esta área pelo card abaixo:" : "Encontrei estas áreas relacionadas. Escolha uma para abrir diretamente:",
    cards,
  };
}

export async function requestAI(message) {
    const response = await fetch(`${API_URL}/ai`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("hackgov.accessToken")}`,
      },
      body: JSON.stringify({ message }),
    });
    return response.json();
}

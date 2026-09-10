import { useEffect } from "react";
import AgriculturePage from "./pages/AgriculturePage.jsx";
import AgendaPage from "./pages/AgendaPage.jsx";
import AuditPage from "./pages/AuditPage.jsx";
import AccessControlPage from "./pages/AccessControlPage.jsx";
import ClientsPage from "./pages/ClientsPage.jsx";
import CityHallFormPage from "./pages/CityHallFormPage.jsx";
import ContactPage from "./pages/ContactPage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import DocumentsPage from "./pages/DocumentsPage.jsx";
import EmployeesPage from "./pages/EmployeesPage.jsx";
import HomePage from "./pages/HomePage.jsx";
import InboxDetailPage from "./pages/InboxDetailPage.jsx";
import InboxPage from "./pages/InboxPage.jsx";
import JobsPage from "./pages/JobsPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import ManagementPage from "./pages/ManagementPage.jsx";
import ProcessesPage from "./pages/ProcessesPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import RegisterPage from "./pages/RegisterPage.jsx";
import SectorsPage from "./pages/SectorsPage.jsx";
import SpreadsheetImportPage from "./pages/SpreadsheetImportPage.jsx";
import TasksPage from "./pages/TasksPage.jsx";
import ToolsPage from "./pages/ToolsPage.jsx";
import Verify2FAPage from "./pages/Verify2FAPage.jsx";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";

const routes = [
  { path: "/", element: <HomePage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  { path: "/contato", element: <ContactPage /> },
  { path: "/dashboard", element: <DashboardPage /> },
  { path: "/ferramentas", element: <ToolsPage /> },
  { path: "/processos", element: <ProcessesPage /> },
  { path: "/tarefas", element: <TasksPage /> },
  { path: "/agenda", element: <AgendaPage /> },
  { path: "/caixa-entrada", element: <InboxPage /> },
  { path: "/caixa-entrada/:id", element: <InboxDetailPage /> },
  { path: "/clientes", element: <ClientsPage /> },
  { path: "/patrulha-agricola", element: <AgriculturePage /> },
  { path: "/documentos", element: <DocumentsPage /> },
  { path: "/auditoria", element: <AuditPage /> },
  { path: "/importacao", element: <SpreadsheetImportPage /> },
  { path: "/importacao/historico", element: <SpreadsheetImportPage /> },
  { path: "/perfil", element: <ProfilePage /> },
  { path: "/verify-2fa", element: <Verify2FAPage /> },
  { path: "/setores", element: <SectorsPage /> },
  { path: "/cargos", element: <JobsPage /> },
  { path: "/funcionarios", element: <EmployeesPage /> },
  { path: "/gestao", element: <ManagementPage /> },
  { path: "/controle-acesso", element: <AccessControlPage /> },
  { path: "/nova-prefeitura", element: <CityHallFormPage /> },
];

function RouteScrollToTop() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "auto" });
  }, [pathname]);

  return null;
}

export function AppRoutes() {
  return (
    <>
      <RouteScrollToTop />
      <Routes>
        {routes.map((route) => (
          <Route key={route.path} path={route.path} element={route.element} />
        ))}
        <Route path="/home" element={<Navigate to="/" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}

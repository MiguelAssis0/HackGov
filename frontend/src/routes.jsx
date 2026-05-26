import { lazy, Suspense, useEffect } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";

const HomePage = lazy(() => import("./pages/HomePage.jsx"));
const LoginPage = lazy(() => import("./pages/LoginPage.jsx"));
const RegisterPage = lazy(() => import("./pages/RegisterPage.jsx"));
const ContactPage = lazy(() => import("./pages/ContactPage.jsx"));
const DashboardPage = lazy(() => import("./pages/DashboardPage.jsx"));
const ToolsPage = lazy(() => import("./pages/ToolsPage.jsx"));
const ProcessesPage = lazy(() => import("./pages/ProcessesPage.jsx"));
const ProfilePage = lazy(() => import("./pages/ProfilePage.jsx"));
const TasksPage = lazy(() => import("./pages/TasksPage.jsx"));
const SectorsPage = lazy(() => import("./pages/SectorsPage.jsx"));
const JobsPage = lazy(() => import("./pages/JobsPage.jsx"));
const EmployeesPage = lazy(() => import("./pages/EmployeesPage.jsx"));
const ManagementPage = lazy(() => import("./pages/ManagementPage.jsx"));
const AccessControlPage = lazy(() => import("./pages/AccessControlPage.jsx"));
const CityHallFormPage = lazy(() => import("./pages/CityHallFormPage.jsx"));

const routes = [
  { path: "/", element: <HomePage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },
  { path: "/contato", element: <ContactPage /> },
  { path: "/dashboard", element: <DashboardPage /> },
  { path: "/ferramentas", element: <ToolsPage /> },
  { path: "/processos", element: <ProcessesPage /> },
  { path: "/tarefas", element: <TasksPage /> },
  { path: "/perfil", element: <ProfilePage /> },
  { path: "/setores", element: <SectorsPage /> },
  { path: "/cargos", element: <JobsPage /> },
  { path: "/funcionarios", element: <EmployeesPage /> },
  { path: "/gestao", element: <ManagementPage /> },
  { path: "/controle-acesso", element: <AccessControlPage /> },
  { path: "/nova-prefeitura", element: <CityHallFormPage /> },
];

function RouteLoading() {
  return <div className="route-loading">Carregando...</div>;
}

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
      <Suspense fallback={<RouteLoading />}>
        <Routes>
          {routes.map((route) => (
            <Route key={route.path} path={route.path} element={route.element} />
          ))}
          <Route path="/home" element={<Navigate to="/" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </>
  );
}

import { useEffect, useMemo, useState } from "react";
import { RouterContext } from "./components/RouterContext.jsx";
import HomePage from "./pages/HomePage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import RegisterPage from "./pages/RegisterPage.jsx";
import ContactPage from "./pages/ContactPage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import ToolsPage from "./pages/ToolsPage.jsx";
import ProcessesPage from "./pages/ProcessesPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";

function normalizePath(pathname) {
  const cleanPath = (pathname || "/").split(/[?#]/)[0];
  if (cleanPath === "/home") return "/";
  return cleanPath.endsWith("/") && cleanPath !== "/" ? cleanPath.slice(0, -1) : cleanPath;
}

export default function App() {
  const [path, setPath] = useState(() => normalizePath(window.location.pathname));

  useEffect(() => {
    const onPopState = () => setPath(normalizePath(window.location.pathname));
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  const router = useMemo(
    () => ({
      path,
      navigate(to) {
        const nextPath = normalizePath(to);
        window.history.pushState({}, "", to);
        window.scrollTo({ top: 0, behavior: "auto" });
        setPath(nextPath);
      },
    }),
    [path],
  );

  const page = {
    "/": <HomePage />,
    "/login": <LoginPage />,
    "/register": <RegisterPage />,
    "/contato": <ContactPage />,
    "/dashboard": <DashboardPage />,
    "/ferramentas": <ToolsPage />,
    "/processos": <ProcessesPage />,
    "/perfil": <ProfilePage />,
  }[path] || <HomePage />;

  return <RouterContext.Provider value={router}>{page}</RouterContext.Provider>;
}

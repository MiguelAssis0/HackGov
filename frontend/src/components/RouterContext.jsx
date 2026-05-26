import {
  Link as RouterLink,
  useLocation,
  useNavigate,
} from "react-router-dom";

function normalizePath(pathname) {
  const cleanPath = (pathname || "/").split(/[?#]/)[0];
  if (cleanPath === "/home") return "/";
  return cleanPath.endsWith("/") && cleanPath !== "/" ? cleanPath.slice(0, -1) : cleanPath;
}

export function useRouter() {
  const location = useLocation();
  const navigateRouter = useNavigate();

  return {
    path: normalizePath(location.pathname),
    navigate(to, options) {
      navigateRouter(to, options);
    },
  };
}

export function Link({ to, className, children, ...props }) {
  return (
    <RouterLink to={to} className={className} {...props}>
      {children}
    </RouterLink>
  );
}

import { BrowserRouter } from "react-router-dom";
import { AppRoutes } from "./routes.jsx";

export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}

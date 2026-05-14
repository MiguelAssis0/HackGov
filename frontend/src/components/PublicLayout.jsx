import { useEffect, useState } from "react";
import { Link } from "./RouterContext.jsx";
import { usePageStyles } from "../hooks/usePageStyles.js";

export function PublicLayout({ children, styles = [] }) {
  const [showScrollTop, setShowScrollTop] = useState(false);
  usePageStyles(["/css/index.css", ...styles]);

  useEffect(() => {
    const onScroll = () => setShowScrollTop(window.scrollY > 400);
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) entry.target.classList.add("visible");
        });
      },
      { threshold: 0.15 },
    );

    document.querySelectorAll(".fade-up").forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, [children]);

  function scrollToTop() {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  return (
    <>
      <nav className="navbar-custom">
        <div className="container d-flex align-items-center justify-content-between">
          <Link className="brand-txt" to="/">
            <i className="bi bi-file-earmark-fill"></i> Integra <span>Brasil</span>
          </Link>

          <div className="gap-2 d-none d-md-flex">
            <Link to="/login" className="btn btn-outline-primary">
              Entrar <i className="bi bi-box-arrow-in-right"></i>
            </Link>
            <Link to="/contato" className="btn btn-primary">
              Contato <i className="bi bi-person-plus-fill"></i>
            </Link>
          </div>

          <div className="d-flex d-md-none">
            <i className="bi bi-list fs-3 text-white"></i>
          </div>
        </div>
      </nav>

      <button
        className={`scroll-top-btn ${showScrollTop ? "show" : ""}`}
        type="button"
        aria-label="Voltar ao topo"
        onClick={scrollToTop}
      >
        <i className="bi bi-arrow-up"></i>
      </button>

      {children}

      <footer>
        <div className="container">
          <div className="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
            <span className="brand-txt">
              <i className="bi bi-file-earmark-fill"></i> Integra <span>Brasil</span>
            </span>
            <span>Todos os direitos reservados, © 2026.</span>
            <div className="d-flex gap-3">
              <a href="#">Política de Privacidade</a>
              <a href="#">Termos de Uso</a>
              <Link to="/contato">Suporte</Link>
            </div>
          </div>
        </div>
      </footer>
    </>
  );
}

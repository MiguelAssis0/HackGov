import { useState } from "react";
import { PublicLayout } from "../components/PublicLayout.jsx";
import { Link } from "../components/RouterContext.jsx";

const features = [
  {
    icon: "bi-diagram-3-fill",
    title: "Gestão de Processos",
    text: "Acompanhe o ciclo completo de cada processo municipal no sistema, com rastreamento em tempo real e histórico completo de ações.",
  },
  {
    icon: "bi-check2-square",
    title: "Controle de Tarefas",
    text: "Atribua, monitore e realize tarefas. Seja entre equipes ou secretarias, e com visualização de progresso e prazos.",
  },
  {
    icon: "bi-chat-dots-fill",
    title: "Comunicação Integrada",
    text: "Troca de qualquer tipo de arquivo, mensagens e memorandos entre setores. Ninguém mais precisa sair do seu posto para entregar documentos em papel!",
  },
  {
    icon: "bi-bar-chart-fill",
    title: "Relatórios e Indicadores",
    text: "Dashboards com métricas de desempenho, gráficos e exportação de dados para tomada de decisão baseada em evidências.",
  },
  {
    icon: "bi-wrench-adjustable-circle-fill",
    title: "Ferramentas por Setor",
    text: "Ferramentas específicas para cada secretaria, adaptadas às necessidades particulares de cada setor. Se faltar algo, ou quiser uma ferramenta nova, é só pedir!",
  },
  {
    icon: "bi-shield-lock-fill",
    title: "Segurança e Conformidade",
    text: "Sistema com autenticação segura, criptografia e seguindo as conformidades da LGPD. (Qualquer problema envolvendo segurança é culpa do Michel)",
  },
];

const team = [
  {
    name: "Michel",
    fullName: "Michel Pereira Dos Santos",
    role: "Desenvolvedor Backend",
    location: "São Paulo - SP",
    image: "/img/michel.png",
  },
  {
    name: "Miguel",
    fullName: "Miguel Kawe Dos Anjos Assis",
    role: "Desenvolvedor Backend",
    location: "Caçapava - SP",
    image: "/img/miguel.jpeg",
  },
  {
    name: "Rodrigo",
    fullName: "Rodrigo Froehlich Machado",
    role: "Desenvolvedor Frontend",
    location: "São Gabriel - RS",
    image: "/img/rodrigo.png",
  },
];

function scrollToSection(event, selector) {
  event.preventDefault();
  const element = document.querySelector(selector);
  const navbar = document.querySelector(".navbar-custom");
  if (!element) return;
  const offset = navbar ? navbar.offsetHeight + 12 : 0;
  const top = element.getBoundingClientRect().top + window.scrollY - offset;
  window.scrollTo({ top: Math.max(top, 0), behavior: "smooth" });
}

function TeamCard({ member }) {
  return (
    <div className="swiper-slide">
      <div className="equipe-card">
        <div className="equipe-card-img overflow-hidden">
          <img src={member.image} alt={member.fullName} className="img-fluid" />
        </div>
        <h3 className="equipe-card-name">{member.name}</h3>
        <span className="equipe-card-profession">{member.role}</span>
        <div className="equipe-info">
          <div className="equipe-info-icon">
            <i className="ri-information-line"></i>
          </div>
          <div className="equipe-info-img overflow-hidden">
            <img src={member.image} alt={member.fullName} className="img-fluid" />
          </div>
          <div className="equipe-info-data">
            <h3 className="equipe-info-name">{member.fullName}</h3>
            <span className="equipe-info-profession">Aluno de SI na FIAP</span>
            <span className="equipe-info-location">{member.location}</span>
          </div>
          <div className="equipe-info-social">
            {[
              ["https://www.linkedin.com/", "ri-linkedin-box-line"],
              ["https://instagram.com/", "ri-instagram-fill"],
              ["https://github.com/", "ri-github-fill"],
            ].map(([href, icon]) => (
              <a
                href={href}
                target="_blank"
                rel="noreferrer"
                className="equipe-info-social-link"
                key={icon}
              >
                <span className="equipe-info-social-icon">
                  <i className={icon}></i>
                </span>
              </a>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function HomePage() {
  const [activeTeam, setActiveTeam] = useState(0);

  return (
    <PublicLayout styles={["/css/swiper-bundle.min.css", "/css/home.css"]}>
      <section className="hero">
        <div className="hero-grid-lines"></div>
        <div className="container py-5">
          <div className="row align-items-center g-5">
            <div className="col-lg-6 hero-content d-flex flex-column text-center text-lg-start justify-content-center justify-content-lg-start">
              <div className="hero-badge fade-up visible me-auto ms-auto ms-lg-0">
                Um projeto desenvolvido por alunos da FIAP!
              </div>
              <h1 className="fade-up visible delay-1">
                Gestão pública
                <br />
                mais <span className="primary">inteligente</span>
                <br />e transparente
              </h1>
              <p className="section-sub fade-up visible delay-2">
                Centralize processos, tarefas e comunicação da sua prefeitura em um único sistema. Mais
                agilidade, controle e automação.
              </p>
              <div className="d-flex justify-content-center justify-content-lg-start flex-wrap gap-3 fade-up visible delay-3">
                <Link to="/contato" className="btn btn-primary">
                  Entre em contato
                </Link>
                <Link to="/login" className="btn btn-outline-primary">
                  Já tenho conta
                </Link>
              </div>
            </div>

            <div className="col-lg-6 fade-up visible delay-2 d-none d-lg-flex justify-content-center hero-img-col">
              <div className="hero-img-wrapper">
                <img src="/img/brasil.png" alt="imagem do brasil" className="img-fluid hero-img" />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="linha-de-status">
        <div className="container">
          <div className="row g-4 text-center">
            {[
              ["3", "bi-person-arms-up", "Devs trabalhando", "#equipe"],
              ["2/7", "bi-check-circle-fill", "Fases concluídas", "#documentacao"],
              ["0", "bi-people-fill", "Usuários cadastrados", ""],
              ["0", "bi-buildings-fill", "Municípios atendidos", ""],
            ].map(([number, icon, desc, target], index) => {
              const content = (
                <>
                  <div className="number">
                    {number}
                    <span>
                      {" "}
                      <i className={`bi ${icon}`}></i>
                    </span>
                  </div>
                  <div className="desc">{desc}</div>
                </>
              );

              return (
                <div className={`col-6 col-md-3 item-de-status fade-up delay-${index + 1}`} key={desc}>
                  {target ? (
                    <a href={target} className="scroll-link" onClick={(event) => scrollToSection(event, target)}>
                      {content}
                    </a>
                  ) : (
                    content
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </section>

      <section className="py-6 branco-bg section-padding-6">
        <div className="container">
          <div className="row mb-5">
            <div className="col-lg-6">
              <div className="section-label fade-up">Funcionalidades</div>
              <h2 className="section-title fade-up delay-1">
                Tudo que sua prefeitura precisa em um só lugar
              </h2>
            </div>
            <div className="col-lg-5 offset-lg-1 d-flex align-items-end">
              <p className="section-sub fade-up delay-2">
                Do controle de processos à comunicação entre setores. O ERP Municipal oferece ferramentas
                completas para tornar a gestão pública mais digital.
              </p>
            </div>
          </div>

          <div className="row g-4">
            {features.map((feature, index) => (
              <div className={`col-md-4 fade-up delay-${index % 3}`} key={feature.title}>
                <div className="card">
                  <div className="card-icon">
                    <i className={`bi ${feature.icon}`}></i>
                  </div>
                  <h3>{feature.title}</h3>
                  <p>{feature.text}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="sobre cinza-claro-bg section-padding-6">
        <div className="container">
          <div className="row mb-5">
            <div className="col-lg-6">
              <div className="section-label fade-up">Sobre</div>
              <h2 className="section-title fade-up delay-1 mb-4">Do que se trata esse projeto?</h2>
              <p className="section-sub fade-up delay-2">
                Esse site é nossa solução para o HackGov, uma atividade proposta pela FIAP em 2026. O tema
                é sobre transformação digital na gestão pública. Acabamos por desenvolver uma ferramenta
                diferente dos outros grupos, que focaram mais em soluções para a população; a nossa ideia
                foca em quem "faz a manivela girar", por assim dizer, nos servidores públicos.
              </p>
              <p className="section-sub fade-up delay-3">
                A plataforma foi desenvolvida inteiramente por três alunos da FIAP ON, turma 2SIOA-2026.
                Logo abaixo você pode saber mais sobre nosso projeto baixando nossa documentação ou sobre a
                gente.
              </p>
            </div>

            <div className="col-lg-5 offset-lg-1 d-flex align-items-end">
              <div className="visual fade-up delay-3 w-100">
                <div className="visual-header">
                  <div className="dot vermelho-bg"></div>
                  <div className="dot amarelo-bg"></div>
                  <div className="dot verde-bg"></div>
                  <span className="visual-title">Dashboard - Visão Geral</span>
                </div>

                <div className="stat-row">
                  <div className="stat-card">
                    <div className="stat-label">Processos Ativos</div>
                    <div className="stat-value">
                      347 <small>↑ 12%</small>
                    </div>
                  </div>
                  <div className="stat-card">
                    <div className="stat-label">Tarefas Concluídas</div>
                    <div className="stat-value">
                      813 <small>↑ 8%</small>
                    </div>
                  </div>
                </div>

                {[
                  ["Secretaria de Saúde", "82%", "vermelho-bg"],
                  ["Secretaria de Obras", "67%", "amarelo-bg"],
                  ["Secretaria de Agricultura", "91%", "verde-bg"],
                ].map(([name, width, color]) => (
                  <div className="bar" key={name}>
                    <div className="bar-label">
                      <span>{name}</span>
                      <span style={{ color: "#fff" }}>{width}</span>
                    </div>
                    <div className="bar-track">
                      <div className={`bar-fill ${color}`} style={{ width }}></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="equipe" className="equipe branco-bg section-padding-6">
        <div className="container">
          <div className="section-label text-center fade-up">Nossa equipe de</div>
          <h2 className="section-title text-center fade-up delay-1 mb-5">Desenvolvedores</h2>

          <div className="equipe-swiper swiper">
            <div className="swiper-wrapper">
              {team.map((member) => (
                <TeamCard member={member} key={member.name} />
              ))}
            </div>

            <div className="swiper-pagination">
              {team.map((member, index) => (
                <button
                  type="button"
                  aria-label={`Ver ${member.name}`}
                  className={`swiper-pagination-bullet ${
                    activeTeam === index ? "swiper-pagination-bullet-active" : ""
                  }`}
                  onClick={() => setActiveTeam(index)}
                  key={member.name}
                ></button>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section id="documentacao" className="sobre cinza-claro-bg section-padding-6">
        <div className="container text-center position-relative">
          <div className="justify-content-center">
            <h2 className="section-title text-center fade-up delay-1">Documentação</h2>
            <p className="mb-5 fade-up delay-2">
              A documentação do ERP Municipal está disponível para download.
            </p>

            <div className="row g-3">
              {Array.from({ length: 7 }, (_, index) => {
                const phase = index + 1;
                const href = phase <= 2 ? `/docs/HackGov_Fase_${phase}.pdf` : "#";
                return (
                  <div className={`col fade-up delay-${phase}`} key={phase}>
                    <a
                      href={href}
                      target={phase <= 2 ? "_blank" : undefined}
                      rel={phase <= 2 ? "noreferrer" : undefined}
                      className={`btn btn-primary btn-primary-lg ${phase > 2 ? "disabled" : ""}`}
                    >
                      Fase {phase} <i className="bi bi-download"></i>
                    </a>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </section>

      <section className="cta-section gradiente-bg section-padding-6">
        <div className="container text-center position-relative">
          <div className="row justify-content-center">
            <div className="col-lg-7 fade-up">
              <h2>Pronto para modernizar sua prefeitura?</h2>
              <p className="mt-3 mb-4 cinza">Cadastre-se ou faça login e comece a usar o ERP Municipal!</p>

              <div className="d-flex gap-3 justify-content-center flex-wrap">
                <Link to="/contato" className="btn btn-primary">
                  Entrar em contato
                </Link>
                <Link to="/login" className="btn btn-outline-primary">
                  Já tenho uma conta
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>
    </PublicLayout>
  );
}

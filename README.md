<div align="center">
  <span style="font-size: 64px;">🏛️</span>

  <h1>HackGov</h1>

  <p>
    Plataforma municipal para gestão integrada de processos, tarefas, comunicação interna e apoio operacional.
  </p>

  <p>
    <img alt="Java" src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
    <img alt="React" src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
    <img alt="Vite" src="https://img.shields.io/badge/Vite-7-646CFF?style=for-the-badge&logo=vite&logoColor=white" />
    <img alt="License" src="https://img.shields.io/badge/License-Academic-green?style=for-the-badge" />
  </p>
</div>

---

## Sobre

**HackGov** é uma solução web pensada para centralizar rotinas administrativas de uma prefeitura em um único ambiente. O sistema combina backend em **Java/Spring Boot** e frontend em **React/Vite** para organizar fluxos internos com mais rastreabilidade, segurança e agilidade.

O projeto cobre desde cadastro e governança municipal até requisições, aprovações, tarefas, mensagens e apoio com IA.

## Documentação

- Fase 1 - https://docs.google.com/document/d/1VyVmYJme32fxz1gj0j8UciZEK1h3tnn6EnchARqw0m8/edit?usp=sharing
- Fase 2 - https://docs.google.com/document/d/1a4SZjF-kROwfuIelkwPM8N8aXTQZkGIqoVtdb5gFEG0/edit?usp=sharing
- Fase 3 - https://docs.google.com/document/d/1R0T0LqrMhG-JAuAYGsOdOuhcqgFUhftO9G5RfKHnr2A/edit?usp=sharing
- Fase 4 - https://docs.google.com/document/d/1jjb1ibS3TBDOjyjgHxo70r9XnZEhO4RS6bPrJGww_Eg/edit?usp=sharing
- Fase 5 -
- Fase 6 -
- Fase 7 -

- Enterprise Challenge 1 - https://docs.google.com/document/d/18eNyBX4_g2aRiT7AfrPjtZAP0o9axsrrneLe3h1PiPU/edit?usp=sharing
- Enterprise Challenge 2 - https://docs.google.com/document/d/1ThQgIluIs597GAoAbnY5IuSneuIsbG-hWDb3u2Iqlhw/edit?usp=sharing
- Enterprise Challenge 3 - https://docs.google.com/document/d/1V1A9BkeTjiZtBLCIWJ_6Sk7FzUFPsMRL7MAx6cl51ZY/edit?usp=sharing

## Recursos

- Autenticação com login, cadastro, JWT e 2FA.
- Controle de acesso, blacklist de token e rate limiting.
- Gestão municipal com cidades, setores, cargos, funcionários, ocupações, permissões e ferramentas.
- Fluxo de requisições e licitações com aprovações, histórico e etapas administrativas.
- Quadros de tarefas para acompanhamento operacional.
- Chat e mensagens em tempo real entre setores.
- Endpoint de IA integrado ao backend.
- Documentação de API com Swagger.
- Perfil de desenvolvimento com H2 em memória.

## Stack

| Camada | Tecnologia |
| --- | --- |
| Backend | Java 21 |
| Framework | Spring Boot |
| Segurança | Spring Security, JWT, 2FA |
| Persistência | Spring Data JPA |
| Banco | H2 e PostgreSQL |
| Tempo real | WebSocket |
| Documentação | SpringDoc OpenAPI |
| Frontend | React 19 + Vite |
| Roteamento | React Router |
| UI | Bootstrap, Bootstrap Icons, Remix Icon |

## Estrutura do Projeto

```text
HackGov
├── backend
│   ├── src/main/java/com/fiap/hackgov
│   ├── src/main/resources
│   └── pom.xml
├── frontend
│   ├── src
│   ├── public
│   └── vite.config.js
└── run_project.py
```

## Módulos Principais

### Autenticação e Segurança

- login e cadastro
- 2FA
- JWT
- blacklist de token
- rate limit
- permissões por perfil

### Gestão Municipal

- cidade
- setores
- cargos
- funcionários
- ocupações
- permissões
- ferramentas

### Processos e Licitações

- requisições
- aprovações
- contratos
- empenhos
- ordens de execução
- declarações de pagamento
- relatórios de prestação de contas
- histórico de etapas

### Tarefas e Comunicação

- quadros de tarefas
- cartões de trabalho
- mensagens
- chat em tempo real

### IA

- endpoint dedicado para interação com IA
- integração via chave externa

## Rotas do Frontend

- `/` ou `/home` - página inicial
- `/login` - autenticação
- `/register` - cadastro
- `/contato` - contato
- `/dashboard` - painel principal
- `/ferramentas` - ferramentas
- `/processos` - processos
- `/tarefas` - tarefas
- `/perfil` - perfil
- `/setores` - setores
- `/cargos` - cargos
- `/funcionarios` - funcionários
- `/gestao` - gestão
- `/controle-acesso` - controle de acesso
- `/nova-prefeitura` - cadastro inicial da prefeitura

## Como Rodar

### Execução completa

Na raiz do projeto:

```bash
python run_project.py
```

O script:

- sobe o backend Spring Boot com o perfil `dev`
- sobe o frontend React
- aguarda o backend ficar pronto antes de liberar a interface

### Execução manual

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Portas e URLs

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- API local: `/api` via proxy do Vite
- Swagger: `http://localhost:8080/swagger-ui.html`
- H2 Console no perfil `dev`: `http://localhost:8080/h2-console`

## Configuração

O frontend usa, por padrão, a rota `/api`, e o Vite encaminha as requisições para o backend em `http://localhost:8080`.

Se quiser sobrescrever a base da API:

```bash
VITE_API_URL=http://localhost:8080/api npm run dev
```

Variáveis úteis no backend:

- `BACKEND_PORT`
- `JWT_SECRET`
- `JWT_REFRESH_SECRET`
- `GOOGLE_API_KEY`
- credenciais de e-mail

## Observações

- O perfil `dev` usa H2 em memória.
- O backend também tem suporte a PostgreSQL.
- A API já expõe documentação via SpringDoc.

## Licença

Projeto acadêmico FIAP - Turma 2SIOA, 2026.

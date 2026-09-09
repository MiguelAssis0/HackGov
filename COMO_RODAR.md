# Como rodar o HackGov React + Java

Guia passo a passo para executar este projeto, que possui frontend React/Vite e
backend Java/Spring Boot. Este documento se aplica somente à pasta `HackGov`;
ele não cobre o projeto Django `Sistema-ERP-Municipal`.

> O projeto usa Java 21. Não altere a versão para Java 25.

## 1. Pré-requisitos

### Execução com Docker

Instale e deixe iniciado:

- Docker Engine ou Docker Desktop;
- Docker Compose v2, usado pelo comando `docker compose`;
- Git, caso ainda precise clonar o repositório.

### Execução sem Docker

Além do Docker opcional, instale:

- Java 21;
- Node.js e npm. O build Docker usa Node 22;
- Python 3, somente para usar `run_project.py`.

Confira as ferramentas:

```bash
docker --version
docker compose version
java -version
node --version
npm --version
python3 --version
```

## 2. Entrar na raiz correta

Os comandos abaixo devem ser executados na pasta que contém `backend`,
`frontend`, `docker-compose.yml` e `run_project.py`:

```bash
cd HackGov
ls docker-compose.yml backend frontend run_project.py
```

Se ainda não clonou o projeto:

```bash
git clone <URL_DO_REPOSITORIO>
cd HackGov
```

## 3. Configurar o `.env`

Crie o arquivo local a partir do exemplo somente se ele ainda não existir:

```bash
cp .env.example .env
```

Não repita esse comando sobre um `.env` existente, pois isso pode substituir
suas configurações locais.

Para desenvolvimento com Docker, configure o Mailpit no `.env`:

```env
SPRING_EMAIL_HOST=mailpit
SPRING_EMAIL_PORT=1025
SPRING_EMAIL=
SPRING_EMAIL_PASSWORD=
SPRING_MAIL_SMTP_AUTH=false
SPRING_MAIL_SMTP_STARTTLS_ENABLE=false
SPRING_MAIL_SMTP_STARTTLS_REQUIRED=false
```

A chave da IA é opcional para iniciar a aplicação. Preencha somente se for
testar os recursos que usam o Google Gemini:

```env
GOOGLE_API_KEY=sua-chave-do-google
```

Não coloque senhas reais, chaves de produção ou outros segredos no Git.

## 4. Rodar com Docker (recomendado)

Na raiz do projeto, construa as imagens e inicie os serviços:

```bash
docker compose up -d --build
```

O Compose inicia:

| Serviço | Função | Porta local |
| --- | --- | ---: |
| `frontend` | React compilado servido pelo Nginx | `5173` |
| `backend` | API Spring Boot | `8080` |
| `postgres` | Banco PostgreSQL | `5432` |
| `mailpit` | SMTP e caixa de e-mails local | `1025` e `8025` |

O backend Docker usa o perfil `prod`, PostgreSQL e Flyway. Isso é diferente da
execução local, que usa o perfil `dev` e H2 em memória.

Verifique se os containers subiram:

```bash
docker compose ps
```

O PostgreSQL deve aparecer como `healthy`. Backend e frontend devem aparecer
como `Up`.

Abra a aplicação em <http://localhost:5173>.

Outros endereços úteis:

- API: <http://localhost:8080>;
- Swagger: <http://localhost:8080/swagger-ui.html>;
- Mailpit: <http://localhost:8025>.

No Docker, o Nginx encaminha `/api/` e `/ws` para o backend. Por isso, use a
porta `5173` no navegador; não é necessário apontar o frontend diretamente
para a porta `8080`.

## 5. Dados de demonstração e primeiro login

O Compose habilita dados mock por padrão através de
`HACKGOV_DOCKER_MOCK_DATA_ENABLED=true`. Na primeira inicialização, o backend
carrega cidades, setores, usuários, tarefas, processos e outros dados de
demonstração no PostgreSQL.

Usuários mock para desenvolvimento:

```text
admin@admin.com              / senha123  (administrador do sistema)
admin.sp@prefeitura.gov.br   / senha123  (administrador municipal)
maria@sp.gov.br              / senha123  (funcionária de compras)
joao@sp.gov.br               / senha123  (funcionário de TI)
ana.compras@sp.gov.br        / senha123  (funcionária de compras)
roberto.financeiro@sp.gov.br / senha123  (funcionário financeiro)
fernanda.contratos@sp.gov.br / senha123  (gestora de contratos)
paula.juridico@sp.gov.br     / senha123  (assessora jurídica)
carlos@rj.gov.br             / senha123  (administrador municipal do RJ)
```

Comece por `admin@admin.com` para acessar os recursos administrativos. Depois
teste um usuário de setor para conferir as diferenças de permissões e escopo.
Todas essas credenciais são somente para desenvolvimento. O carregamento dos
mocks é ignorado quando a base já contém o registro de dados inicial.

Para iniciar sem mocks, coloque no `.env`:

```env
HACKGOV_DOCKER_MOCK_DATA_ENABLED=false
```

Nesse caso, será necessário cadastrar ou preparar os dados antes de testar as
áreas autenticadas.

## 6. Logs do Docker

Todos os serviços:

```bash
docker compose logs -f
```

Um serviço específico:

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
docker compose logs -f mailpit
```

Pressione `Ctrl+C` para sair do acompanhamento dos logs. Os containers
continuam executando.

## 7. Parar, iniciar e reconstruir

Parar e remover os containers sem apagar o volume do banco:

```bash
docker compose down
```

Iniciar novamente sem reconstruir as imagens:

```bash
docker compose up -d
```

Depois de alterar código do backend ou frontend:

```bash
docker compose up -d --build
```

Para reconstruir apenas o frontend:

```bash
docker compose up -d --build frontend
```

Depois de alterações visuais, faça um recarregamento forçado no navegador com
`Ctrl+F5`.

## 8. Resetar o banco Docker

O banco é persistido no volume `postgres_data`. Para apagar o banco e criar
uma base limpa:

```bash
docker compose down -v
docker compose up -d --build
```

O `-v` remove o volume local do PostgreSQL, incluindo usuários, cadastros,
migrations aplicadas e dados mock. Use esse comando somente quando quiser
perder os dados locais.

## 9. Rodar sem Docker com `run_project.py`

Primeiro instale as dependências do frontend:

```bash
cd frontend
npm install
cd ..
```

Na raiz do projeto, execute:

```bash
python3 run_project.py
```

O script inicia backend e frontend juntos. Ele:

1. inicia o Spring Boot com o perfil `dev`;
2. usa H2 em memória e desativa o Flyway;
3. aguarda o backend e o carregamento dos mocks;
4. inicia o Vite na porta `5173`;
5. encerra os dois processos quando `Ctrl+C` é pressionado.

As portas `8080` e `5173` precisam estar livres. Os dados do H2 são perdidos
quando o backend é encerrado.

Para usar outra porta no Linux/macOS:

```bash
BACKEND_PORT=8081 python3 run_project.py
```

No PowerShell:

```powershell
$env:BACKEND_PORT="8081"
python run_project.py
```

O script ajusta automaticamente o `VITE_API_URL` do frontend quando essa
variável ainda não foi definida.

## 10. Rodar backend e frontend manualmente

Use dois terminais.

### Terminal 1: backend

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

### Terminal 2: frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend ficará em <http://localhost:5173> e usará a API em
`http://localhost:8080/api`.

Se a API estiver em outra porta:

```bash
VITE_API_URL=http://localhost:8081/api npm run dev
```

## 11. H2 Console no perfil `dev`

Com o backend local em execução, acesse
<http://localhost:8080/h2-console> e use:

```text
JDBC URL: jdbc:h2:mem:testdb
User Name: sa
Password: deixe em branco
```

O H2 existe somente enquanto o backend estiver rodando.

## 12. Validações

Build do frontend:

```bash
cd frontend
npm run build
```

Testes do backend:

```bash
cd backend
./mvnw test
```

Compilar e empacotar o backend sem executar testes:

```bash
cd backend
./mvnw clean package -Dmaven.test.skip=true
```

Esse comando confirma compilação e empacotamento, mas não substitui a suíte de
testes.

Validar o arquivo Compose sem iniciar containers:

```bash
docker compose config
```

Validar whitespace do Git:

```bash
git diff --check
```

## 13. Problemas comuns

### Porta ocupada

Verifique qual processo usa a porta e encerre-o. No Docker, também é possível
alterar o lado esquerdo do mapeamento em `docker-compose.yml`, por exemplo:

```yaml
ports:
  - "8081:8080"
  - "3000:5173"
```

Nesse exemplo, a aplicação passa a ser acessada em `http://localhost:3000` e
a API em `http://localhost:8081`.

### Frontend não acessa a API

Confira o status e os logs:

```bash
docker compose ps
docker compose logs backend
docker compose logs frontend
```

Na execução manual, confirme `VITE_API_URL` e a porta em que o backend foi
iniciado.

### Alteração do frontend não aparece

Reconstrua o frontend e recrie o container:

```bash
docker compose up -d --build frontend
```

Depois use `Ctrl+F5` no navegador.

### Erro no banco ou nas migrations

Consulte os logs do PostgreSQL e do backend. Se for um ambiente descartável,
faça o reset do volume conforme a seção 8.

### IA não responde

Preencha `GOOGLE_API_KEY` no `.env` e reconstrua o backend:

```bash
docker compose up -d --build backend
```

### E-mails não aparecem

No Docker, use `mailpit:1025` como host/porta SMTP e consulte as mensagens em
<http://localhost:8025>. Com o backend fora do Docker, use `localhost:1025`.

## 14. Portas e variáveis principais

| Recurso | Endereço ou valor |
| --- | --- |
| Frontend | <http://localhost:5173> |
| Backend | <http://localhost:8080> |
| Swagger | <http://localhost:8080/swagger-ui.html> |
| H2 Console (`dev`) | <http://localhost:8080/h2-console> |
| Mailpit | <http://localhost:8025> |
| PostgreSQL | `localhost:5432` |
| `SPRING_PROFILES_ACTIVE` | `dev` na execução local |
| `HACKGOV_DOCKER_SPRING_PROFILE` | `prod` no Docker |
| `HACKGOV_DOCKER_MOCK_DATA_ENABLED` | `true` por padrão |
| `VITE_API_URL` | `http://localhost:8080/api` no modo manual |
| `BACKEND_PORT` | `8080` no `run_project.py` |
| `GOOGLE_API_KEY` | vazia por padrão |
| `PASSWORD_EXPIRE_HOURS` | `24` por padrão |

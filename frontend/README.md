# HackGov Frontend React

Este diretório substitui a antiga interface Django por uma aplicação React com Vite.

## Rotas recriadas

- `/` ou `/home`: página inicial
- `/login`: login
- `/register`: cadastro
- `/contato`: contato
- `/dashboard`: painel
- `/ferramentas`: ferramentas
- `/processos`: acompanhamento de processos

## Como rodar

```bash
npm install
npm run dev
```

Por padrão, as chamadas de API usam `http://localhost:8080/api`. Para mudar:

```bash
VITE_API_URL=http://localhost:8080/api npm run dev
```

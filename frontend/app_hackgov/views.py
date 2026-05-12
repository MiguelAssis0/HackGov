from django.shortcuts import render, redirect
from . import services
from .decorators import jwt_login_required
from django.contrib import messages
from django.core.mail import send_mail
from django.conf import settings


def home(request):
    return render(request, 'home.html')


def login_view(request):
    if request.session.get("jwt_token"):
        return redirect("dashboard")

    if request.method == "POST":
        email = request.POST.get("email", "").strip()
        password = request.POST.get("password", "").strip()

        data, erro = services.auth_login(email, password)

        if erro:
            messages.error(request, erro)
            return render(request, "login.html")

        request.session["jwt_token"] = data.get("token") or data.get("accessToken")
        request.session["user"] = {
            "nome": data.get("nome") or data.get("name") or email,
            "cargo": data.get("cargo") or data.get("role") or "",
            "setor": data.get("setor") or "",
        }

        return redirect("dashboard")

    return render(request, "login.html")


def logout_view(request):
    request.session.flush()
    return redirect("login")


# def register_view(request):
#     if request.session.get("jwt_token"):
#         return redirect("dashboard")

#     if request.method == "POST":
#         data = {
#             "nome": request.POST.get("nome", "").strip(),
#             "username": request.POST.get("username", "").strip(),
#             "email": request.POST.get("email", "").strip(),
#             "password": request.POST.get("password", "").strip(),
#             "setor": request.POST.get("setor", "").strip(),
#             "cargo": request.POST.get("cargo", "").strip(),
#         }

#         _, erro = services.auth_register(data)

#         if erro:
#             messages.error(request, erro)
#             return render(request, "register.html")

#         messages.success(request, "Cadastro realizado! Faça login para continuar.")
#         return redirect("login")

#     return render(request, "register.html")


@jwt_login_required
def dashboard(request):
    token = request.session["jwt_token"]
    user = request.session.get("user", {})

    data, erro = services.get_dashboard(token)

    if erro:
        data = {}

    ctx = {
        "user": user,
        "dashboard": data,
        "api_error": erro,
    }
    return render(request, "dashboard.html", ctx)


@jwt_login_required
def processos(request):
    token = request.session["jwt_token"]
    user = request.session.get("user", {})

    page = request.GET.get("page", 1)
    search = request.GET.get("search", "")
    setor = request.GET.get("setor", "")
    status = request.GET.get("status", "")
    processo_id = request.GET.get("id")

    lista, erro_lista = services.get_processos(
        token, page=page, search=search, setor=setor, status=status
    )

    detalhe, erro_detalhe = None, None
    if processo_id:
        detalhe, erro_detalhe = services.get_processo_detalhe(token, processo_id)

    if request.method == "POST":
        novo = {
            "titulo": request.POST.get("titulo"),
            "setor": request.POST.get("setor"),
            "tipo": request.POST.get("tipo"),
            "requisitante": request.POST.get("requisitante"),
            "valor": request.POST.get("valor"),
            "dotacao": request.POST.get("dotacao"),
            "justificativa": request.POST.get("justificativa"),
            "descricao_tecnica": request.POST.get("descricao_tecnica"),
        }
        _, erro = services.criar_processo(token, novo)
        if erro:
            messages.error(request, erro)
        else:
            messages.success(request, "Processo cadastrado com sucesso!")
        return redirect("processos")

    ctx = {
        "user": user,
        "token": token,
        "lista": lista,
        "detalhe": detalhe,
        "processo_id": processo_id,
        "filtros": {"page": page, "search": search, "setor": setor, "status": status},
        "api_error": erro_lista or erro_detalhe,
    }
    return render(request, "processos.html", ctx)

def contato(request):
    if request.method == "POST":
        nome     = request.POST.get("nome", "").strip()
        email    = request.POST.get("email", "").strip()
        assunto  = request.POST.get("assunto", "").strip()
        mensagem = request.POST.get("mensagem", "").strip()
 
        if not all([nome, email, assunto, mensagem]):
            messages.error(request, "Por favor, preencha todos os campos obrigatórios.")
            return render(request, "contato.html")
 
        assuntos_labels = {
            "suporte":  "Suporte técnico",
            "acesso":   "Problema de acesso / login",
            "processo": "Dúvida sobre processos",
            "sugestao": "Sugestão de melhoria",
            "bug":      "Reportar um problema",
            "outro":    "Outro",
        }
        assunto_label = assuntos_labels.get(assunto, assunto)
 
        corpo = (
            f"Nova mensagem recebida pelo formulário de contato do ERP Municipal.\n"
            f"{'-' * 50}\n\n"
            f"Nome:    {nome}\n"
            f"E-mail:  {email}\n"
            f"Assunto: {assunto_label}\n\n"
            f"Mensagem:\n{mensagem}\n"
        )
 
        try:
            send_mail(
                subject=f"[ERP Municipal] {assunto_label} - {nome}",
                message=corpo,
                from_email=settings.DEFAULT_FROM_EMAIL,
                recipient_list=[settings.CONTATO_EMAIL],
                fail_silently=False,
            )
            messages.success(request, "Mensagem enviada com sucesso! Responderemos em breve.")
        except Exception:
            messages.error(request, "Não foi possível enviar a mensagem. Tente novamente mais tarde.")
 
        return redirect("contato")
 
    return render(request, "contato.html")

@jwt_login_required
def ferramentas(request):
    token = request.session["jwt_token"]
    user  = request.session.get("user", {})

    lista_usuarios, erro = services.get_usuarios(token)

    if request.method == "POST" and request.POST.get("acao") == "cadastrar_usuario":
        senha  = request.POST.get("senha", "")
        senha2 = request.POST.get("senha2", "")

        if senha != senha2:
            messages.error(request, "As senhas não coincidem.")
            return redirect("ferramentas")

        dados = {
            "nome":          request.POST.get("nome", "").strip(),
            "username":      request.POST.get("username", "").strip(),
            "email":         request.POST.get("email", "").strip(),
            "cargo":         request.POST.get("cargo", "").strip(),
            "setor":         request.POST.get("setor", "").strip(),
            "perfil":        request.POST.get("perfil", "").strip(),
            "senha":         senha,
            "enviar_email":  bool(request.POST.get("enviar_email")),
        }

        _, erro = services.criar_usuario(token, dados)

        if erro:
            messages.error(request, f"Erro ao cadastrar usuário: {erro}")
        else:
            messages.success(request, f"Usuário '{dados['nome']}' cadastrado com sucesso!")

        return redirect("/ferramentas/?ferramenta=cadastro-usuario")

    ctx = {
        "user":          user,
        "lista_usuarios": lista_usuarios or [],
        "api_error":     erro,
    }
    return render(request, "ferramentas.html", ctx)
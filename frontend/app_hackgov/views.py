from django.shortcuts import render, redirect
from django.contrib import messages
from . import services
from .decorators import jwt_login_required


def home(request):
    return render(request, 'home.html')


def login_view(request):
    if request.session.get("jwt_token"):
        return redirect("dashboard")

    if request.method == "POST":
        username = request.POST.get("username", "").strip()
        password = request.POST.get("password", "").strip()

        data, erro = services.auth_login(username, password)

        if erro:
            messages.error(request, erro)
            return render(request, "login.html")

        request.session["jwt_token"] = data.get("token") or data.get("accessToken")
        request.session["user"] = {
            "nome": data.get("nome") or data.get("name") or username,
            "cargo": data.get("cargo") or data.get("role") or "",
            "setor": data.get("setor") or "",
        }

        return redirect("dashboard")

    return render(request, "login.html")


def logout_view(request):
    request.session.flush()
    return redirect("login")


def register_view(request):
    if request.session.get("jwt_token"):
        return redirect("dashboard")

    if request.method == "POST":
        data = {
            "nome": request.POST.get("nome", "").strip(),
            "username": request.POST.get("username", "").strip(),
            "email": request.POST.get("email", "").strip(),
            "password": request.POST.get("password", "").strip(),
            "setor": request.POST.get("setor", "").strip(),
            "cargo": request.POST.get("cargo", "").strip(),
        }

        _, erro = services.auth_register(data)

        if erro:
            messages.error(request, erro)
            return render(request, "register.html")

        messages.success(request, "Cadastro realizado! Faça login para continuar.")
        return redirect("login")

    return render(request, "register.html")


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
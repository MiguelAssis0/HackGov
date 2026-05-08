import requests
from django.conf import settings

API_URL = settings.SPRINGBOOT_API_URL


def _headers(token=None):
    h = {"Content-Type": "application/json"}
    if token:
        h["Authorization"] = f"Bearer {token}"
    return h


def _get(endpoint, token=None, params=None):
    try:
        r = requests.get(
            f"{API_URL}{endpoint}",
            headers=_headers(token),
            params=params,
            timeout=10,
        )
        r.raise_for_status()
        return r.json(), None
    except requests.exceptions.ConnectionError:
        return None, "Não foi possível conectar ao servidor."
    except requests.exceptions.Timeout:
        return None, "O servidor demorou para responder."
    except requests.exceptions.HTTPError as e:
        return None, f"Erro {r.status_code}: {r.text}"
    except Exception as e:
        return None, str(e)


def _post(endpoint, data, token=None):
    try:
        r = requests.post(
            f"{API_URL}{endpoint}",
            json=data,
            headers=_headers(token),
            timeout=10,
        )
        r.raise_for_status()
        return r.json(), None
    except requests.exceptions.ConnectionError:
        return None, "Não foi possível conectar ao servidor."
    except requests.exceptions.Timeout:
        return None, "O servidor demorou para responder."
    except requests.exceptions.HTTPError:
        try:
            return None, r.json().get("message", r.text)
        except Exception:
            return None, r.text
    except Exception as e:
        return None, str(e)


def auth_login(email, password):
    return _post("/auth/login", {"email": email, "password": password})


# def auth_register(data):
#     return _post("/auth/register", data)


def get_processos(token, page=1, search="", setor="", status=""):
    return _get(
        "/processos",
        token=token,
        params={"page": page, "search": search, "setor": setor, "status": status},
    )


def get_processo_detalhe(token, processo_id):
    return _get(f"/processos/{processo_id}", token=token)


def criar_processo(token, data):
    return _post("/processos", data, token=token)


def get_dashboard(token):
    return _get("/dashboard", token=token)
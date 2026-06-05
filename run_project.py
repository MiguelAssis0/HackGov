import os
import shutil
import signal
import socket
import subprocess
import sys
import threading
import time
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent
BACKEND_DIR = ROOT_DIR / "backend"
FRONTEND_DIR = ROOT_DIR / "frontend"
DEFAULT_BACKEND_PORT = 8080
DEFAULT_FRONTEND_PORT = 5173

IS_WINDOWS = os.name == "nt"


# -----------------------------
# Utilitarios de terminal
# -----------------------------
def print_header(text):
    print(f"\n{'=' * 70}\n{text}\n{'=' * 70}")


def print_step(text):
    print(f"\n-> {text}")


def which(command):
    return shutil.which(command)


def command_exists(command):
    return which(command) is not None


def run_command(command, cwd=None, env=None, check=True):
    print(f"$ {' '.join(map(str, command))}")
    completed = subprocess.run(
        command,
        cwd=cwd,
        env=env,
        shell=False,
        text=True,
    )

    if check and completed.returncode != 0:
        raise RuntimeError(f"Comando falhou com codigo {completed.returncode}: {' '.join(map(str, command))}")

    return completed.returncode


# -----------------------------
# Validacoes e instrucoes
# -----------------------------
def get_port(env_name, default):
    value = os.environ.get(env_name, "").strip()
    if not value:
        return default

    try:
        port = int(value)
    except ValueError:
        print(f"{env_name} precisa ser um numero de porta valido.")
        return None

    if port < 1 or port > 65535:
        print(f"{env_name} precisa estar entre 1 e 65535.")
        return None

    return port


def is_port_in_use(port, host="127.0.0.1"):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.settimeout(0.5)
        return sock.connect_ex((host, port)) == 0


def print_port_help(env_name, example_port):
    if IS_WINDOWS:
        print(f'$env:{env_name}="{example_port}"; python run_project.py')
    else:
        print(f'{env_name}={example_port} python3 run_project.py')


def install_help():
    print_header("Dependencias globais ausentes")
    print("Este projeto precisa de Java JDK e Node.js/npm instalados no sistema.")
    print("O script instala automaticamente as dependencias do projeto, mas nao instala programas globais do sistema sem voce fazer isso antes.")

    if IS_WINDOWS:
        print("\nWindows, usando winget:")
        print("  winget install EclipseAdoptium.Temurin.21.JDK")
        print("  winget install OpenJS.NodeJS.LTS")
        print("\nDepois feche e abra o terminal novamente e rode:")
        print("  python run_project.py")
    else:
        print("\nUbuntu/Debian:")
        print("  sudo apt update")
        print("  sudo apt install -y openjdk-21-jdk nodejs npm")
        print("\nFedora:")
        print("  sudo dnf install -y java-21-openjdk-devel nodejs npm")
        print("\nArch/Manjaro:")
        print("  sudo pacman -S jdk21-openjdk nodejs npm")
        print("\nDepois rode:")
        print("  python3 run_project.py")


def check_global_dependencies():
    missing = []

    # O Maven Wrapper baixa o Maven automaticamente, mas ainda precisa de Java.
    if not command_exists("java"):
        missing.append("Java JDK")

    if not command_exists("node"):
        missing.append("Node.js")

    npm_command = "npm.cmd" if IS_WINDOWS else "npm"
    if not command_exists(npm_command):
        missing.append("npm")

    if missing:
        print(f"Dependencias nao encontradas: {', '.join(missing)}")
        install_help()
        return False

    return True


def ensure_project_structure():
    if not BACKEND_DIR.exists():
        print(f"Pasta backend nao encontrada: {BACKEND_DIR}")
        return False

    if not FRONTEND_DIR.exists():
        print(f"Pasta frontend nao encontrada: {FRONTEND_DIR}")
        return False

    package_json = FRONTEND_DIR / "package.json"
    if not package_json.exists():
        print(f"package.json nao encontrado no frontend: {package_json}")
        return False

    pom_xml = BACKEND_DIR / "pom.xml"
    if not pom_xml.exists():
        print(f"pom.xml nao encontrado no backend: {pom_xml}")
        return False

    return True


def ensure_maven_wrapper():
    mvnw_name = "mvnw.cmd" if IS_WINDOWS else "mvnw"
    mvnw_path = BACKEND_DIR / mvnw_name

    if not mvnw_path.exists():
        print(f"Maven wrapper nao encontrado: {mvnw_path}")
        print("O projeto deveria conter backend/mvnw, backend/mvnw.cmd e backend/.mvn/wrapper.")
        print("Se esses arquivos nao foram commitados, rode em uma maquina com Maven instalado:")
        print("  mvn -N wrapper:wrapper")
        return None

    if not IS_WINDOWS:
        try:
            current_mode = mvnw_path.stat().st_mode
            # Adiciona bits de execucao para usuario/grupo/outros sem remover permissoes existentes.
            mvnw_path.chmod(current_mode | 0o111)
        except OSError as exc:
            print(f"Nao foi possivel aplicar permissao de execucao em {mvnw_path}: {exc}")
            print("Tente manualmente: chmod +x backend/mvnw")
            return None

    return mvnw_path


# -----------------------------
# Bootstrap do frontend
# -----------------------------
def should_run_npm_install():
    node_modules = FRONTEND_DIR / "node_modules"
    package_json = FRONTEND_DIR / "package.json"
    package_lock = FRONTEND_DIR / "package-lock.json"

    if not node_modules.exists():
        return True

    if package_lock.exists() and package_lock.stat().st_mtime > node_modules.stat().st_mtime:
        return True

    if package_json.exists() and package_json.stat().st_mtime > node_modules.stat().st_mtime:
        return True

    return False


def install_frontend_dependencies(npm_command):
    if not should_run_npm_install():
        print_step("Dependencias do frontend ja parecem instaladas.")
        return True

    print_step("Instalando dependencias do frontend.")
    package_lock = FRONTEND_DIR / "package-lock.json"
    command = [npm_command, "ci"] if package_lock.exists() else [npm_command, "install"]

    try:
        run_command(command, cwd=FRONTEND_DIR)
        return True
    except RuntimeError as exc:
        if command[1] == "ci":
            print("npm ci falhou. Tentando npm install como fallback...")
            try:
                run_command([npm_command, "install"], cwd=FRONTEND_DIR)
                return True
            except RuntimeError as fallback_exc:
                print(f"Falha ao instalar dependencias do frontend: {fallback_exc}")
                return False

        print(f"Falha ao instalar dependencias do frontend: {exc}")
        return False


# -----------------------------
# Processos
# -----------------------------
def stream_output(process, prefix, ready_event=None, settled_event=None):
    if process.stdout is None:
        return

    for line in iter(process.stdout.readline, ""):
        if not line:
            break

        print(f"[{prefix}] {line}", end="")

        normalized = line.lower()
        if ready_event and (
            "tomcat started on port" in normalized
            or "netty started on port" in normalized
            or "started " in normalized
            or "started application" in normalized
        ):
            ready_event.set()

        if settled_event and "mocks carregados" in normalized:
            settled_event.set()


def start_process(name, command, cwd, env=None):
    print_step(f"Iniciando {name}...")

    popen_kwargs = {
        "cwd": cwd,
        "env": env,
        "stdout": subprocess.PIPE,
        "stderr": subprocess.STDOUT,
        "text": True,
        "bufsize": 1,
        "shell": False,
    }

    if IS_WINDOWS:
        popen_kwargs["creationflags"] = subprocess.CREATE_NEW_PROCESS_GROUP
    else:
        # Cria um novo grupo de processos para encerrar filhos do Maven/Node com mais confianca.
        popen_kwargs["start_new_session"] = True

    return subprocess.Popen(command, **popen_kwargs)


def stop_process(process, name):
    if process.poll() is not None:
        return

    print(f"\nEncerrando {name}...")

    try:
        if IS_WINDOWS:
            try:
                process.send_signal(signal.CTRL_BREAK_EVENT)
            except Exception:
                process.terminate()
        else:
            try:
                os.killpg(os.getpgid(process.pid), signal.SIGTERM)
            except Exception:
                process.terminate()

        process.wait(timeout=8)
    except Exception:
        try:
            if IS_WINDOWS:
                process.kill()
            else:
                os.killpg(os.getpgid(process.pid), signal.SIGKILL)
        except Exception:
            process.kill()


def wait_for_backend(backend_process, ready_event, settled_event, timeout_seconds=120, settle_timeout_seconds=90):
    print("\nAguardando backend iniciar antes de subir o frontend...\n")

    finished = ready_event.wait(timeout_seconds)
    if not finished:
        if backend_process.poll() is not None:
            print("\nBackend encerrou antes do frontend iniciar.")
            return False

        print("\nNao foi possivel confirmar o backend dentro do tempo limite. Subindo frontend mesmo assim...\n")
        return True

    print("\nBackend iniciou.")

    if settled_event is None:
        return True

    print("Aguardando os mocks terminarem para deixar o link do Vite no final...\n")
    settled = settled_event.wait(settle_timeout_seconds)

    if settled:
        print("\nMocks carregados. Subindo frontend agora...\n")
        return True

    if backend_process.poll() is not None:
        print("\nBackend encerrou antes do frontend iniciar.")
        return False

    print("\nNao foi possivel confirmar o fim dos mocks dentro do tempo limite. Subindo frontend mesmo assim...\n")
    return True


# -----------------------------
# Main
# -----------------------------
def main():
    print_header("Run Project")

    if not ensure_project_structure():
        return 1

    if not check_global_dependencies():
        return 1

    npm_command = "npm.cmd" if IS_WINDOWS else "npm"
    mvnw_path = ensure_maven_wrapper()
    if mvnw_path is None:
        return 1

    if not install_frontend_dependencies(npm_command):
        return 1

    backend_port = get_port("BACKEND_PORT", DEFAULT_BACKEND_PORT)
    if backend_port is None:
        return 1

    frontend_port = get_port("FRONTEND_PORT", DEFAULT_FRONTEND_PORT)
    if frontend_port is None:
        return 1

    if is_port_in_use(backend_port):
        print(f"Porta {backend_port} ja esta em uso. O backend nao pode iniciar nela.")
        print("Feche o processo que esta usando essa porta ou rode em outra porta:")
        print_port_help("BACKEND_PORT", backend_port + 1)
        return 1

    if is_port_in_use(frontend_port):
        print(f"Porta {frontend_port} ja esta em uso. O frontend pode falhar ao iniciar nela.")
        print("Feche o processo que esta usando essa porta ou rode em outra porta:")
        print_port_help("FRONTEND_PORT", frontend_port + 1)
        return 1

    backend_env = os.environ.copy()
    backend_env["SPRING_PROFILES_ACTIVE"] = os.environ.get("SPRING_PROFILES_ACTIVE", "dev")
    backend_env["SERVER_PORT"] = str(backend_port)

    frontend_env = os.environ.copy()
    frontend_env.setdefault("VITE_API_URL", f"http://localhost:{backend_port}/api")
    frontend_env.setdefault("PORT", str(frontend_port))

    processes = []

    try:
        backend_ready = threading.Event()
        backend_settled = threading.Event()

        backend_command = [str(mvnw_path), "spring-boot:run"] if IS_WINDOWS else ["./mvnw", "spring-boot:run"]
        backend_process = start_process(
            "backend Spring Boot",
            backend_command,
            BACKEND_DIR,
            backend_env,
        )
        processes.append((backend_process, "backend"))

        backend_thread = threading.Thread(
            target=stream_output,
            args=(backend_process, "backend", backend_ready, backend_settled),
            daemon=True,
        )
        backend_thread.start()

        if not wait_for_backend(backend_process, backend_ready, backend_settled):
            return backend_process.poll() or 1

        frontend_process = start_process(
            "frontend React/Vite",
            [npm_command, "run", "dev", "--", "--host", "0.0.0.0", "--port", str(frontend_port)],
            FRONTEND_DIR,
            frontend_env,
        )
        processes.append((frontend_process, "frontend"))

        frontend_thread = threading.Thread(
            target=stream_output,
            args=(frontend_process, "frontend"),
            daemon=True,
        )
        frontend_thread.start()

        print("\nProjeto rodando.")
        print(f"Backend:  http://localhost:{backend_port}")
        print(f"Frontend: http://localhost:{frontend_port}")
        print("Pressione Ctrl+C para encerrar backend e frontend.\n")

        while True:
            for process, name in processes:
                exit_code = process.poll()
                if exit_code is not None:
                    print(f"\n{name} encerrou com codigo {exit_code}. Encerrando os demais processos.")
                    return exit_code
            time.sleep(1)

    except KeyboardInterrupt:
        print("\nInterrupcao recebida.")
        return 0
    finally:
        for process, name in reversed(processes):
            stop_process(process, name)


if __name__ == "__main__":
    sys.exit(main())

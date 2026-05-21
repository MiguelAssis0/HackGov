import os
import signal
import socket
import subprocess
import sys
import threading
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent
BACKEND_DIR = ROOT_DIR / "backend"
FRONTEND_DIR = ROOT_DIR / "frontend"
DEFAULT_BACKEND_PORT = 8080


def command_exists(command):
    executable = command[0]
    if os.name == "nt" and not executable.lower().endswith((".exe", ".cmd", ".bat")):
        executable = f"{executable}.cmd"
    return any((Path(path) / executable).exists() for path in os.environ.get("PATH", "").split(os.pathsep))


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


def stream_output(process, prefix, ready_event=None, settled_event=None):
    for line in iter(process.stdout.readline, ""):
        if not line:
            break
        print(f"[{prefix}] {line}", end="")
        if ready_event and ("Tomcat started on port" in line or "Started " in line):
            ready_event.set()
        if settled_event and "Mocks carregados!" in line:
            settled_event.set()


def start_process(name, command, cwd, env=None):
    print(f"Iniciando {name}...")
    creationflags = subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0

    return subprocess.Popen(
        command,
        cwd=cwd,
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
        shell=False,
        creationflags=creationflags,
    )


def stop_process(process, name):
    if process.poll() is not None:
        return

    print(f"\nEncerrando {name}...")
    try:
        if os.name == "nt":
            process.send_signal(signal.CTRL_BREAK_EVENT)
        else:
            process.terminate()
        process.wait(timeout=8)
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

    print("\nBackend iniciou. Aguardando os mocks terminarem para deixar o link do Vite no final...\n")

    settled = settled_event.wait(settle_timeout_seconds)
    if settled:
        print("\nMocks carregados. Subindo frontend agora...\n")
        return True

    if backend_process.poll() is not None:
        print("\nBackend encerrou antes do frontend iniciar.")
        return False

    print("\nNao foi possivel confirmar o fim dos mocks dentro do tempo limite. Subindo frontend mesmo assim...\n")
    return True


def main():
    if not BACKEND_DIR.exists():
        print(f"Pasta backend nao encontrada: {BACKEND_DIR}")
        return 1

    if not FRONTEND_DIR.exists():
        print(f"Pasta frontend nao encontrada: {FRONTEND_DIR}")
        return 1

    npm_command = "npm.cmd" if os.name == "nt" else "npm"
    mvnw_command = "mvnw.cmd" if os.name == "nt" else "./mvnw"
    mvnw_path = BACKEND_DIR / mvnw_command

    if not command_exists([npm_command]):
        print("npm nao encontrado no PATH. Instale o Node.js ou ajuste o PATH.")
        return 1

    if not mvnw_path.exists():
        print(f"Maven wrapper nao encontrado: {mvnw_path}")
        return 1

    backend_port = get_port("BACKEND_PORT", DEFAULT_BACKEND_PORT)
    if backend_port is None:
        return 1

    if is_port_in_use(backend_port):
        print(f"Porta {backend_port} ja esta em uso. O backend nao pode iniciar nela.")
        print("Feche o processo que esta usando essa porta ou rode em outra porta:")
        print('$env:BACKEND_PORT="8081"; python run_project.py')
        return 1

    backend_env = os.environ.copy()
    backend_env["SPRING_PROFILES_ACTIVE"] = "dev"
    backend_env["SERVER_PORT"] = str(backend_port)

    frontend_env = os.environ.copy()
    frontend_env.setdefault("VITE_API_URL", f"http://localhost:{backend_port}/api")

    processes = []

    try:
        backend_ready = threading.Event()
        backend_settled = threading.Event()
        backend_process = start_process(
            "backend Spring Boot (perfil dev)",
            [str(mvnw_path) if os.name == "nt" else mvnw_command, "spring-boot:run"],
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
            "frontend React",
            [npm_command, "run", "dev"],
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

        print("\nProjeto rodando. Pressione Ctrl+C para encerrar backend e frontend.\n")

        while True:
            for process, name in processes:
                exit_code = process.poll()
                if exit_code is not None:
                    print(f"\n{name} encerrou com codigo {exit_code}. Encerrando os demais processos.")
                    return exit_code
            threading.Event().wait(1)

    except KeyboardInterrupt:
        print("\nInterrupcao recebida.")
        return 0
    finally:
        for process, name in reversed(processes):
            stop_process(process, name)


if __name__ == "__main__":
    sys.exit(main())

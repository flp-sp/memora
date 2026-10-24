import platform
import shutil
import subprocess
from pathlib import Path
from urllib.parse import urlparse, parse_qs, unquote

def dot_env_gen(url, overwrite: bool = False):
    PATH = ".env"

    if not url.startswith("jdbc:postgresql://"):
        raise ValueError("Url inválida")
    
    p = urlparse(url[len("jdbc:"):])
    params = parse_qs(p.query)

    user = params.get("user", [unquote(p.username or "")])[0]
    passwd = params.get("password", [unquote(p.password or "")])[0]

    if not p.hostname or not p.path.strip("/"):
        raise ValueError("URL sem host ou banco.")
    if not user or not passwd:
        raise ValueError("URL sem user/password.")

    port = p.port or 5432
    base = p.path.lstrip("/") 

    lines = [
        f"DB_URL=jdbc:postgresql://{p.hostname}:{port}/{base}?sslmode=require",
        f"DB_USER={user}",
        f"DB_PASS={passwd}",
    ]

    file = Path(PATH)
    file.parent.mkdir(parents=True, exist_ok=True)
    with open(file, "w" if overwrite else "x", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")

def manual_env_gen(host, port, base, user, passwd, overwrite = False):
    PATH = ".env"
    lines = [
        f"DB_URL=jdbc:postgresql://{host}:{port}/{base}?sslmode=require",
        f"DB_USER={user}",
        f"DB_PASS={passwd}",
    ]

    file = Path(PATH)
    file.parent.mkdir(parents=True, exist_ok=True)
    with open(file, "w" if overwrite else "x", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")

def compile_command(task):
        ROOT = Path(__file__).resolve().parent.parent
        IS_WINDOWS = platform.system() == "Windows"
        GRADLEW = "gradlew.bat" if IS_WINDOWS else "./gradlew"
        OUT_BASE = ROOT / "build" / "compose" / "binaries" / "main"

        try:
            result = subprocess.run([GRADLEW, task], cwd=ROOT, shell=IS_WINDOWS)
        except FileNotFoundError:
            print(f"{GRADLEW} não encontrado em {ROOT}. Rode o setup na raiz do projeto.")
            return
        if result.returncode == 0:
            print(f"\nPronto! Arquivos em: {OUT_BASE}")
        else:
            print("\nO build falhou. Veja a mensagem do Gradle acima.")

print("""
===================================
Bem vindo ao Quick Setup do Memora!
===================================
""")
match input("Insira como quer cadastrar o banco:\n1) Url(Neon)\n2) Inserir credenciais\n"):
    case "1":
        url = input("Insira a URL: ")
        try:
            dot_env_gen(url)
        except FileExistsError:
            if input("Arquivo já existe, deseja sobrescrever? (s/N): ").lower() == "s":
                dot_env_gen(url, True)
    case "2":
        host = input("Insira o host: ")
        port = input("Insira a porta: (Padrão 5432) ")
        base = input("Insira o banco de dados: ")
        user = input("Insira o usuário: ")
        passwd = input("Insira a senha: ")

        try:
            manual_env_gen(host, port, base, user, passwd)
        except FileExistsError:
            if input("Arquivo já existe, deseja sobrescrever? (s/N): ").lower() == "s":
                manual_env_gen(host, port, base, user, passwd, True)

    case _:
        print("Opção não encontrada, tente novamente.")

match input("Insira o executável desejado:\n1) Windows(EXE)\n2) Debian\n3) Fedora\n4) Linux(AppImage)\n"):
        case "1":
            compile_command("packageExe")
        case "2":
            compile_command("packageDeb")
        case "3":
            compile_command("packageRpm")
        case "4":
            compile_command("packageAppImage")
        case _:
            print("Opção não encontrada, tente novamente.")
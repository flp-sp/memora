import os
import shutil
import subprocess
import sys
from datetime import datetime
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parent
ENV_PATH = ROOT / ".env"
BACKUP_DIR = ROOT / "backup"
KEEP = 28
LOG_RETENTION_DAYS = 7


def load_env(path: Path) -> dict:
    env = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            env[k.strip()] = v.strip()
    return env


def purge_old_logs(host, port, db, user, proc_env) -> None:
    """Apaga da tabela logs os registros com mais de LOG_RETENTION_DAYS dias."""
    if shutil.which("psql") is None:
        print("psql não encontrado, limpeza de logs ignorada. "
              "Ubuntu: sudo apt install postgresql-client-18", file=sys.stderr)
        return

    sql = (f"DELETE FROM logs "
           f"WHERE date_time < now() - interval '{int(LOG_RETENTION_DAYS)} days';")
    cmd = [
        "psql",
        "-h", host,
        "-p", str(port),
        "-U", user,
        "-d", db,
        "--no-psqlrc",
        "-v", "ON_ERROR_STOP=1",
        "-c", sql,
    ]
    result = subprocess.run(cmd, env=proc_env, capture_output=True, text=True)
    if result.returncode != 0:
        # o backup já foi salvo; só avisa que a limpeza falhou
        print(f"Limpeza de logs falhou:\n{result.stderr}", file=sys.stderr)
        return
    print(f"Limpeza de logs: {result.stdout.strip()}")   # ex.: DELETE 42


def main() -> int:
    if shutil.which("pg_dump") is None:
        print("pg_dump não encontrado. Ubuntu: sudo apt install postgresql-client-18")
        return 1

    env = load_env(ENV_PATH)
    p = urlparse(env["DB_URL"][len("jdbc:"):])
    passwd = env.get("DB_PASSWORD") or env.get("DB_PASS", "")
    host = p.hostname
    port = p.port or 5432
    db = p.path.lstrip("/")
    user = env["DB_USER"]

    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    for t in BACKUP_DIR.glob("memora_*.tmp"):
        t.unlink(missing_ok=True)

    stamp = datetime.now().strftime("%Y-%m-%d_%H-%M")
    final = BACKUP_DIR / f"memora_{stamp}.dump"
    tmp = final.with_suffix(".tmp")

    cmd = [
        "pg_dump",
        "-h", host,
        "-p", str(port),
        "-U", user,
        "-d", db,
        "-Fc",
        "--no-owner",
        "-f", str(tmp),
    ]
    proc_env = {**os.environ, "PGPASSWORD": passwd, "PGSSLMODE": "require"}

    result = subprocess.run(cmd, env=proc_env, capture_output=True, text=True)
    if result.returncode != 0:
        tmp.unlink(missing_ok=True)
        print(f"Backup falhou:\n{result.stderr}", file=sys.stderr)
        return 1

    tmp.rename(final)
    print(f"Backup salvo: {final}")

    for old in sorted(BACKUP_DIR.glob("memora_*.dump"))[:-KEEP]:
        old.unlink()

    purge_old_logs(host, port, db, user, proc_env)
    return 0


if __name__ == "__main__":
    sys.exit(main())
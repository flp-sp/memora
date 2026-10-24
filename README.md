# Memora

## Objetivo
Aplicativo para registro e acompanhamento de atendimentos a presos, voltado a equipes de advocacia. A aplicação permite cadastrar atendimentos vinculados a cada preso, consultá-los por número de processo ou nome, filtrá-los por data, e gerar o histórico de registros dos presos selecionados.

O acesso é controlado por perfis (ADMIN, USER e VIEWER(Ainda não implementado)): novos usuários só entram mediante e-mail previamente autorizado por um administrador, e a edição de registros é restrita ao autor e aos administradores. O sistema também realiza backups automáticos do banco de dados, garantindo a preservação das informações.

## Stack
- UI: A interface de usuário usa `Kotlin` + `Compose`
- Backend: Toda a lógica do sistema é feita com `Java 21`
- Database: O banco de dados usado é o `PostgreSQL` e está hospedado no `neon`
- Backup: Um script feito com `Python` realizará um `pg_dump` periodicamente

## Como rodar
Recomenda-se rodar a aplicação hospedando o banco de dados no `Neon`, pois foi onde a aplicação foi desenvolvida nativamente.

Usuario e senha padrões são:  
```
admin@local.net  
admin123
```

### Quick setup
No seu terminal use:
```bash
python scripts/setup.py
```
Esse script ajuda a configurar o banco de dados no projeto e compilar o app 

### Caso queira rodar manualmente:
Primeiro crie um arquivo `.env` na raiz do projeto
```bash
DB_URL=
DB_USER=
DB_PASS=
```

Depois você pode gerar um executável de acordo com o seu sistema operacional
```bash
# Windows
.\gradlew.bat packageExe

# Debian/Ubuntu
./gradlew packageDeb

# Fedora/RHEL
./gradlew packageRpm

# AppImage
./gradlew packageAppImage
```

### Configuração no executável instalado

O arquivo `.env` da raiz do projeto é incluído no pacote durante o build. Ele precisa existir antes de gerar o pacote. As variáveis de ambiente `DB_URL`, `DB_USER` e `DB_PASS` têm prioridade e podem ser usadas para sobrescrever os valores embutidos.

Ao alterar as credenciais, gere o pacote novamente.

Se quiser rodar direto sem compilar
```bash
# Windows
.\gradlew.bat run

# Unix
./gradlew run
```

## Backup
O backup funciona com um serviço em um servidor que roda um script em `python` nas horas selecionadas(12 e 00), e gera arquivos `.dump` do Postgre.
> [!NOTE]  
> O backup foi pensado apenas para servidores Linux, mais especificamente para Ubuntu Server.
### Para configurar siga as instruções: 
```bash 
# Verifique o fuso horário e concerte se necessário
timedatectl
sudo timedatectl set-timezone America/Fortaleza

# Dependencias
sudo apt update
sudo apt install -y python3 postgresql-common
```
Garanta que a versão do `Postgre` do `Neon` é igual a sua
```bash
sudo /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh -y
sudo apt install -y postgresql-client-18 # Versão atual da data desse commit

# Confirme
pg_dump --version
```

### Agora vamos criar o serviço
```bash
sudo nano /etc/systemd/system/memora-backup.service
```
```service
[Unit]
Description=Backup do banco Memora (pg_dump)
After=network-online.target
Wants=network-online.target

[Service]
Type=oneshot
User=SEU_USUARIO
WorkingDirectory=DIRETORIO_BACKUP
ExecStart=/usr/bin/python3 DIRETORIO_BACKUP/backup.py
TimeoutStartSec=30min
Nice=10
```
> [!WARNING]  
> Lembre de trocar `SEU_USUARIO` para o seu usuário do servidor e `DIRETORIO_BACKUP` para o local onde vai ficar seu script.

### Por fim vamos criar o timer
```bash
sudo nano /etc/systemd/system/memora-backup.timer
```
```timer
[Unit]
Description=Backup Memora

[Timer]
OnCalendar=*-*-* 00,12:00:00
Persistent=true
RandomizedDelaySec=60

[Install]
WantedBy=timers.target
```
> [!TIP]  
> Você pode configurar os horários do seu backup aqui.

### Caso queira pode checar se funcionou
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now memora-backup.timer

# conferir o próximo disparo
systemctl list-timers memora-backup.timer

# disparar um backup agora, para testar o service
sudo systemctl start memora-backup.service

# ver o resultado
systemctl status memora-backup.service
journalctl -u memora-backup.service -n 50 --no-pager
```

### Comandos uteis
```bash
journalctl -u memora-backup.service -f # acompanhar status atual
sudo systemctl stop memora-backup.timer # pausar o timer
sudo systemctl disable --now memora-backup.timer # desativar o timer
``` 
## Tabelas
### User
|Valor|Tipo|Constraints|Obs|
|-|-|-|-|
|id|SERIAL|PK||
|nome|VARCHAR(150)|NOT NULL||
|email|VARCHAR(255)|NOT NULL UNIQUE FK|FK para Whitelisted User|
|senha_hash|TEXT|NOT NULL||

### Whitelisted User
|Valor|Tipo|Constraints|Obs|
|-|-|-|-|
|email|VARCHAR(255)|PK||
|status_atual|CHAR|NOT NULL|A - activated (já criou conta), C - cancelled (não pode mais logar), O - open (pode se cadastrar com o email)|
|cargo|CHAR|NOT NULL|A - ADMIN, U - USER, V - VIEWER|

### Preso
|Valor|Tipo|Constraints|Obs|
|-|-|-|-|
|id|SERIAL|PK||
|numero_processo|VARCHAR(25)|NOT NULL|0000000-00.0000.8.10.0060|
|nome|VARCHAR(150)|NOT NULL||
|data_direito|DATE|NOT NULL||
|tipo|CHAR|NOT NULL|S - Progressão semiaberto, A - Progressão aberto, L - Livramento condicional|
|data_ultima_analise|DATE|NULL||
|data_ultimo_atendimento|DATE|NULL||
|data_transferencia_apac|DATE|NULL|Apenas para apac|
|presidio|CHAR|NOT NULL|A - APAC, U - UPTIM|
|pendencia_administrativa|VARCHAR(25)|NOT NULL|SEM PENDÊNCIA, DOCUMENTO, PEDIDO, PARECER, DECISÃO|

### Registro
|Valor|Tipo|Constraints|Obs|
|-|-|-|-|
|id|SERIAL|PK||
|id_user|INT|NOT NULL FK||
|id_preso|INT|NOT NULL FK||
|data_hora|TIMESTAMP|NOT NULL||
|descricao|TEXT|NOT NULL||
|observacoes|TEXT|NULL|pendencias|
|id_user_edit|INT|NULL|Último usuário que editou o registro|

### Logs
|Valor|Tipo|Constraints|Obs|
|-|-|-|-|
|id|SERIAL|PK||
|id_user|INT|NOT NULL FK||
|id_preso|INT|NULL FK||
|email_whitelisted|VARCHAR(255)|NULL FK||
|descricao|VARCHAR(50)|NOT NULL||
|data_hora|TIMESTAMP|NOT NULL||
|id_registro|INT|NULL FK|ON DELETE SET NULL|
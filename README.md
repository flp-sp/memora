# Memora

## Objetivo
Aplicativo para registro e acompanhamento de atendimentos a presos, voltado a equipes de advocacia. A aplicação permite cadastrar atendimentos vinculados a cada preso, consultá-los por número de processo ou nome, filtrá-los por presídio (APAC ou UPTIM) e data, e gerar o histórico de registros dos presos selecionados.

O acesso é controlado por perfis (ADMIN, USER e VIEWER): novos usuários só entram mediante e-mail previamente autorizado por um administrador, e a edição de registros é restrita ao autor e aos administradores. O sistema também realiza backups automáticos do banco de dados, garantindo a preservação das informações.

## Stack
- UI: A interface de usuário usa `Kotlin` + `Compose`
- Backend: Toda a lógica do sistema é feita com `Java`
- Database: O banco de dados usado é o `PostgreSQL` e está hospedado no `neon`
- Backup: Um script feito com `Python` realizará um `pg_dump` periodicamente

## Tabelas
### User

| Valor      | Tipo         | Constraints        | Obs                           |
| ---------- | ------------ | ------------------ | ------------------------------- |
| id         | SERIAL       | PK                 |                                 |
| nome       | VARCHAR(150) | NOT NULL           |                                 |
| email      | VARCHAR(255) | NOT NULL UNIQUE FK |                                 |
| senha_hash | TEXT         | NOT NULL           |                                 |

### Whitelisted User
| Valor  | Tipo         | Constraints | Obs                                 |
| ------ | ------------ | ----------- | ------------------------------------- |
| email  | VARCHAR(255) | PK          |                                       |
| current_status | CHAR         | NOT NULL    | A- activated, C - cancelled, O - open |
| cargo   | CHAR  | NOT NULL    | A - ADMIN, U - USER, V - VIEWER                                      |
### Preso
| Valor                    | Tipo         | Constraints | Obs                                                                        |
| ------------------------ | ------------ | ----------- | ---------------------------------------------------------------------------- |
| id                       | SERIAL       | PK          |                                                                              |
| numero_processo          | VARCHAR(25)  | NOT NULL    | 0000000-00.0000.8.10.0060                                                    |
| nome                     | VARCHAR(150) | NOT NULL    |                                                                              |
| beneficio_vencido | BOOLEAN         | NOT NULL    |                                                                         |
| data_direito             | DATE         | NOT NULL    |                                                                              |
| tipo                     | CHAR         | NOT NULL    | S - Progressao semiaberto, A - Progressão aberto, L - Livramento condicional |
| date_ultima_analise      | DATE         | NOT NULL    |                                                                              |
| date_ultimo_atendimento  | DATE         | NOT NULL    |                                                                              |
| date_transferencia_apac  | DATE         | NULL        | Apenas para apac                                                             |
| presidio                 | CHAR         | NOT NULL    | A - APAC, U - UPTIM                                                          |

### Registro
| Valor       | Tipo   | Constraints | Obs      |
| ----------- | ------ | ----------- | ---------- |
| id          | SERIAL | PK          |            |
| id_user     | INT    | FK          |            |
| id_preso    | INT    | FK          |            |
| data        | DATE   | NOT NULL    |            |
| descrição   | TEXT   | NOT NULL    |            |
| observacoes | TEXT   | NULL        | pendencias |

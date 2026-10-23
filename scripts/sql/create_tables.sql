-- Whitelisted user
CREATE TABLE whitelisted(
email VARCHAR(255) PRIMARY KEY,
current_status CHAR(1) NOT NULL CHECK(current_status IN('A', 'C', 'O')),
sys_role CHAR(1) NOT NULL CHECK(sys_role IN('A', 'U', 'V'))
);

-- User
CREATE TABLE users(
id SERIAL PRIMARY KEY,
user_name VARCHAR(150) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
pass_hash TEXT NOT NULL,

FOREIGN KEY(email) REFERENCES whitelisted(email)
);

-- Preso
CREATE TABLE prisoner(
id SERIAL PRIMARY KEY,
lawsuit_number VARCHAR(25) NOT NULL,
prisoner_name VARCHAR(150) NOT NULL,
duty_date DATE NOT NULL,
sentence_type CHAR(1) NOT NULL CHECK (sentence_type IN('S', 'A','L')),
date_last_analysis DATE NULL,
date_last_service DATE NULL,
date_transfer_apac DATE NULL,
jail CHAR(1) NOT NULL CHECK(jail IN('A', 'U')),
administrative_pending VARCHAR(25) NOT NULL
);

-- Registro
CREATE TABLE register(
    id SERIAL PRIMARY KEY,
    id_user INT NOT NULL,
    id_prisoner INT NOT NULL,
    date_time_register TIMESTAMP NOT NULL,
    description TEXT NOT NULL,
    observations TEXT NULL,
    id_user_edit INT NULL,

    FOREIGN KEY(id_user) REFERENCES users(id),
    FOREIGN KEY(id_prisoner) REFERENCES prisoner(id) ON DELETE CASCADE
);

-- Logs
CREATE TABLE logs(
    id SERIAL PRIMARY KEY,
    id_user INT NOT NULL,
    id_prisoner INT NULL,
    whitelisted_email VARCHAR(255) NULL,
    description VARCHAR(50) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    id_register INT NULL,

    FOREIGN KEY(id_user) REFERENCES users(id),
    FOREIGN KEY(id_prisoner) REFERENCES prisoner(id) ON DELETE SET NULL,
    FOREIGN KEY(whitelisted_email) REFERENCES whitelisted(email),
    FOREIGN KEY(id_register) REFERENCES register(id) ON DELETE SET NULL
);

-- Indices
CREATE INDEX idx_register_user ON register (id_user, date_time_register DESC);
CREATE INDEX idx_register_prisoner ON register (id_prisoner, date_time_register DESC);
CREATE INDEX idx_logs_id_register ON logs(id_register);
CREATE TABLE IF NOT EXISTS users (
    id            uuid PRIMARY KEY,
    username      varchar(255) NOT NULL UNIQUE,
    email         varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    sex           varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS buyers (
    user_id uuid PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sellers (
    user_id uuid PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE
);

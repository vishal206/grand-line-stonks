CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    balance    NUMERIC(19,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

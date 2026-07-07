CREATE TABLE markets (
    id                      BIGSERIAL PRIMARY KEY,
    question                VARCHAR(300)  NOT NULL,
    status                  VARCHAR(20)   NOT NULL,
    liquidity               NUMERIC(19,4) NOT NULL CHECK (liquidity > 0),
    created_by              BIGINT        NOT NULL REFERENCES users (id),
    market_maker_account_id BIGINT UNIQUE REFERENCES accounts (id),
    created_at              TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE market_outcomes (
    id        BIGSERIAL PRIMARY KEY,
    market_id BIGINT        NOT NULL REFERENCES markets (id),
    idx       INT           NOT NULL,
    label     VARCHAR(100)  NOT NULL,
    shares    NUMERIC(19,4) NOT NULL DEFAULT 0,
    UNIQUE (market_id, idx)
);

CREATE TABLE positions (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT        NOT NULL REFERENCES users (id),
    market_id  BIGINT        NOT NULL REFERENCES markets (id),
    outcome_id BIGINT        NOT NULL REFERENCES market_outcomes (id),
    shares     NUMERIC(19,4) NOT NULL DEFAULT 0,
    spent      NUMERIC(19,4) NOT NULL DEFAULT 0,
    UNIQUE (user_id, outcome_id)
);

CREATE TABLE bets (
    id             BIGSERIAL PRIMARY KEY,
    market_id      BIGINT        NOT NULL REFERENCES markets (id),
    outcome_id     BIGINT        NOT NULL REFERENCES market_outcomes (id),
    user_id        BIGINT        NOT NULL REFERENCES users (id),
    transaction_id BIGINT        NOT NULL UNIQUE REFERENCES transactions (id),
    amount         NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    shares         NUMERIC(19,4) NOT NULL CHECK (shares > 0),
    prices_after   JSONB         NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX market_outcomes_market_id_idx ON market_outcomes (market_id);
CREATE INDEX positions_user_id_idx ON positions (user_id);
CREATE INDEX positions_market_id_idx ON positions (market_id);
CREATE INDEX bets_market_id_created_at_idx ON bets (market_id, created_at);

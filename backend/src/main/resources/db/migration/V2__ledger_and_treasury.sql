ALTER TABLE accounts ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE accounts ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE accounts ALTER COLUMN type DROP DEFAULT;
-- The treasury mints user berries under zero-sum double entry, so it alone may hold a negative balance.
ALTER TABLE accounts ADD CONSTRAINT accounts_balance_non_negative CHECK (type = 'TREASURY' OR balance >= 0);

CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(30)  NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
    id             BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT        NOT NULL REFERENCES transactions (id),
    account_id     BIGINT        NOT NULL REFERENCES accounts (id),
    amount         NUMERIC(19,4) NOT NULL CHECK (amount <> 0),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ledger_entries_account_id_idx ON ledger_entries (account_id);
CREATE INDEX ledger_entries_transaction_id_idx ON ledger_entries (transaction_id);

INSERT INTO accounts (user_id, balance, type) VALUES (NULL, 0, 'TREASURY');

DO $$
DECLARE
    total       NUMERIC(19,4);
    treasury_id BIGINT;
    tx_id       BIGINT;
BEGIN
    SELECT COALESCE(SUM(balance), 0) INTO total FROM accounts WHERE type = 'USER';
    IF total <> 0 THEN
        SELECT id INTO treasury_id FROM accounts WHERE type = 'TREASURY';
        INSERT INTO transactions (type) VALUES ('MINT') RETURNING id INTO tx_id;
        INSERT INTO ledger_entries (transaction_id, account_id, amount)
            SELECT tx_id, id, balance FROM accounts WHERE type = 'USER' AND balance <> 0;
        INSERT INTO ledger_entries (transaction_id, account_id, amount) VALUES (tx_id, treasury_id, -total);
        UPDATE accounts SET balance = -total WHERE id = treasury_id;
    END IF;
END $$;

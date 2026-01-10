-- V3__create_transactions_table.sql
-- Flyway migration: create transactions table

DROP TABLE IF EXISTS transactions;

CREATE TABLE transactions
(
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT         NOT NULL,
    wallet_id             BIGINT         NOT NULL,
    transaction_type      VARCHAR(10)    NOT NULL,
    transaction_category  VARCHAR(20)    NOT NULL,
    status                VARCHAR(10)    NOT NULL,
    amount                NUMERIC(19, 2) NOT NULL,
    balance_before        NUMERIC(19, 2) NOT NULL,
    balance_after         NUMERIC(19, 2) NOT NULL,
    idempotency_key       VARCHAR(100)   NOT NULL UNIQUE,
    reference_id          VARCHAR(100)   NOT NULL UNIQUE,
    description           VARCHAR(255),
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_tx_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id),
    CONSTRAINT chk_tx_amounts
        CHECK (
            amount > 0
            AND balance_before >= 0
            AND balance_after >= 0
        )
);

CREATE INDEX idx_tx_user_id ON transactions (user_id);
CREATE INDEX idx_tx_wallet_id ON transactions (wallet_id);
CREATE INDEX idx_tx_category ON transactions (transaction_category);
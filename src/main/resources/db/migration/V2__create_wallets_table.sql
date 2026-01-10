-- V2__create_wallets_table.sql
-- Flyway migration: create wallets table

CREATE TABLE wallets
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT         NOT NULL UNIQUE,
    balance        NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    locked_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_wallet_balances
        CHECK (
            balance >= 0
            AND locked_balance >= 0
            AND balance >= locked_balance
        )
);
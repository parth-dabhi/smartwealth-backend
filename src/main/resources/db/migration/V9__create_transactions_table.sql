CREATE TABLE transactions
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         NOT NULL,
    wallet_id        BIGINT         NOT NULL,
    transaction_type VARCHAR(10)    NOT NULL,
    status           VARCHAR(10)    NOT NULL,
    amount           NUMERIC(19, 2) NOT NULL,
    idempotency_key  VARCHAR(100)   NOT NULL,
    reference_id     VARCHAR(100)   NOT NULL,
    description      VARCHAR(255),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_tx_idempotency_key
        UNIQUE (idempotency_key),
    CONSTRAINT uk_tx_reference_id
        UNIQUE (reference_id),
    CONSTRAINT fk_tx_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_tx_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallets (id)
);

-- indexes
CREATE INDEX idx_tx_user_id ON transactions (user_id);
CREATE INDEX idx_tx_wallet_id ON transactions (wallet_id);
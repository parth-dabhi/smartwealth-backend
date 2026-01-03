CREATE TABLE wallets
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    balance        NUMERIC(19, 2) NOT NULL,
    locked_balance NUMERIC(19, 2) NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_wallet_user
        UNIQUE (user_id),
    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);
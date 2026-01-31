CREATE TABLE holding_transactions
(
    holding_txn_id      BIGSERIAL PRIMARY KEY,

    holding_id          BIGINT         NOT NULL,
    investment_order_id BIGINT,

    txn_type    VARCHAR(20)    NOT NULL, -- BUY | SELL

    units               NUMERIC(18, 8) NOT NULL,
    amount              NUMERIC(14, 2) NOT NULL,
    nav                 NUMERIC(12, 4) NOT NULL,
    nav_date            DATE           NOT NULL,

    txn_date            DATE           NOT NULL,

    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT fk_holding_txn_holding
        FOREIGN KEY (holding_id)
            REFERENCES user_holdings (holding_id),

    CONSTRAINT fk_holding_txn_order
        FOREIGN KEY (investment_order_id)
            REFERENCES investment_order (investment_order_id)
);

CREATE INDEX IF NOT EXISTS idx_holding_txn_holding
    ON holding_transactions (holding_id);

CREATE INDEX IF NOT EXISTS idx_holding_txn_order
    ON holding_transactions (investment_order_id);
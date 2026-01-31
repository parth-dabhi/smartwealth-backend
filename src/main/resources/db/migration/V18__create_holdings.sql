CREATE TABLE user_holdings
(
    holding_id      BIGSERIAL PRIMARY KEY,

    user_id         BIGINT         NOT NULL,
    plan_id         INTEGER        NOT NULL,

    -- Current holding state
    total_units     NUMERIC(18, 8) NOT NULL DEFAULT 0,

    -- Monetary aggregates (simple model)
    total_invested_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_redeemed_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,

    -- Soft state
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT uq_user_plan UNIQUE (user_id, plan_id),

    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_id FOREIGN KEY (plan_id) REFERENCES scheme_plans (plan_id) ON DELETE CASCADE
);

CREATE INDEX idx_user_holdings_user ON user_holdings (user_id);
CREATE INDEX idx_user_holdings_plan ON user_holdings (plan_id);
CREATE TABLE IF NOT EXISTS investment_order
(
    investment_order_id BIGSERIAL PRIMARY KEY,

    user_id        BIGINT      NOT NULL,
    plan_id        INTEGER     NOT NULL,
    sip_mandate_id BIGINT, -- NULL for LUMPSUM, NOT NULL for SIP executions

    investment_type VARCHAR(20) NOT NULL, -- BUY | SELL
    investment_mode VARCHAR(20) NOT NULL, -- LUMPSUM | SIP

    amount          NUMERIC(14,2) NOT NULL,
    units           NUMERIC(18, 6),

    applicable_nav_date DATE      NOT NULL,

    status          VARCHAR(20)   NOT NULL, -- PENDING | ALLOTTED | FAILED | CANCELLED

    payment_status      VARCHAR(20) NOT NULL,
    payment_reference_id varchar(100) NOT NULL,

    order_time      TIMESTAMPTZ   NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),
    CONSTRAINT fk_order_plan
        FOREIGN KEY (plan_id)
            REFERENCES scheme_plans (plan_id),
    CONSTRAINT fk_order_sip
        FOREIGN KEY (sip_mandate_id)
            REFERENCES sip_mandate (sip_mandate_id),
    CONSTRAINT fk_investment_order_payment_reference
            FOREIGN KEY (payment_reference_id)
                REFERENCES transactions(reference_id)
);

CREATE INDEX IF NOT EXISTS idx_order_user
    ON investment_order (user_id);

CREATE INDEX IF NOT EXISTS idx_order_plan
    ON investment_order (plan_id);

CREATE INDEX IF NOT EXISTS idx_order_sip
    ON investment_order (sip_mandate_id);

CREATE INDEX IF NOT EXISTS idx_order_status
    ON investment_order (status);

CREATE INDEX IF NOT EXISTS idx_order_nav_date
    ON investment_order (applicable_nav_date);

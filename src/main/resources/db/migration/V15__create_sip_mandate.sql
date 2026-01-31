CREATE TABLE IF NOT EXISTS sip_mandate
(
    sip_mandate_id BIGSERIAL PRIMARY KEY,

    user_id        BIGINT        NOT NULL,
    plan_id        INTEGER       NOT NULL,

    sip_amount     NUMERIC(12, 2) NOT NULL,
    sip_day        SMALLINT      NOT NULL CHECK (sip_day BETWEEN 1 AND 28),

    start_date     DATE          NOT NULL,
    end_date       DATE,

    total_installments INT NOT NULL CHECK (total_installments > 0),
    completed_installments INT   NOT NULL DEFAULT 0,

    next_run_at    TIMESTAMPTZ,
    last_run_at    TIMESTAMPTZ,

    status         VARCHAR(20)   NOT NULL,
    -- ACTIVE | PAUSED | CANCELLED | COMPLETED

    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_installments_valid
        CHECK (completed_installments <= total_installments),

    CONSTRAINT fk_sip_mandate_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_sip_mandate_plan
        FOREIGN KEY (plan_id)
            REFERENCES scheme_plans (plan_id)
);

CREATE INDEX IF NOT EXISTS idx_sip_mandate_user
    ON sip_mandate (user_id);

CREATE INDEX IF NOT EXISTS idx_sip_mandate_plan
    ON sip_mandate (plan_id);

CREATE INDEX IF NOT EXISTS idx_sip_mandate_next_run
    ON sip_mandate (next_run_at);

CREATE INDEX IF NOT EXISTS idx_sip_mandate_status
    ON sip_mandate (status);

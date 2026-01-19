CREATE TABLE IF NOT EXISTS scheme_plans
(
    plan_id        INTEGER PRIMARY KEY,
    scheme_id      INTEGER      NOT NULL,
    isin           VARCHAR(20)  NOT NULL UNIQUE,
    plan_name      VARCHAR(255) NOT NULL,
    scheme_code    INTEGER,
    plan_type      VARCHAR(50)  NOT NULL,
    option_type    VARCHAR(50)  NOT NULL,

    expense_ratio  NUMERIC(5, 2),
    min_investment NUMERIC(12, 2),
    min_sip        NUMERIC(12, 2),

    return_1y      NUMERIC(10, 4),
    return_3y      NUMERIC(10, 4),
    return_5y      NUMERIC(10, 4),

    is_sip_allowed BOOLEAN      NOT NULL DEFAULT FALSE,
    exit_load      TEXT,
    is_recommended BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT fk_plan_scheme
        FOREIGN KEY (scheme_id)
            REFERENCES mutual_fund_schemes (scheme_id)
);

CREATE INDEX idx_scheme_plans_scheme_id ON scheme_plans (scheme_id);
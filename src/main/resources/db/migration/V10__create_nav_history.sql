CREATE TABLE IF NOT EXISTS nav_history
(
    nav_id     BIGSERIAL PRIMARY KEY,
    plan_id    INTEGER        NOT NULL,
    nav_date   DATE           NOT NULL,
    nav_value  NUMERIC(12, 4) NOT NULL,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT fk_nav_plan
        FOREIGN KEY (plan_id)
            REFERENCES scheme_plans (plan_id),

    CONSTRAINT uk_nav_plan_date
        UNIQUE (plan_id, nav_date)
);

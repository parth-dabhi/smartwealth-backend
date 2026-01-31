CREATE TABLE IF NOT EXISTS investment_allotment
(
    investment_allotment_id BIGSERIAL PRIMARY KEY,

    investment_order_id BIGINT NOT NULL,

    nav_date     DATE           NOT NULL,
    nav_value    NUMERIC(12,4)  NOT NULL,

    units        NUMERIC(18,8)  NOT NULL,

    allotted_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT fk_investment_order
        FOREIGN KEY (investment_order_id)
            REFERENCES investment_order (investment_order_id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_allotment_order
    ON investment_allotment (investment_order_id);

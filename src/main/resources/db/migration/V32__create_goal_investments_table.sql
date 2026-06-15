CREATE TABLE goal_investments
(
    id BIGSERIAL PRIMARY KEY,

    goal_id BIGSERIAL NOT NULL,

    sip_mandate_id BIGINT,
    investment_order_id BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_goal
        FOREIGN KEY (goal_id)
            REFERENCES user_goals(goal_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_goal_sip
        FOREIGN KEY (sip_mandate_id)
            REFERENCES sip_mandate(sip_mandate_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_goal_order
        FOREIGN KEY (investment_order_id)
            REFERENCES investment_order(investment_order_id)
            ON DELETE CASCADE,

    -- Ensure only one type is filled
    CONSTRAINT chk_one_source
        CHECK (
            (sip_mandate_id IS NOT NULL AND investment_order_id IS NULL)
                OR
            (sip_mandate_id IS NULL AND investment_order_id IS NOT NULL)
            )
);

CREATE INDEX idx_goal_inv_goal
    ON goal_investments(goal_id);

CREATE INDEX idx_goal_inv_sip
    ON goal_investments(sip_mandate_id);

CREATE INDEX idx_goal_inv_order
    ON goal_investments(investment_order_id);

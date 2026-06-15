-- V40__add_holding_id_to_goal_investments.sql
ALTER TABLE goal_investments
    ADD COLUMN IF NOT EXISTS holding_id BIGINT;

ALTER TABLE goal_investments
    ADD CONSTRAINT fk_goal_investments_holding
        FOREIGN KEY (holding_id)
            REFERENCES user_holdings(holding_id)
            ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS idx_goal_investments_holding_id
    ON goal_investments(holding_id);

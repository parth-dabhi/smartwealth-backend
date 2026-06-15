CREATE TABLE user_goals
(
    goal_id         BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,

    -- USER INPUT
    goal_name       VARCHAR(200) NOT NULL,
    target_amount   NUMERIC(14,2) NOT NULL,   -- final target chosen by user
    duration_years  INTEGER NOT NULL,

    -- SYSTEM
    expected_return NUMERIC(5,2) NOT NULL,

    -- TRACKING
    current_value   NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_invested  NUMERIC(14,2) NOT NULL DEFAULT 0,

    -- STATUS
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | COMPLETED | CANCELLED

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_goal_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_goal_amounts
        CHECK (target_amount > 0),

    CONSTRAINT chk_duration
        CHECK (duration_years > 0 AND duration_years <= 50)
);

CREATE INDEX idx_goal_user
    ON user_goals(user_id);

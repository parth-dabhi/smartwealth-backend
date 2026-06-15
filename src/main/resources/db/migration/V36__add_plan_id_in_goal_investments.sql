ALTER TABLE goal_investments
    ADD COLUMN IF NOT EXISTS plan_id INTEGER;

-- Step 2: Add foreign key constraint
ALTER TABLE goal_investments
    ADD CONSTRAINT fk_goal_plan
        FOREIGN KEY (plan_id)
            REFERENCES scheme_plans (plan_id)
            ON DELETE CASCADE;

ALTER TABLE goal_investments
    ALTER COLUMN plan_id SET NOT NULL;
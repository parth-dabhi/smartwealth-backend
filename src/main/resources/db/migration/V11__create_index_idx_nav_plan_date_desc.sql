CREATE INDEX IF NOT EXISTS idx_nav_plan_date_desc
    ON nav_history (plan_id, nav_date DESC);
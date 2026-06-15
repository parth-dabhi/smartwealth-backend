-- V35 : nav_anchors table  +  score column on scheme_plans

-- 1. nav_anchors — one row per plan, overwritten nightly.
--    Stores the 4 NAV anchor values used to compute live returns.
--    Much faster than scanning the full nav_history for every plan nightly.
CREATE TABLE IF NOT EXISTS nav_anchors
(
    plan_id     INTEGER PRIMARY KEY,
    nav_today   NUMERIC(12, 4), -- latest available NAV
    nav_1y_ago  NUMERIC(12, 4), -- closest NAV on/before (today - 1 year)
    nav_3y_ago  NUMERIC(12, 4), -- closest NAV on/before (today - 3 years)
    nav_5y_ago  NUMERIC(12, 4), -- closest NAV on/before (today - 5 years)
    anchored_on DATE NOT NULL DEFAULT CURRENT_DATE,

    CONSTRAINT fk_nav_anchor_plan
        FOREIGN KEY (plan_id) REFERENCES scheme_plans (plan_id)
);

-- 2. score column — composite ranking score, recalculated nightly
ALTER TABLE scheme_plans
    ADD COLUMN IF NOT EXISTS score NUMERIC(10, 4);

-- Index so ORDER BY score DESC in the recommendation query uses index scan
CREATE INDEX IF NOT EXISTS idx_scheme_plans_score
    ON scheme_plans (score DESC NULLS LAST);
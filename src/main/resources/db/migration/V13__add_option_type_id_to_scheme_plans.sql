-- Add new column (nullable first for safe migration)
ALTER TABLE scheme_plans
    ADD COLUMN option_type_id INTEGER;

-- Backfill data using existing option_type column
UPDATE scheme_plans
SET option_type_id = CASE
                         WHEN option_type = 'GROWTH' THEN 1
                         WHEN option_type = 'IDCW_PAYOUT' THEN 2
                         WHEN option_type = 'IDCW_REINVEST' THEN 3
                         ELSE NULL
    END;

-- This will fail migration if unmapped values exist
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM scheme_plans
                   WHERE option_type IS NOT NULL
                     AND option_type_id IS NULL) THEN
            RAISE EXCEPTION 'Unmapped option_type values found in scheme_plan';
        END IF;
    END
$$;

-- Add foreign key constraint
ALTER TABLE scheme_plans
    ADD CONSTRAINT fk_scheme_plan_option_type
        FOREIGN KEY (option_type_id)
            REFERENCES plan_options_type (option_id);

-- Make column NOT NULL
ALTER TABLE scheme_plans
    ALTER COLUMN option_type_id SET NOT NULL;

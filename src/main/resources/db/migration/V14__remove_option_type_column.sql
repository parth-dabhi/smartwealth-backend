-- Safety check: ensure no row depends on option_type
-- (If option_type_id is NOT NULL for all rows, we are safe)
DO $$
    BEGIN
        IF EXISTS (
            SELECT 1
            FROM scheme_plans
            WHERE option_type_id IS NULL
        ) THEN
            RAISE EXCEPTION 'Cannot drop option_type column: option_type_id contains NULL values';
        END IF;
    END $$;

-- Drop old denormalized column
ALTER TABLE scheme_plans
    DROP COLUMN option_type;

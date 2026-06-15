-- 1. Add new column
ALTER TABLE users
    ADD COLUMN risk_profile_id INT;

-- 2. Backfill based on existing string values
UPDATE users
SET risk_profile_id =
    CASE UPPER(risk_profile)
        WHEN 'VERY CONSERVATIVE' THEN 1
        WHEN 'CONSERVATIVE' THEN 2
        WHEN 'MODERATE' THEN 3
        WHEN 'AGGRESSIVE' THEN 4
        WHEN 'VERY AGGRESSIVE' THEN 5
        END;

-- 3. Make NOT NULL after backfill
ALTER TABLE users
    ALTER COLUMN risk_profile_id SET NOT NULL;

-- 4. Add FK constraint
ALTER TABLE users
    ADD CONSTRAINT fk_users_risk_profile
        FOREIGN KEY (risk_profile_id)
            REFERENCES risk_profiles (risk_id);

-- 5. Drop old column
ALTER TABLE users
    DROP COLUMN risk_profile;

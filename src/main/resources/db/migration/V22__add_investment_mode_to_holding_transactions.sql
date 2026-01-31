-- 1. Add column as nullable
ALTER TABLE holding_transactions
    ADD COLUMN investment_mode VARCHAR(20);

-- 2. Backfill existing rows (example logic)
UPDATE holding_transactions
SET investment_mode = 'LUMPSUM'
WHERE investment_mode IS NULL;

-- 3. Enforce NOT NULL
ALTER TABLE holding_transactions
    ALTER COLUMN investment_mode SET NOT NULL;

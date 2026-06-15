-- V38__add_folio_to_holdings.sql

-- Drop the old unique constraint
ALTER TABLE user_holdings DROP CONSTRAINT uq_user_plan;

-- Add folio_number column
ALTER TABLE user_holdings
    ADD COLUMN folio_number VARCHAR(20) UNIQUE;

-- Now make it NOT NULL
ALTER TABLE user_holdings
    ALTER COLUMN folio_number SET NOT NULL;
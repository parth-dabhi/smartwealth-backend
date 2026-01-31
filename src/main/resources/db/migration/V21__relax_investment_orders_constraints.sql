-- 1. Drop foreign key constraint on payment_reference_id
ALTER TABLE investment_order
    DROP CONSTRAINT fk_investment_order_payment_reference;

-- 2. Remove NOT NULL constraint from payment_reference_id
ALTER TABLE investment_order
    ALTER COLUMN payment_reference_id DROP NOT NULL;

-- 3. Remove NOT NULL constraint from amount
ALTER TABLE investment_order
    ALTER COLUMN amount DROP NOT NULL;

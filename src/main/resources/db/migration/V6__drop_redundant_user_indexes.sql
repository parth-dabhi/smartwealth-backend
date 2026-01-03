-- These indexes are redundant because UNIQUE constraints
-- already create implicit unique indexes in PostgreSQL.

DROP INDEX IF EXISTS idx_user_customer_id;
DROP INDEX IF EXISTS idx_user_email;
DROP INDEX IF EXISTS idx_user_mobile_number;
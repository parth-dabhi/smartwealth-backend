-- V4__insert_admin_user.sql
-- Insert admin user for SmartWealth

INSERT INTO users (
    customer_id,
    email,
    mobile_number,
    password_hash,
    full_name,
    role,
    kyc_status,
    risk_profile,
    is_active,
    created_at,
    updated_at
)
VALUES (
           LPAD(nextval('customer_id_seq')::text, 8, '0'),
           'admin@smartwealth.internal',
           '8888899999',
           '$2a$10$HKeUJrSkJ500zZCgv9ZCNOLXJNbMiebfcmlfj36q8xGOdUNi5U5z2',
           'System Admin',
           'ADMIN',
           'VERIFIED',
           'MODERATE',
           TRUE,
           now(),
           now()
       );
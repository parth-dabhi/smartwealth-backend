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
           '10000001',
           'testuser@smartwealth.com',
           '9999999999',
           '$2a$10$E6Z6J9H6QYhWq5d9Rz8p7O1JQF7b0zVfR4rYlN1q8uKcKXvZc9N1e',
           'Test User',
           'USER',
           'VERIFIED',
           'MODERATE',
           true,
           now(),
           now()
       );

-- Create wallets for existing users who do not have one yet
INSERT INTO wallets (user_id, balance, locked_balance, status, created_at)
SELECT
    u.id,
    0.00,
    0.00,
    'ACTIVE',
    CURRENT_TIMESTAMP
FROM users u
         LEFT JOIN wallets w ON w.user_id = u.id
WHERE w.id IS NULL;
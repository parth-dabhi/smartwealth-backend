-- V5__add_user_profile_fields.sql
-- Add optional profile fields to users table

ALTER TABLE users
    ADD COLUMN date_of_birth DATE,
    ADD COLUMN gender VARCHAR(20),

    ADD COLUMN address_line_1 VARCHAR(255),
    ADD COLUMN address_line_2 VARCHAR(255),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN state VARCHAR(100),
    ADD COLUMN country VARCHAR(100),
    ADD COLUMN postal_code VARCHAR(20);

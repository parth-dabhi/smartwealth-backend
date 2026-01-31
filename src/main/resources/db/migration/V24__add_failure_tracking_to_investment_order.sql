ALTER TABLE investment_order
    ADD COLUMN failure_reason_type VARCHAR(100),
    ADD COLUMN failure_reason VARCHAR(255),
    ADD COLUMN failed_at TIMESTAMP WITH TIME ZONE;

-- Add failure tracking columns to SIP mandate table

ALTER TABLE sip_mandate
    ADD COLUMN failure_count INT NOT NULL DEFAULT 0,
    ADD COLUMN last_failure_at TIMESTAMPTZ

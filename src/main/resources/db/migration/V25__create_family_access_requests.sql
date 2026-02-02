-- Purpose: Handle family access request workflow (PENDING / ACCEPTED / REJECTED)

CREATE TABLE family_member_requests
(
    id BIGSERIAL PRIMARY KEY,

    requester_id     BIGINT      NOT NULL,  -- User who wants to view (sends request)
    member_id        BIGINT      NOT NULL,  -- User whose portfolio will be viewed (who receives request)

    request_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    -- PENDING | ACCEPTED | REJECTED

    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at TIMESTAMPTZ,
    last_requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_family_request_requester
        FOREIGN KEY (requester_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_family_request_member
        FOREIGN KEY (member_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    -- Prevent self-request
    CONSTRAINT chk_family_request_not_self
        CHECK (requester_id <> member_id),

    -- Only one request per direction (Only one active request between same users)
    CONSTRAINT uq_family_request_pair
        UNIQUE (requester_id, member_id)
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_family_requests_target_status
    ON family_member_requests (member_id, request_status);

CREATE INDEX IF NOT EXISTS idx_family_requests_requester
    ON family_member_requests (requester_id);

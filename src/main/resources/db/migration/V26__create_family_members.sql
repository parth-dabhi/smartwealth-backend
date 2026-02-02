CREATE TABLE IF NOT EXISTS family_members
(
    family_member_id BIGSERIAL PRIMARY KEY,

    viewer_id   BIGINT NOT NULL, -- User who can view investments
    owner_id    BIGINT NOT NULL,  -- User whose investments are visible

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_family_access_viewer
        FOREIGN KEY (viewer_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_family_access_owner
        FOREIGN KEY (owner_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    -- Prevent self-access
    CONSTRAINT chk_family_access_not_self
        CHECK (viewer_id <> owner_id),

    -- One access per direction
    CONSTRAINT uq_family_access_pair
        UNIQUE (viewer_id, owner_id)
);
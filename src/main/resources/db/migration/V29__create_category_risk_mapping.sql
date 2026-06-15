CREATE TABLE IF NOT EXISTS category_risk_mapping
(
    category_id BIGINT NOT NULL,
    risk_id     BIGINT NOT NULL,

    CONSTRAINT pk_category_risk_mapping
        PRIMARY KEY (category_id, risk_id),

    CONSTRAINT fk_category_risk_mapping_category
        FOREIGN KEY (category_id)
            REFERENCES fund_categories (category_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_category_risk_mapping_risk
        FOREIGN KEY (risk_id)
            REFERENCES risk_profiles (risk_id)
            ON DELETE CASCADE
);


-- INDEXES (important for performance)

CREATE INDEX IF NOT EXISTS idx_category_risk_category
    ON category_risk_mapping (category_id);

CREATE INDEX IF NOT EXISTS idx_category_risk_risk
    ON category_risk_mapping (risk_id);
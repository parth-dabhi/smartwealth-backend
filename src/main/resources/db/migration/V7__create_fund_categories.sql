CREATE TABLE IF NOT EXISTS fund_categories (
    category_id             INTEGER PRIMARY KEY,
    category_name           VARCHAR(150) NOT NULL,
    category_short_name     VARCHAR(100) NOT NULL UNIQUE,
    asset_id                INTEGER NOT NULL,
    suitable_for_profiles   INTEGER[] NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_category_asset
        FOREIGN KEY (asset_id)
        REFERENCES asset_master(asset_id)
)
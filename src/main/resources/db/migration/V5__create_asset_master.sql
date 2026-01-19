CREATE TABLE IF NOT EXISTS asset_master (
    asset_id    INTEGER PRIMARY KEY,
    asset_name  VARCHAR(50) NOT NULL UNIQUE
);
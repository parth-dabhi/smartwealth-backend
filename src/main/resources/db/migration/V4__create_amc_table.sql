CREATE TABLE IF NOT EXISTS amc (
    amc_id     INTEGER PRIMARY KEY,
    amc_name        VARCHAR(150) NOT NULL UNIQUE,
    website         VARCHAR(255)
);
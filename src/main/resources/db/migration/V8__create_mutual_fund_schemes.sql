CREATE TABLE IF NOT EXISTS mutual_fund_schemes
(
    scheme_id    INTEGER PRIMARY KEY,
    amc_id       INTEGER      NOT NULL,
    scheme_name  VARCHAR(255) NOT NULL,
    asset_id     INTEGER,
    category_id  INTEGER,
    benchmark_id INTEGER,

    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT fk_scheme_amc
        FOREIGN KEY (amc_id)
            REFERENCES amc (amc_id),

    CONSTRAINT fk_scheme_asset
        FOREIGN KEY (asset_id)
            REFERENCES asset_master (asset_id),

    CONSTRAINT fk_scheme_category
        FOREIGN KEY (category_id)
            REFERENCES fund_categories (category_id),

    CONSTRAINT fk_scheme_benchmark
        FOREIGN KEY (benchmark_id)
            REFERENCES benchmark_master (benchmark_id)
);

CREATE INDEX idx_scheme_amc_id ON mutual_fund_schemes (amc_id);
CREATE INDEX idx_scheme_asset_id ON mutual_fund_schemes (asset_id);
CREATE INDEX idx_scheme_category_id ON mutual_fund_schemes (category_id);
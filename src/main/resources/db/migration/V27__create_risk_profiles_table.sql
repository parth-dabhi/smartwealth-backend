CREATE TABLE risk_profiles
(
    risk_id           INT PRIMARY KEY,
    risk_profile_name VARCHAR(40)   NOT NULL UNIQUE,

    risk_level        INT           NOT NULL,

    -- base allocation percentages
    equity_percent    NUMERIC(5, 2) NOT NULL,
    debt_percent      NUMERIC(5, 2) NOT NULL,
    hybrid_percent    NUMERIC(5, 2) NOT NULL,
    commodities_percent NUMERIC(5, 2) NOT NULL
);


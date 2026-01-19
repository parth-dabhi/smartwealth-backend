CREATE TABLE plan_options_type
(
    option_id           INTEGER PRIMARY KEY,
    option_name         VARCHAR(50) NOT NULL
);

-- Seed default plan options
INSERT INTO plan_options_type (option_id, option_name) VALUES
(1, 'GROWTH'),
(2, 'IDCW_PAYOUT'),
(3, 'IDCW_REINVEST');

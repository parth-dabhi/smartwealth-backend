-- Add column
ALTER TABLE fund_categories
    ADD COLUMN avg_return_5y NUMERIC(5,2);

-- EQUITY

UPDATE fund_categories SET avg_return_5y = 17.11 WHERE category_id = 1;   -- Multi Cap
UPDATE fund_categories SET avg_return_5y = 20.16 WHERE category_id = 2;   -- Thematic Energy
UPDATE fund_categories SET avg_return_5y = 27.55 WHERE category_id = 3;   -- Thematic PSU
UPDATE fund_categories SET avg_return_5y = 12.68 WHERE category_id = 4;   -- ESG
UPDATE fund_categories SET avg_return_5y = 13.50 WHERE category_id = 5;   -- Banking
UPDATE fund_categories SET avg_return_5y = 9.63  WHERE category_id = 6;   -- FMCG
UPDATE fund_categories SET avg_return_5y = 22.37 WHERE category_id = 7;   -- Infra
UPDATE fund_categories SET avg_return_5y = 13.80 WHERE category_id = 8;   -- Pharma
UPDATE fund_categories SET avg_return_5y = 18.39 WHERE category_id = 9;   -- Dividend
UPDATE fund_categories SET avg_return_5y = 13.51 WHERE category_id = 10;  -- Technology
UPDATE fund_categories SET avg_return_5y = 11.00 WHERE category_id = 11;  -- MNC
UPDATE fund_categories SET avg_return_5y = 14.19 WHERE category_id = 13;  -- Consumption
UPDATE fund_categories SET avg_return_5y = 12.57 WHERE category_id = 17;  -- Large Cap
UPDATE fund_categories SET avg_return_5y = 16.42 WHERE category_id = 18;  -- Large Mid
UPDATE fund_categories SET avg_return_5y = 14.27 WHERE category_id = 19;  -- Flexi
UPDATE fund_categories SET avg_return_5y = 19.34 WHERE category_id = 20;  -- Mid
UPDATE fund_categories SET avg_return_5y = 20.53 WHERE category_id = 21;  -- Small
UPDATE fund_categories SET avg_return_5y = 16.82 WHERE category_id = 22;  -- Value
UPDATE fund_categories SET avg_return_5y = 14.51 WHERE category_id = 23;  -- ELSS
UPDATE fund_categories SET avg_return_5y = 17.29 WHERE category_id = 24;  -- Thematic generic
UPDATE fund_categories SET avg_return_5y = 9.23  WHERE category_id = 25;  -- International
UPDATE fund_categories SET avg_return_5y = 19.26 WHERE category_id = 92;  -- Auto Transport

-- DEBT

UPDATE fund_categories SET avg_return_5y = 6.29 WHERE category_id = 14; -- Floater
UPDATE fund_categories SET avg_return_5y = 5.50 WHERE category_id = 15; -- Gilt 10y
UPDATE fund_categories SET avg_return_5y = 5.02 WHERE category_id = 16; -- Gilt
UPDATE fund_categories SET avg_return_5y = 9.21 WHERE category_id = 37; -- Credit Risk
UPDATE fund_categories SET avg_return_5y = 6.75 WHERE category_id = 28; -- Medium
UPDATE fund_categories SET avg_return_5y = 5.72 WHERE category_id = 32; -- Liquid
UPDATE fund_categories SET avg_return_5y = 5.75 WHERE category_id = 30; -- Ultra Short
UPDATE fund_categories SET avg_return_5y = 5.90 WHERE category_id = 38; -- Banking PSU
UPDATE fund_categories SET avg_return_5y = 5.64 WHERE category_id = 35; -- Dynamic
UPDATE fund_categories SET avg_return_5y = 5.96 WHERE category_id = 34; -- Money Market
UPDATE fund_categories SET avg_return_5y = 6.07 WHERE category_id = 29; -- Short
UPDATE fund_categories SET avg_return_5y = 5.94 WHERE category_id = 31; -- Low
UPDATE fund_categories SET avg_return_5y = 4.83 WHERE category_id = 26; -- Long
UPDATE fund_categories SET avg_return_5y = 5.37 WHERE category_id = 27; -- Medium Long
UPDATE fund_categories SET avg_return_5y = 5.36 WHERE category_id = 33; -- Overnight
UPDATE fund_categories SET avg_return_5y = 5.97 WHERE category_id = 40; -- Corporate Bond
UPDATE fund_categories SET avg_return_5y = 6.89 WHERE category_id = 81; -- Target Maturity

-- HYBRID

UPDATE fund_categories SET avg_return_5y = 12.82 WHERE category_id = 39; -- Aggressive
UPDATE fund_categories SET avg_return_5y = 7.99  WHERE category_id = 42; -- Conservative
UPDATE fund_categories SET avg_return_5y = 5.68  WHERE category_id = 44; -- Arbitrage
UPDATE fund_categories SET avg_return_5y = 9.80  WHERE category_id = 45; -- Dynamic Asset
UPDATE fund_categories SET avg_return_5y = 8.43  WHERE category_id = 43; -- Equity Savings
UPDATE fund_categories SET avg_return_5y = 16.49 WHERE category_id = 46; -- Multi Asset

-- COMMODITIES

UPDATE fund_categories SET avg_return_5y = 25.23 WHERE category_id = 47; -- Gold

-- Silver → no reliable avg → keep NULL

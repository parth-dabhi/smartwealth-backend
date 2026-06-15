
ALTER TABLE fund_categories
    ADD COLUMN IF NOT EXISTS min_months INTEGER,
    ADD COLUMN IF NOT EXISTS max_months INTEGER;

ALTER TABLE fund_categories
    DROP COLUMN IF EXISTS suitable_for_profiles;

UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=1;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=2;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=3;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=4;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=5;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=6;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=7;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=8;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=9;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=10;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=11;
UPDATE fund_categories SET min_months=12, max_months=120 WHERE category_id=12;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=13;
UPDATE fund_categories SET min_months=6, max_months=60 WHERE category_id=14;
UPDATE fund_categories SET min_months=24, max_months=120 WHERE category_id=15;
UPDATE fund_categories SET min_months=24, max_months=120 WHERE category_id=16;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=17;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=18;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=19;
UPDATE fund_categories SET min_months=84, max_months=360 WHERE category_id=20;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=21;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=22;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=23;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=24;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=25;
UPDATE fund_categories SET min_months=36, max_months=180 WHERE category_id=39;
UPDATE fund_categories SET min_months=12, max_months=60 WHERE category_id=40;
UPDATE fund_categories SET min_months=24, max_months=120 WHERE category_id=41;
UPDATE fund_categories SET min_months=12, max_months=60 WHERE category_id=42;
UPDATE fund_categories SET min_months=12, max_months=60 WHERE category_id=43;
UPDATE fund_categories SET min_months=1, max_months=24 WHERE category_id=44;
UPDATE fund_categories SET min_months=12, max_months=120 WHERE category_id=45;
UPDATE fund_categories SET min_months=36, max_months=240 WHERE category_id=46;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=47;
UPDATE fund_categories SET min_months=60, max_months=360 WHERE category_id=79;
UPDATE fund_categories SET min_months=0, max_months=24 WHERE category_id=80;
UPDATE fund_categories SET min_months=12, max_months=120 WHERE category_id=81;
UPDATE fund_categories SET min_months=24, max_months=120 WHERE category_id=91;
UPDATE fund_categories SET min_months=120, max_months=360 WHERE category_id=92;

ALTER TABLE fund_categories
    ADD CONSTRAINT chk_fund_category_month_range
        CHECK (
            min_months IS NULL
                OR max_months IS NULL
                OR min_months <= max_months
            );

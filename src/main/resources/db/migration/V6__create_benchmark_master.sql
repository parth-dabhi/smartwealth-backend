CREATE TABLE IF NOT EXISTS benchmark_master (
    benchmark_id    INTEGER PRIMARY KEY,
    benchmark_name  VARCHAR(100) NOT NULL UNIQUE
);
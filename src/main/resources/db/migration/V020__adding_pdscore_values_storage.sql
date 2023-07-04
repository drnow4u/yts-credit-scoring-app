ALTER TABLE credit_score_report ADD COLUMN loans DOUBLE PRECISION;
ALTER TABLE credit_score_report ADD COLUMN lines DOUBLE PRECISION;
ALTER TABLE credit_score_report ADD COLUMN pd_status VARCHAR(256);
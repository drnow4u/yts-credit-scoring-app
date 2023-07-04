ALTER TABLE credit_score_report DROP COLUMN loans;
ALTER TABLE credit_score_report DROP COLUMN lines;

ALTER TABLE credit_score_report ADD COLUMN pd_score INTEGER;
ALTER TABLE credit_score_report ADD COLUMN pd_grade TEXT;
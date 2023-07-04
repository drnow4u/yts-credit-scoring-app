ALTER TABLE credit_score_report
ALTER COLUMN newest_transaction_date DROP NOT NULL;

ALTER TABLE credit_score_report
ALTER COLUMN oldest_transaction_date DROP NOT NULL;

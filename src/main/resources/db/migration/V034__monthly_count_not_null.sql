ALTER TABLE credit_score_monthly_report DROP COLUMN incoming_transactions_size;
ALTER TABLE credit_score_monthly_report DROP COLUMN outgoing_transactions_size;

ALTER TABLE credit_score_monthly_report ADD COLUMN incoming_transactions_size INTEGER NOT NULL DEFAULT 0;
ALTER TABLE credit_score_monthly_report ADD COLUMN outgoing_transactions_size INTEGER NOT NULL DEFAULT 0;
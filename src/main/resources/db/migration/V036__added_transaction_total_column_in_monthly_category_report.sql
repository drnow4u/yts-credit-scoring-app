ALTER TABLE credit_score_monthly_category_report
    ADD COLUMN transaction_total INTEGER NOT NULL DEFAULT 1;

ALTER TABLE credit_score_monthly_category_report
    ALTER transaction_total DROP DEFAULT;

ALTER TABLE credit_score_monthly_category_report
    ADD CHECK (transaction_total > 0);
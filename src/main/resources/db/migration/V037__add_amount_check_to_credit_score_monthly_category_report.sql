ALTER TABLE credit_score_monthly_category_report
    ADD CHECK (amount >= 0);

ALTER TABLE credit_score_monthly_category_report
    ADD UNIQUE (credit_score_monthly_report_id, category);

ALTER TABLE credit_score_monthly_category_report
    ADD FOREIGN KEY (credit_score_monthly_report_id) REFERENCES credit_score_monthly_report (id);

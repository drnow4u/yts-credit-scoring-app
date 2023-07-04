ALTER TABLE credit_score_report
    DROP CONSTRAINT credit_score_report_credit_score_user_id_fkey;

ALTER TABLE credit_score_report
    ADD CONSTRAINT credit_score_report_credit_score_user_id_fkey
        FOREIGN KEY (credit_score_user_id) REFERENCES credit_score_user (id)
            ON DELETE CASCADE;

ALTER TABLE credit_score_monthly_report
    DROP CONSTRAINT credit_score_monthly_report_credit_score_report_id_fkey;

ALTER TABLE credit_score_monthly_report
    ADD CONSTRAINT credit_score_monthly_report_credit_score_report_id_fkey
        FOREIGN KEY (credit_score_report_id) REFERENCES credit_score_report (id)
            ON DELETE CASCADE;

ALTER TABLE credit_score_monthly_category_report
    DROP CONSTRAINT credit_score_monthly_category_credit_score_monthly_report__fkey;

ALTER TABLE credit_score_monthly_category_report
    ADD CONSTRAINT credit_score_monthly_category_credit_score_monthly_report__fkey
        FOREIGN KEY (credit_score_monthly_report_id) REFERENCES credit_score_monthly_report (id)
            ON DELETE CASCADE;

ALTER TABLE credit_score_monthly_recurring_transactions_report
    DROP CONSTRAINT credit_score_monthly_recurring_transaction_credit_score_id_fkey;

ALTER TABLE credit_score_monthly_recurring_transactions_report
    ADD CONSTRAINT credit_score_monthly_recurring_transaction_credit_score_id_fkey
        FOREIGN KEY (credit_score_id) REFERENCES credit_score_report (id)
            ON DELETE CASCADE;

ALTER TABLE credit_score_report_signature_json_paths
    DROP CONSTRAINT credit_score_report_signature_json__credit_score_report_id_fkey;

ALTER TABLE credit_score_report_signature_json_paths
    ADD CONSTRAINT credit_score_report_signature_json__credit_score_report_id_fkey
        FOREIGN KEY (credit_score_report_id) REFERENCES credit_score_report (id)
            ON DELETE CASCADE;

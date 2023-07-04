ALTER TABLE credit_score_report ADD COLUMN bban VARCHAR(256);
ALTER TABLE credit_score_report ADD COLUMN sort_code_account_number VARCHAR(256);
ALTER TABLE credit_score_report ADD COLUMN masked_pan VARCHAR(256);
ALTER TABLE credit_score_report ALTER COLUMN iban DROP NOT NULL;
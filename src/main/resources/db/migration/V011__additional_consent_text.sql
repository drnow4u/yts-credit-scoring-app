ALTER TABLE client RENAME additional_text TO additional_text_report;
ALTER TABLE client ADD COLUMN additional_text_consent VARCHAR(256);
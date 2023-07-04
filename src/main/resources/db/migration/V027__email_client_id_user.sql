ALTER TABLE credit_score_user ADD COLUMN client_email_id UUID,
ADD FOREIGN KEY (client_email_id) REFERENCES client_email(id);
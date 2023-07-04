ALTER TABLE credit_score_user ADD COLUMN date_time_consent TIMESTAMP WITH TIME ZONE;

ALTER TABLE credit_score_user
ALTER COLUMN date_time_invited TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE credit_score_user
ALTER COLUMN date_time_status_change TYPE TIMESTAMP WITH TIME ZONE;
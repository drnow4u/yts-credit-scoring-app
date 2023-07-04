ALTER TABLE credit_score_user
    ADD COLUMN admin_email VARCHAR(256);

UPDATE credit_score_user
SET admin_email = 'no-reply-cashflow-analyser@yolt.com'
WHERE true;

ALTER TABLE credit_score_user
    ALTER COLUMN admin_email SET NOT NULL;

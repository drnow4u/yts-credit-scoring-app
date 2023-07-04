CREATE TABLE IF NOT EXISTS public_key
(
    kid            UUID                     NOT NULL,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL,
    public_key     BYTEA                    NOT NULL,
    PRIMARY KEY (kid)
);

ALTER TABLE credit_score_report
    ADD COLUMN signature_key_id UUID;

ALTER TABLE terms_and_conditions RENAME TO legal_document;

ALTER TABLE legal_document ADD COLUMN document_type VARCHAR(256) NOT NULL DEFAULT 'TERMS_AND_CONDITIONS';

ALTER TABLE legal_document ADD UNIQUE (client_id, version, document_type);

ALTER TABLE credit_score_user ADD COLUMN privacy_policy_id UUID,
ADD FOREIGN KEY (privacy_policy_id) REFERENCES legal_document(id);
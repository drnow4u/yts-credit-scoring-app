ALTER TABLE client DROP COLUMN api_token_feature_toggle;
ALTER TABLE client ADD COLUMN api_token_feature_toggle BOOLEAN NOT NULL DEFAULT FALSE;

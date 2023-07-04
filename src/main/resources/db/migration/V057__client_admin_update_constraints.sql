-- So far unique has been email, client_id in V001__database_setup.sql.
-- Since we have multiple identity provides the constraint has to be change to pair (email, auth_provider).
-- Column client_id is not in unique anymore because for a given pair (email, auth_provider) CFA has to determine only one client_id.
-- For a given pair (email, auth_provider) multiple idp_id are not allowed, and idp_id is not in unique constraint.
ALTER TABLE client_admin
    DROP CONSTRAINT client_admin_email_client_id_key;

ALTER TABLE client_admin
    ADD CONSTRAINT client_admin_email_auth_provider_key
        UNIQUE (email, auth_provider);

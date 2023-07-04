ALTER TABLE client
    ADD COLUMN invitation_email_template_prefix TEXT NOT NULL DEFAULT 'UserInvitation_Test_Client';

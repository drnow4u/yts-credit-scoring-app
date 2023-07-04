-- <CLIENT_ID> - replace with random uuid, relates to CLIENT_ID in client_admin and legal_document table
-- <CLIENT_NAME> - Client name
-- <LOGO> - base64 client logo image - optional, add null or remove from insert
-- <SITE_TAGS> - tag for site query in Yolt API
-- <DEFAULT_LANGUAGE> - default language on consent page e.g NL, EN
-- <ADDITIONAL_TEXT_CONSENT> - additional client text on consent page - optional, add null or remove from insert
-- <ADDITIONAL_TEXT_REPORT> - additional client text on report page - optional, add null or remove from insert
insert into client (id, name, logo, site_tags, default_language, additional_text_consent, additional_text_report) values
(
    '<CLIENT_ID>',
    '<CLIENT_NAME>',
    '<LOGO>',
    '<SITE_TAGS>',
    '<DEFAULT_LANGUAGE>',
    '<ADDITIONAL_TEXT_CONSENT>',
    '<ADDITIONAL_TEXT_REPORT>'
);

-- <CLIENT_EMAIL_ID> - replace with random uuid
-- <TEMPLATE> - email template name for client. IMPORTANT: html and text template with that name should be placed in resource/mail catalog
-- <SUBJECT> - email subject if not provided default will be used
-- <SENDER> - email sender, if not provided default will be used
-- <CLIENT_ID> - relates to CLIENT_ID in client table
insert into client_email(id, template, subject, sender, client_id) values
(
    '<CLIENT_EMAIL_ID>',
    '<TEMPLATE>',
    '<SUBJECT>',
    '<SENDER>',
    '<CLIENT_ID>'
);

-- <CLIENT_ADMIN_ID> - replace with random uuid
-- <CLIENT_ADMIN_EMAIL> - user email address
-- <CLIENT_ADMIN_GITHUB_ID> - user Github ID
-- Copy insert for additional users
insert into client_admin (id, email, client_id, idp_id) values
(
    '<CLIENT_ADMIN_ID>',
    '<CLIENT_ADMIN_EMAIL>',
    '<CLIENT_ID>',
    '<CLIENT_ADMIN_GITHUB_ID>'
);

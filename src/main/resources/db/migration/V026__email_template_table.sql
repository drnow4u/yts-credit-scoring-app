ALTER TABLE client DROP COLUMN invitation_email_template_prefix;

CREATE TABLE IF NOT EXISTS client_email (
    id              UUID NOT NULL,
    template        VARCHAR(256) NOT NULL,
    subject         VARCHAR(256),
    sender          VARCHAR(256),
    client_id       UUID NOT NULL,
    PRIMARY KEY(id),

    FOREIGN KEY (client_id) REFERENCES client (id)
);
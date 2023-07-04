CREATE TABLE IF NOT EXISTS client(
    id              UUID NOT NULL,
    name            VARCHAR(256) NOT NULL,
    logo            VARCHAR(256),
    PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS client_admin(
    id              UUID NOT NULL,
    email           VARCHAR(256) NOT NULL,
    client_id       UUID NOT NULL,
    idp_id          VARCHAR(256) NOT NULL UNIQUE,
    PRIMARY KEY(id),
    FOREIGN KEY (client_id) REFERENCES client (id),
    UNIQUE (email, client_id)
);

CREATE TABLE IF NOT EXISTS terms_and_conditions(
    id              UUID NOT NULL,
    content         TEXT NOT NULL,
    version         INTEGER NULL,
    creation_date   DATE NOT NULL,
    client_id       UUID NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (client_id) REFERENCES client (id)
);

CREATE TABLE IF NOT EXISTS credit_score_user(
    id                      UUID NOT NULL,
    name                    VARCHAR(256) NOT NULL,
    email                   VARCHAR(256) NOT NULL,
    client_id               UUID NOT NULL,
    status                  VARCHAR(256) NOT NULL,
    date_time_invited       TIMESTAMP NOT NULL,
    date_time_status_change TIMESTAMP,
    consent boolean         NOT NULL,
    invitation_hash         VARCHAR(60) NOT NULL UNIQUE,
    user_agent              VARCHAR(256),
    ip_address              VARCHAR(45),
    terms_and_condition_id  UUID,
    yolt_user_id            UUID,
    yolt_user_site_id       UUID,
    yolt_activity_id        UUID,
    UNIQUE (email, client_id),
    PRIMARY KEY(id),
    FOREIGN KEY (client_id) REFERENCES client (id),
    FOREIGN KEY (terms_and_condition_id) REFERENCES terms_and_conditions(id)
);

CREATE TABLE IF NOT EXISTS credit_score_report(
    id                      UUID NOT NULL,
    credit_score_user_id    UUID NOT NULL UNIQUE,
    iban                    VARCHAR(256) NOT NULL,
    initial_balance         DECIMAL NOT NULL,
    currency                VARCHAR(10) NOT NULL,
    newest_transaction_date DATE NOT NULL,
    oldest_transaction_date DATE NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (credit_score_user_id) REFERENCES credit_score_user (id)
);

CREATE TABLE IF NOT EXISTS credit_score_monthly_report(
    id                      UUID NOT NULL,
    credit_score_report_id  UUID NOT NULL,
    month                   INTEGER NOT NULL,
    year                    INTEGER NOT NULL,
    highest_balance         DECIMAL NOT NULL,
    lowest_balance          DECIMAL NOT NULL,
    total_incoming          DECIMAL NOT NULL,
    total_outgoing          DECIMAL NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY (credit_score_report_id) REFERENCES credit_score_report (id)
);

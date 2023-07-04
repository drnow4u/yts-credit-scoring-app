CREATE TABLE IF NOT EXISTS client_token
(
    jwt_id              UUID                     NOT NULL,
    client_id           UUID                     NOT NULL,
    name                VARCHAR(256)             NOT NULL,
    created_date        TIMESTAMP WITH TIME ZONE NOT NULL,
    expiration_date     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_accessed_date  TIMESTAMP WITH TIME ZONE,
    status              VARCHAR(256)             NOT NULL,
    PRIMARY KEY (jwt_id),
    FOREIGN KEY (client_id) REFERENCES client (id)
);

CREATE TABLE IF NOT EXISTS client_token_permission
(
    jwt_id              UUID,
    permission          VARCHAR(256)             NOT NULL,
    UNIQUE (jwt_id, permission),
    FOREIGN KEY (jwt_id) REFERENCES client_token (jwt_id)
);
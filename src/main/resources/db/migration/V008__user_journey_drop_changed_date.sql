DROP TABLE IF EXISTS user_journey_metric;

CREATE TABLE IF NOT EXISTS user_journey_metric
(
    id           UUID                     NOT NULL,
    user_id      UUID                     NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    client_id    UUID                     NOT NULL,
    status       VARCHAR(256)             NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (client_id) REFERENCES client (id)
);

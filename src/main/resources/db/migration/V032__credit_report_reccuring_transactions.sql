CREATE TABLE IF NOT EXISTS credit_score_monthly_recurring_transactions_report
(
    id                             UUID         NOT NULL,
    credit_score_id                UUID         NOT NULL,
    month                          INTEGER      NOT NULL,
    year                           INTEGER      NOT NULL,
    income_recurring_amount        DECIMAL,
    income_recurring_size          INT,
    outcome_recurring_amount       DECIMAL,
    outcome_recurring_size         INT,
    PRIMARY KEY(id),
    FOREIGN KEY (credit_score_id) REFERENCES credit_score_report (id) ON DELETE CASCADE
);
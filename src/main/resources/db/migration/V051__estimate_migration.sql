CREATE TABLE IF NOT EXISTS estimate_report
(
    id      UUID          NOT NULL,
    user_id UUID          NOT NULL,
    status  VARCHAR(256)  NOT NULL,
    score   INTEGER,
    grade   TEXT,

    PRIMARY KEY (id)

);

INSERT INTO estimate_report
SELECT uuid_generate_v4()   AS id,
       credit_score_user_id AS user_id,
       pd_status            AS status,
       pd_score             AS score,
       pd_grade             AS grade
FROM credit_score_report
WHERE credit_score_report.pd_status IS NOT NULL;

ALTER TABLE credit_score_report DROP COLUMN pd_score;
ALTER TABLE credit_score_report DROP COLUMN pd_grade;
ALTER TABLE credit_score_report DROP COLUMN pd_status;

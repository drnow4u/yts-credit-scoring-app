CREATE TABLE IF NOT EXISTS credit_score_report_signature_json_paths
(
    credit_score_report_id UUID,
    signature_json_paths   VARCHAR(256) NOT NULL,
    signature_json_paths_order INT NOT NULL,
    UNIQUE (credit_score_report_id, signature_json_paths),
    FOREIGN KEY (credit_score_report_id) REFERENCES credit_score_report (id)
);

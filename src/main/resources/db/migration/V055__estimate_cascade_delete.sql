DELETE
FROM estimate_report er
WHERE er.id in (select er.id
                from estimate_report er
                         left join credit_score_user csu on er.user_id = csu.id
                where csu.id is null
);

ALTER TABLE estimate_report
    ADD CONSTRAINT estimate_report_credit_score_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES credit_score_user (id)
            ON DELETE CASCADE;
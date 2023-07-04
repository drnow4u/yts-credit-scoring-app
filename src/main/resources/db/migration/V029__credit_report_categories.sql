CREATE TABLE IF NOT EXISTS credit_score_monthly_category_report
(
    id                             UUID         NOT NULL,
    credit_score_monthly_report_id UUID,
    amount                         DECIMAL,
    category                       VARCHAR(100) NOT NULL
);

create extension if not exists "uuid-ossp";

insert into credit_score_monthly_category_report
select uuid_generate_v4()                         as id,
       credit_score_monthly_report.id             as credit_score_monthly_report_id,
       credit_score_monthly_report.total_outgoing as amount,
       'OTHER_EXPENSES'                           as category
from credit_score_monthly_report;

insert into credit_score_monthly_category_report
select uuid_generate_v4()                         as id,
       credit_score_monthly_report.id             as credit_score_monthly_report_id,
       credit_score_monthly_report.total_incoming as amount,
       'OTHER_INCOME'                             as category
from credit_score_monthly_report;

ALTER TABLE credit_score_monthly_report
    DROP COLUMN total_outgoing;

ALTER TABLE credit_score_monthly_report
    DROP COLUMN total_incoming;

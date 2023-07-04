UPDATE credit_score_user
SET status='CALCULATION_ERROR'
WHERE status = 'ERROR';

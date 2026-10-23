-- Run once after sql/multicompany_hardening.sql on databases where
-- that migration was already applied before company registration was added.
-- Explicitly inserted company IDs do not advance PostgreSQL identity sequences.
SELECT setval(
    pg_get_serial_sequence('public.companies', 'company_id'),
    GREATEST(COALESCE((SELECT MAX(company_id) FROM public.companies), 1), 1),
    true
);

-- Run once against the EMS PostgreSQL database before enabling password recovery.
-- Employee/HR/Manager contacts come from employees.email and employees.phone.
-- Admin recovery contacts belong to the company, not to a person.
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS recovery_email VARCHAR(320), ADD COLUMN IF NOT EXISTS recovery_phone VARCHAR(30);
CREATE TABLE IF NOT EXISTS public.password_reset_requests (
 request_id BIGSERIAL PRIMARY KEY,
 user_id INTEGER NOT NULL REFERENCES public.users(user_id) ON DELETE CASCADE,
 company_id BIGINT NOT NULL REFERENCES public.companies(company_id) ON DELETE CASCADE,
 channel VARCHAR(10) NOT NULL CHECK (channel IN ('EMAIL','SMS')),
 otp_hash VARCHAR(100), expires_at TIMESTAMPTZ NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0 AND attempts <= 5),
 verified BOOLEAN NOT NULL DEFAULT FALSE, reset_token_hash VARCHAR(64),
 reset_token_expires_at TIMESTAMPTZ, used_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_password_reset_requests_user_channel_created ON public.password_reset_requests(user_id,channel,created_at DESC);
CREATE INDEX IF NOT EXISTS idx_password_reset_requests_token ON public.password_reset_requests(reset_token_hash) WHERE reset_token_hash IS NOT NULL;
-- Backfill recovery_email for existing Admin accounts before enabling email recovery:
-- UPDATE companies SET recovery_email='your-verified-company-email@example.com' WHERE company_id=<company_id>;

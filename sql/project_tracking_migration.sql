-- Projects module extension. Run once in the employee_management_system database.
-- Adds descriptive metadata and controlled values without removing existing project rows.
ALTER TABLE public.projects
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    ADD COLUMN IF NOT EXISTS priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'projects_status_valid'
          AND conrelid = 'public.projects'::regclass
    ) THEN
        ALTER TABLE public.projects
            ADD CONSTRAINT projects_status_valid
            CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'projects_priority_valid'
          AND conrelid = 'public.projects'::regclass
    ) THEN
        ALTER TABLE public.projects
            ADD CONSTRAINT projects_priority_valid
            CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_projects_company_manager
    ON public.projects (company_id, manager_id);

CREATE INDEX IF NOT EXISTS idx_employee_project_company_project
    ON public.employee_project (company_id, project_id);

-- Assertions run after sql/multicompany_hardening.sql in a disposable CI database.
DO $$
DECLARE
    blocked BOOLEAN := FALSE;
BEGIN
    IF (SELECT COUNT(*) FROM public.companies) <> 2 THEN
        RAISE EXCEPTION 'Expected two companies in migration fixture';
    END IF;

    IF EXISTS (
        SELECT 1 FROM public.attendance a
        JOIN public.employees e ON e.employee_id = a.employee_id
        WHERE a.company_id <> e.company_id
    ) OR EXISTS (
        SELECT 1 FROM public.leaves l
        JOIN public.employees e ON e.employee_id = l.employee_id
        WHERE l.company_id <> e.company_id
    ) OR EXISTS (
        SELECT 1 FROM public.payroll p
        JOIN public.employees e ON e.employee_id = p.employee_id
        WHERE p.company_id <> e.company_id
    ) THEN
        RAISE EXCEPTION 'Employee-owned records were not backfilled to the employee company';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.employee_project ep
        JOIN public.employees e ON e.employee_id = ep.employee_id
        JOIN public.projects p ON p.project_id = ep.project_id
        WHERE ep.company_id <> e.company_id OR ep.company_id <> p.company_id
    ) THEN
        RAISE EXCEPTION 'Project membership contains cross-company rows';
    END IF;

    BEGIN
        INSERT INTO public.employee_project (employee_id, project_id)
        VALUES (1, 2);
    EXCEPTION WHEN OTHERS THEN
        IF SQLERRM LIKE 'Employee and project must belong to the same company%' THEN
            blocked := TRUE;
        ELSE
            RAISE;
        END IF;
    END;

    IF NOT blocked THEN
        RAISE EXCEPTION 'Cross-company project membership was unexpectedly accepted';
    END IF;
END $$;

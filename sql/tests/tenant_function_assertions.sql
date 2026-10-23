-- Run after legacy functions and tenant-scoped overloads are installed.
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM public.get_all_leaves(1)) <> 1
       OR (SELECT COUNT(*) FROM public.get_all_leaves(2)) <> 1 THEN
        RAISE EXCEPTION 'Company-scoped leave listing returned an unexpected row count';
    END IF;

    IF EXISTS (SELECT 1 FROM public.get_leave_by_id(1, 2))
       OR EXISTS (SELECT 1 FROM public.get_leaves_by_employee(1, 2)) THEN
        RAISE EXCEPTION 'Leave SQL overload leaked a different company record';
    END IF;

    IF (SELECT COUNT(*) FROM public.get_all_payrolls(1)) <> 1
       OR (SELECT COUNT(*) FROM public.get_all_payrolls(2)) <> 1 THEN
        RAISE EXCEPTION 'Company-scoped payroll listing returned an unexpected row count';
    END IF;

    IF EXISTS (SELECT 1 FROM public.get_payroll_by_id(1, 2))
       OR EXISTS (SELECT 1 FROM public.get_payrolls_by_employee(1, 2)) THEN
        RAISE EXCEPTION 'Payroll SQL overload leaked a different company record';
    END IF;
END $$;

-- Tenant-aware overloads used by LeaveRepository and PayrollRepository.
-- Apply after the legacy leave/payroll function files and the company migration.
-- Legacy overloads remain for compatibility; application repositories use these
-- company_id-bearing signatures exclusively.

-- ========================= LEAVES =========================

CREATE OR REPLACE FUNCTION public.create_leave(
    p_from_date DATE, p_to_date DATE, p_reason VARCHAR,
    p_status VARCHAR, p_employee_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.employees
        WHERE employee_id = p_employee_id AND company_id = p_company_id
    ) THEN
        RAISE EXCEPTION 'Employee does not belong to this company';
    END IF;
    RETURN QUERY SELECT * FROM public.create_leave(
        p_from_date, p_to_date, p_reason, p_status, p_employee_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.get_all_leaves(p_company_id BIGINT)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.company_id = p_company_id
    ORDER BY l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.get_leave_by_id(
    p_leave_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.leave_id = p_leave_id AND l.company_id = p_company_id;
$$;

CREATE OR REPLACE FUNCTION public.update_leave(
    p_leave_id INTEGER, p_from_date DATE, p_to_date DATE,
    p_reason VARCHAR, p_status VARCHAR, p_employee_id INTEGER,
    p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.leaves
        WHERE leave_id = p_leave_id AND company_id = p_company_id
    ) OR NOT EXISTS (
        SELECT 1 FROM public.employees
        WHERE employee_id = p_employee_id AND company_id = p_company_id
    ) THEN
        RAISE EXCEPTION 'Leave or employee does not belong to this company';
    END IF;
    RETURN QUERY SELECT * FROM public.update_leave(
        p_leave_id, p_from_date, p_to_date, p_reason, p_status, p_employee_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.withdraw_leave(
    p_leave_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.leaves
        WHERE leave_id = p_leave_id AND company_id = p_company_id
    ) THEN
        RETURN;
    END IF;
    RETURN QUERY SELECT * FROM public.withdraw_leave(p_leave_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.delete_leave(
    p_leave_id INTEGER, p_company_id BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.leaves
        WHERE leave_id = p_leave_id AND company_id = p_company_id
    ) THEN
        RETURN FALSE;
    END IF;
    RETURN public.delete_leave(p_leave_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.get_leaves_by_employee(
    p_employee_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.employee_id = p_employee_id AND l.company_id = p_company_id
    ORDER BY l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.get_leaves_by_status(
    p_status VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.company_id = p_company_id AND LOWER(l.status) = LOWER(p_status)
    ORDER BY l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.get_employee_leaves_by_status(
    p_employee_id INTEGER, p_status VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.employee_id = p_employee_id
      AND l.company_id = p_company_id
      AND LOWER(l.status) = LOWER(p_status)
    ORDER BY l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.get_leaves_by_from_date(
    p_from_date DATE, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.from_date = p_from_date AND l.company_id = p_company_id
    ORDER BY l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.get_leaves_by_to_date(
    p_to_date DATE, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.to_date = p_to_date AND l.company_id = p_company_id
    ORDER BY l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.get_leaves_between_dates(
    p_start_date DATE, p_end_date DATE, p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE sql STABLE
AS $$
    SELECT l.* FROM public.leaves l
    WHERE l.company_id = p_company_id
      AND l.from_date <= p_end_date AND l.to_date >= p_start_date
    ORDER BY l.from_date, l.leave_id;
$$;

CREATE OR REPLACE FUNCTION public.update_manager_leave_status(
    p_leave_id INTEGER, p_manager_id INTEGER, p_status VARCHAR,
    p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.employees
        WHERE employee_id = p_manager_id AND company_id = p_company_id
    ) OR NOT EXISTS (
        SELECT 1
        FROM public.leaves l
        JOIN public.employees e ON e.employee_id = l.employee_id
        WHERE l.leave_id = p_leave_id
          AND l.company_id = p_company_id
          AND e.company_id = p_company_id
          AND e.manager_id = p_manager_id
    ) THEN
        RAISE EXCEPTION 'Leave request is not managed by this manager';
    END IF;
    RETURN QUERY SELECT * FROM public.update_manager_leave_status(
        p_leave_id, p_manager_id, p_status);
END;
$$;

CREATE OR REPLACE FUNCTION public.get_manager_leave_requests(
    p_manager_id INTEGER, p_company_id BIGINT
)
RETURNS TABLE (
    leave_id INTEGER,
    from_date DATE,
    to_date DATE,
    reason VARCHAR,
    status VARCHAR,
    employee_id INTEGER,
    company_id BIGINT,
    employee_first_name VARCHAR,
    employee_last_name VARCHAR
)
LANGUAGE sql STABLE
AS $$
    SELECT l.leave_id, l.from_date, l.to_date, l.reason, l.status,
           l.employee_id, l.company_id,
           e.first_name AS employee_first_name,
           e.last_name AS employee_last_name
    FROM public.leaves l
    JOIN public.employees e ON e.employee_id = l.employee_id
    JOIN public.users u
      ON u.employee_id = e.employee_id
     AND u.company_id = e.company_id
     AND u.is_active = TRUE
    WHERE e.manager_id = p_manager_id
      AND e.company_id = p_company_id
      AND l.company_id = p_company_id
    ORDER BY l.leave_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_leaves_by_hr(
    p_hr_id INTEGER, p_company_id BIGINT
)
RETURNS TABLE (
    leave_id INTEGER,
    from_date DATE,
    to_date DATE,
    reason VARCHAR,
    status VARCHAR,
    employee_id INTEGER,
    company_id BIGINT,
    employee_first_name VARCHAR,
    employee_last_name VARCHAR
)
LANGUAGE sql STABLE
AS $$
    SELECT l.leave_id, l.from_date, l.to_date, l.reason, l.status,
           l.employee_id, l.company_id,
           e.first_name AS employee_first_name,
           e.last_name AS employee_last_name
    FROM public.leaves l
    JOIN public.employees e ON e.employee_id = l.employee_id
    JOIN public.users u
      ON u.employee_id = e.employee_id
     AND u.company_id = e.company_id
     AND u.is_active = TRUE
    WHERE e.hr_id = p_hr_id
      AND e.company_id = p_company_id
      AND l.company_id = p_company_id
    ORDER BY l.leave_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.delete_leave_by_hr(
    p_leave_id INTEGER, p_hr_id INTEGER, p_company_id BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM public.leaves l
        JOIN public.employees e ON e.employee_id = l.employee_id
        JOIN public.employees hr ON hr.employee_id = p_hr_id
        WHERE l.leave_id = p_leave_id
          AND l.company_id = p_company_id
          AND e.company_id = p_company_id
          AND hr.company_id = p_company_id
          AND e.hr_id = p_hr_id
    ) THEN
        RETURN FALSE;
    END IF;
    RETURN public.delete_leave_by_hr(p_leave_id, p_hr_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.update_hr_leave_status(
    p_leave_id INTEGER, p_hr_id INTEGER, p_status VARCHAR,
    p_company_id BIGINT
)
RETURNS SETOF public.leaves
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM public.leaves l
        JOIN public.employees e ON e.employee_id = l.employee_id
        JOIN public.employees hr ON hr.employee_id = p_hr_id
        WHERE l.leave_id = p_leave_id
          AND l.company_id = p_company_id
          AND e.company_id = p_company_id
          AND hr.company_id = p_company_id
          AND e.hr_id = p_hr_id
    ) THEN
        RAISE EXCEPTION 'Leave request is not assigned to this HR user';
    END IF;
    RETURN QUERY SELECT * FROM public.update_hr_leave_status(
        p_leave_id, p_hr_id, p_status);
END;
$$;

-- ========================= PAYROLL =========================

CREATE OR REPLACE FUNCTION public.create_payroll(
    p_pay_month VARCHAR, p_basic_salary NUMERIC, p_allowances NUMERIC,
    p_deductions NUMERIC, p_net_salary NUMERIC, p_payment_date DATE,
    p_employee_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.employees
        WHERE employee_id = p_employee_id AND company_id = p_company_id
    ) THEN
        RAISE EXCEPTION 'Employee does not belong to this company';
    END IF;
    RETURN QUERY SELECT * FROM public.create_payroll(
        p_pay_month, p_basic_salary, p_allowances, p_deductions,
        p_net_salary, p_payment_date, p_employee_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.get_all_payrolls(p_company_id BIGINT)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_payroll_by_id(
    p_payroll_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.payroll_id = p_payroll_id AND p.company_id = p_company_id;
$$;

CREATE OR REPLACE FUNCTION public.update_payroll(
    p_payroll_id INTEGER, p_pay_month VARCHAR, p_basic_salary NUMERIC,
    p_allowances NUMERIC, p_deductions NUMERIC, p_net_salary NUMERIC,
    p_payment_date DATE, p_employee_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.payroll
        WHERE payroll_id = p_payroll_id AND company_id = p_company_id
    ) OR NOT EXISTS (
        SELECT 1 FROM public.employees
        WHERE employee_id = p_employee_id AND company_id = p_company_id
    ) THEN
        RAISE EXCEPTION 'Payroll or employee does not belong to this company';
    END IF;
    RETURN QUERY SELECT * FROM public.update_payroll(
        p_payroll_id, p_pay_month, p_basic_salary, p_allowances,
        p_deductions, p_net_salary, p_payment_date, p_employee_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.delete_payroll(
    p_payroll_id INTEGER, p_company_id BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.payroll
        WHERE payroll_id = p_payroll_id AND company_id = p_company_id
    ) THEN
        RETURN FALSE;
    END IF;
    RETURN public.delete_payroll(p_payroll_id);
END;
$$;

CREATE OR REPLACE FUNCTION public.get_payrolls_by_employee(
    p_employee_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.employee_id = p_employee_id AND p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_payrolls_by_month(
    p_pay_month VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.pay_month = p_pay_month AND p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_employee_payroll_by_month(
    p_employee_id INTEGER, p_pay_month VARCHAR, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.employee_id = p_employee_id
      AND p.pay_month = p_pay_month
      AND p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_payrolls_by_payment_date(
    p_payment_date DATE, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.payment_date = p_payment_date AND p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_employee_payroll_by_payment_date(
    p_employee_id INTEGER, p_payment_date DATE, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.* FROM public.payroll p
    WHERE p.employee_id = p_employee_id
      AND p.payment_date = p_payment_date
      AND p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

CREATE OR REPLACE FUNCTION public.get_payrolls_by_hr(
    p_hr_id INTEGER, p_company_id BIGINT
)
RETURNS SETOF public.payroll
LANGUAGE sql STABLE
AS $$
    SELECT p.*
    FROM public.payroll p
    JOIN public.employees e ON e.employee_id = p.employee_id
    JOIN public.users u
      ON u.employee_id = e.employee_id
     AND u.company_id = e.company_id
     AND u.is_active = TRUE
    WHERE e.hr_id = p_hr_id
      AND e.company_id = p_company_id
      AND p.company_id = p_company_id
    ORDER BY p.payroll_id DESC;
$$;

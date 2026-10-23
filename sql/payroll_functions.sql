
-- =========================================================
-- 1. CREATE PAYROLL
-- =========================================================

CREATE OR REPLACE FUNCTION create_payroll(
    p_pay_month VARCHAR,
    p_basic_salary NUMERIC,
    p_allowances NUMERIC,
    p_deductions NUMERIC,
    p_net_salary NUMERIC,
    p_payment_date DATE,
    p_employee_id INTEGER
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT INTO payroll (
        pay_month,
        basic_salary,
        allowances,
        deductions,
        net_salary,
        payment_date,
        employee_id,
        company_id
    )
    SELECT
        p_pay_month,
        p_basic_salary,
        p_allowances,
        p_deductions,
        p_net_salary,
        p_payment_date,
        e.employee_id,
        e.company_id
    FROM public.employees e
    WHERE e.employee_id = p_employee_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Payroll employee not found: %', p_employee_id;
    END IF;

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE payroll_id = currval(
        pg_get_serial_sequence('payroll', 'payroll_id')
    );

END;
$$;


-- =========================================================
-- 2. GET ALL PAYROLLS
-- =========================================================

CREATE OR REPLACE FUNCTION get_all_payrolls()
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    ORDER BY payroll_id;

END;
$$;


-- =========================================================
-- 3. GET PAYROLL BY ID
-- =========================================================

CREATE OR REPLACE FUNCTION get_payroll_by_id(
    p_payroll_id INTEGER
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE payroll_id = p_payroll_id;

END;
$$;


-- =========================================================
-- 4. UPDATE PAYROLL
-- =========================================================

CREATE OR REPLACE FUNCTION update_payroll(
    p_payroll_id INTEGER,
    p_pay_month VARCHAR,
    p_basic_salary NUMERIC,
    p_allowances NUMERIC,
    p_deductions NUMERIC,
    p_net_salary NUMERIC,
    p_payment_date DATE,
    p_employee_id INTEGER
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    UPDATE payroll
    SET
        pay_month = p_pay_month,
        basic_salary = p_basic_salary,
        allowances = p_allowances,
        deductions = p_deductions,
        net_salary = p_net_salary,
        payment_date = p_payment_date,
        employee_id = p_employee_id,
        company_id = (
            SELECT e.company_id FROM public.employees e
            WHERE e.employee_id = p_employee_id
        )
    WHERE payroll_id = p_payroll_id
      AND company_id = (
            SELECT e.company_id FROM public.employees e
            WHERE e.employee_id = p_employee_id
      );

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE payroll_id = p_payroll_id;

END;
$$;


-- =========================================================
-- 5. DELETE PAYROLL
-- =========================================================

CREATE OR REPLACE FUNCTION delete_payroll(
    p_payroll_id INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN

    DELETE FROM payroll
    WHERE payroll_id = p_payroll_id;

    RETURN FOUND;

END;
$$;


-- =========================================================
-- 6. GET PAYROLLS BY EMPLOYEE
-- =========================================================

CREATE OR REPLACE FUNCTION get_payrolls_by_employee(
    p_employee_id INTEGER
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE employee_id = p_employee_id
    ORDER BY payroll_id;

END;
$$;


-- =========================================================
-- 7. GET PAYROLLS BY MONTH
-- =========================================================

CREATE OR REPLACE FUNCTION get_payrolls_by_month(
    p_pay_month VARCHAR
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE LOWER(pay_month) = LOWER(p_pay_month)
    ORDER BY payroll_id;

END;
$$;


-- =========================================================
-- 8. EMPLOYEE + MONTH
-- =========================================================

CREATE OR REPLACE FUNCTION get_employee_payroll_by_month(
    p_employee_id INTEGER,
    p_pay_month VARCHAR
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE employee_id = p_employee_id
      AND LOWER(pay_month) = LOWER(p_pay_month)
    ORDER BY payroll_id;

END;
$$;


-- =========================================================
-- 9. GET PAYROLLS BY PAYMENT DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_payrolls_by_payment_date(
    p_payment_date DATE
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE payment_date = p_payment_date
    ORDER BY payroll_id;

END;
$$;


-- =========================================================
-- 10. EMPLOYEE + PAYMENT DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_employee_payroll_by_payment_date(
    p_employee_id INTEGER,
    p_payment_date DATE
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM payroll
    WHERE employee_id = p_employee_id
      AND payment_date = p_payment_date
    ORDER BY payroll_id;

END;
$$;


CREATE OR REPLACE FUNCTION public.get_payrolls_by_hr(
    p_hr_id INTEGER
)
RETURNS SETOF payroll
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT p.*
FROM payroll p
         JOIN employees e
              ON p.employee_id = e.employee_id
         JOIN users u
              ON u.employee_id = e.employee_id
                  AND u.company_id = e.company_id
                  AND u.is_active = TRUE
WHERE e.hr_id = p_hr_id
ORDER BY p.payroll_id DESC;
END;
$$;

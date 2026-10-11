-- ============================================
-- EMPLOYEE SQL FUNCTIONS
-- ============================================

CREATE OR REPLACE FUNCTION public.get_all_employees(p_company_id bigint)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $function$
BEGIN
RETURN QUERY
SELECT e.*
FROM employees e
WHERE e.company_id = p_company_id
  AND NOT EXISTS (
    SELECT 1
    FROM users u
             JOIN roles r ON r.role_id = u.role_id
    WHERE u.employee_id = e.employee_id
      AND u.company_id = p_company_id
      AND UPPER(r.role_name) = 'ADMIN'
);
END;
$function$;

-- 2. GET EMPLOYEE BY ID
CREATE OR REPLACE FUNCTION get_employee_by_id(
    p_employee_id INTEGER
)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM employees
    WHERE employee_id = p_employee_id;
END;
$$;


-- 3. SEARCH EMPLOYEE BY NAME
CREATE OR REPLACE FUNCTION search_employee_by_name(
    p_name VARCHAR
)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM employees
    WHERE first_name ILIKE '%' || p_name || '%'
       OR last_name ILIKE '%' || p_name || '%';
END;
$$;


-- 4. GET EMPLOYEE BY EMAIL
CREATE OR REPLACE FUNCTION get_employee_by_email(
    p_email VARCHAR
)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM employees
    WHERE email = p_email;
END;
$$;


-- 5. GET EMPLOYEES BY DESIGNATION
CREATE OR REPLACE FUNCTION get_employees_by_designation(
    p_designation_id INTEGER
)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM employees
    WHERE designation_id = p_designation_id;
END;
$$;


-- 6. UPDATE EMPLOYEE PROFILE
CREATE OR REPLACE FUNCTION update_employee_profile(
    p_employee_id INTEGER,
    p_first_name VARCHAR,
    p_last_name VARCHAR,
    p_email VARCHAR,
    p_phone VARCHAR,
    p_date_of_birth DATE,
    p_address VARCHAR,
    p_profile_photo VARCHAR,
    p_department_id INTEGER,
    p_manager_id INTEGER,
    p_hr_id INTEGER
)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
BEGIN

UPDATE employees
SET
    first_name = p_first_name,
    last_name = p_last_name,
    email = p_email,
    phone = p_phone,
    date_of_birth = p_date_of_birth,
    address = p_address,
    profile_photo = p_profile_photo,
    department_id = p_department_id,
    manager_id = p_manager_id,
    hr_id = p_hr_id
WHERE employee_id = p_employee_id;

RETURN QUERY
SELECT *
FROM employees
WHERE employee_id = p_employee_id;

END;
$$;


-- ============================================
-- GET EMPLOYEE PROFILE DETAILS
-- Includes department, designation,
-- manager and HR basic information
-- ============================================

CREATE OR REPLACE FUNCTION get_employee_profile_details(
    p_employee_id INTEGER
)
RETURNS TABLE (
    employee_id INTEGER,

    first_name VARCHAR,
    last_name VARCHAR,
    email VARCHAR,
    phone VARCHAR,
    date_of_birth DATE,
    hire_date DATE,
    address VARCHAR,
    profile_photo VARCHAR,

    department_id INTEGER,
    department_name VARCHAR,

    designation_id INTEGER,
    designation_title VARCHAR,

    manager_id INTEGER,
    manager_name VARCHAR,
    manager_email VARCHAR,
    manager_phone VARCHAR,

    hr_id INTEGER,
    hr_name VARCHAR,
    hr_email VARCHAR,
    hr_phone VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY

SELECT
    e.employee_id,

    e.first_name,
    e.last_name,
    e.email,
    e.phone,
    e.date_of_birth,
    e.hire_date,
    e.address,
    e.profile_photo,

    -- Department
    d.department_id,
    d.department_name,

    -- Designation
    des.designation_id,
    des.designation_title,

    -- Manager
    m.employee_id,
    CONCAT(m.first_name, ' ', m.last_name)::VARCHAR,
    m.email,
    m.phone,

    -- HR
    h.employee_id,
    CONCAT(h.first_name, ' ', h.last_name)::VARCHAR,
    h.email,
    h.phone

FROM employees e

         LEFT JOIN department d
                   ON e.department_id = d.department_id

         LEFT JOIN designation des
                   ON e.designation_id = des.designation_id

         LEFT JOIN employees m
                   ON e.manager_id = m.employee_id

         LEFT JOIN employees h
                   ON e.hr_id = h.employee_id

WHERE e.employee_id = p_employee_id;

END;
$$;

CREATE OR REPLACE FUNCTION create_employee_with_user(
    p_first_name VARCHAR,
    p_last_name VARCHAR,
    p_email VARCHAR,
    p_phone VARCHAR,
    p_date_of_birth DATE,
    p_hire_date DATE,
    p_address VARCHAR,
    p_department_id INTEGER,
    p_designation_id INTEGER,
    p_manager_id INTEGER,
    p_hr_id INTEGER,
    p_username VARCHAR,
    p_password VARCHAR,
    p_role_id INTEGER
)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
DECLARE
v_employee employees%ROWTYPE;
BEGIN
    -- Create employee
INSERT INTO employees (
    first_name,
    last_name,
    email,
    phone,
    date_of_birth,
    hire_date,
    address,
    department_id,
    designation_id,
    manager_id,
    hr_id
)
VALUES (
           p_first_name,
           p_last_name,
           p_email,
           p_phone,
           p_date_of_birth,
           p_hire_date,
           p_address,
           p_department_id,
           p_designation_id,
           p_manager_id,
           p_hr_id
       )
    RETURNING * INTO v_employee;

-- Create the associated login account
PERFORM *
    FROM create_user(
        p_username,
        p_password,
        TRUE,
        v_employee.employee_id,
        p_role_id
    );

RETURN NEXT v_employee;
END;
$$;

CREATE OR REPLACE FUNCTION public.get_employees_by_hr(p_hr_id integer)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $function$
BEGIN
RETURN QUERY
SELECT e.*
FROM public.employees e
WHERE e.hr_id = p_hr_id
  AND EXISTS (
    SELECT 1
    FROM public.users u
    WHERE u.employee_id = e.employee_id
      AND u.company_id = e.company_id
      AND u.is_active = TRUE
);
END;
$function$;


CREATE OR REPLACE FUNCTION public.get_employees_by_manager(p_manager_id integer)
RETURNS SETOF employees
LANGUAGE plpgsql
AS $function$
BEGIN
RETURN QUERY
SELECT e.*
FROM public.employees e
WHERE e.manager_id = p_manager_id
  AND EXISTS (
    SELECT 1
    FROM public.users u
    WHERE u.employee_id = e.employee_id
      AND u.company_id = e.company_id
      AND u.is_active = TRUE
);
END;
$function$;
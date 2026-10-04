-- ============================================
-- EMPLOYEE SQL FUNCTIONS
-- ============================================


-- 1. GET ALL EMPLOYEES
CREATE OR REPLACE FUNCTION get_all_employees()
RETURNS SETOF employees
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM employees;
END;
$$;


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

CREATE OR REPLACE FUNCTION get_projects_by_employee(
    p_employee_id INTEGER
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT p.*
FROM projects p
         JOIN employee_project ep
              ON p.project_id = ep.project_id
WHERE ep.employee_id = p_employee_id;
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
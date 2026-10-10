
-- =========================================================
-- 1. CREATE ATTENDANCE
-- =========================================================

CREATE OR REPLACE FUNCTION create_attendance(
    p_att_date DATE,
    p_status VARCHAR,
    p_check_in_time TIME,
    p_check_out_time TIME,
    p_employee_id INTEGER
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT INTO attendance (
        att_date,
        status,
        check_in_time,
        check_out_time,
        employee_id
    )
    VALUES (
        p_att_date,
        p_status,
        p_check_in_time,
        p_check_out_time,
        p_employee_id
    );

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE attendance_id = currval(
        pg_get_serial_sequence('attendance', 'attendance_id')
    );

END;
$$;


-- =========================================================
-- 2. GET ALL ATTENDANCE
-- =========================================================

CREATE OR REPLACE FUNCTION get_all_attendance()
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    ORDER BY attendance_id;

END;
$$;


-- =========================================================
-- 3. GET ATTENDANCE BY ID
-- =========================================================

CREATE OR REPLACE FUNCTION get_attendance_by_id(
    p_attendance_id INTEGER
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE attendance_id = p_attendance_id;

END;
$$;


-- =========================================================
-- 4. UPDATE ATTENDANCE
-- =========================================================

CREATE OR REPLACE FUNCTION update_attendance(
    p_attendance_id INTEGER,
    p_att_date DATE,
    p_status VARCHAR,
    p_check_in_time TIME,
    p_check_out_time TIME,
    p_employee_id INTEGER
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    UPDATE attendance
    SET
        att_date = p_att_date,
        status = p_status,
        check_in_time = p_check_in_time,
        check_out_time = p_check_out_time,
        employee_id = p_employee_id
    WHERE attendance_id = p_attendance_id;

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE attendance_id = p_attendance_id;

END;
$$;


-- =========================================================
-- 5. DELETE ATTENDANCE
-- =========================================================

CREATE OR REPLACE FUNCTION delete_attendance(
    p_attendance_id INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN

    DELETE FROM attendance
    WHERE attendance_id = p_attendance_id;

    RETURN FOUND;

END;
$$;


-- =========================================================
-- 6. GET ATTENDANCE BY EMPLOYEE
-- =========================================================

CREATE OR REPLACE FUNCTION get_attendance_by_employee(
    p_employee_id INTEGER
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE employee_id = p_employee_id
    ORDER BY att_date, attendance_id;

END;
$$;


-- =========================================================
-- 7. GET ATTENDANCE BY DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_attendance_by_date(
    p_att_date DATE
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE att_date = p_att_date
    ORDER BY attendance_id;

END;
$$;


-- =========================================================
-- 8. GET EMPLOYEE ATTENDANCE BY DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_employee_attendance_by_date(
    p_employee_id INTEGER,
    p_att_date DATE
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE employee_id = p_employee_id
      AND att_date = p_att_date
    ORDER BY attendance_id;

END;
$$;


-- =========================================================
-- 9. GET ATTENDANCE BY STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_attendance_by_status(
    p_status VARCHAR
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE LOWER(status) = LOWER(p_status)
    ORDER BY attendance_id;

END;
$$;


-- =========================================================
-- 10. GET EMPLOYEE ATTENDANCE BY STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_employee_attendance_by_status(
    p_employee_id INTEGER,
    p_status VARCHAR
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

    RETURN QUERY
    SELECT *
    FROM attendance
    WHERE employee_id = p_employee_id
      AND LOWER(status) = LOWER(p_status)
    ORDER BY attendance_id;

END;
$$;

-- =========================================================
-- GET TEAM ATTENDANCE BY MANAGER
-- =========================================================

CREATE OR REPLACE FUNCTION get_team_attendance(
    p_manager_id INTEGER
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT a.*
FROM attendance a
         JOIN employees e
              ON a.employee_id = e.employee_id
WHERE e.manager_id = p_manager_id
ORDER BY a.att_date DESC;

END;
$$;

CREATE OR REPLACE FUNCTION get_attendance_by_hr(
    p_hr_id INTEGER
)
RETURNS SETOF attendance
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT a.*
FROM attendance a
         JOIN employees e
              ON a.employee_id = e.employee_id
WHERE e.hr_id = p_hr_id
ORDER BY a.att_date DESC, a.attendance_id;
END;
$$;
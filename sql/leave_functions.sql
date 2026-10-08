-- =========================================================
-- 1. CREATE LEAVE
-- =========================================================

CREATE OR REPLACE FUNCTION create_leave(
    p_from_date DATE,
    p_to_date DATE,
    p_reason VARCHAR,
    p_status VARCHAR,
    p_employee_id INTEGER
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

INSERT INTO leaves (
    from_date,
    to_date,
    reason,
    status,
    employee_id
)
VALUES (
           p_from_date,
           p_to_date,
           p_reason,
           p_status,
           p_employee_id
       );

RETURN QUERY
SELECT *
FROM leaves
WHERE leave_id = currval(
        pg_get_serial_sequence('leaves', 'leave_id')
                 );

END;
$$;


-- =========================================================
-- 2. GET ALL LEAVES
-- =========================================================

CREATE OR REPLACE FUNCTION get_all_leaves()
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
ORDER BY leave_id;

END;
$$;


-- =========================================================
-- 3. GET LEAVE BY ID
-- =========================================================

CREATE OR REPLACE FUNCTION get_leave_by_id(
    p_leave_id INTEGER
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE leave_id = p_leave_id;

END;
$$;


-- =========================================================
-- 4. UPDATE LEAVE
-- =========================================================

CREATE OR REPLACE FUNCTION update_leave(
    p_leave_id INTEGER,
    p_from_date DATE,
    p_to_date DATE,
    p_reason VARCHAR,
    p_status VARCHAR,
    p_employee_id INTEGER
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

UPDATE leaves
SET
    from_date = p_from_date,
    to_date = p_to_date,
    reason = p_reason,
    status = p_status,
    employee_id = p_employee_id
WHERE leave_id = p_leave_id;

RETURN QUERY
SELECT *
FROM leaves
WHERE leave_id = p_leave_id;

END;
$$;


-- =========================================================
-- 5. DELETE LEAVE
-- =========================================================

CREATE OR REPLACE FUNCTION delete_leave(
    p_leave_id INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN

DELETE FROM leaves
WHERE leave_id = p_leave_id;

RETURN FOUND;

END;
$$;


-- =========================================================
-- 6. GET LEAVES BY EMPLOYEE
-- =========================================================

CREATE OR REPLACE FUNCTION get_leaves_by_employee(
    p_employee_id INTEGER
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE employee_id = p_employee_id
ORDER BY leave_id;

END;
$$;


-- =========================================================
-- 7. GET LEAVES BY STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_leaves_by_status(
    p_status VARCHAR
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE LOWER(status) = LOWER(p_status)
ORDER BY leave_id;

END;
$$;


-- =========================================================
-- 8. EMPLOYEE + STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_employee_leaves_by_status(
    p_employee_id INTEGER,
    p_status VARCHAR
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE employee_id = p_employee_id
  AND LOWER(status) = LOWER(p_status)
ORDER BY leave_id;

END;
$$;


-- =========================================================
-- 9. GET LEAVES BY FROM DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_leaves_by_from_date(
    p_from_date DATE
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE from_date = p_from_date
ORDER BY leave_id;

END;
$$;


-- =========================================================
-- 10. GET LEAVES BY TO DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_leaves_by_to_date(
    p_to_date DATE
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE to_date = p_to_date
ORDER BY leave_id;

END;
$$;


-- =========================================================
-- 11. GET LEAVES BETWEEN FROM DATES
-- =========================================================

CREATE OR REPLACE FUNCTION get_leaves_between_dates(
    p_start_date DATE,
    p_end_date DATE
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM leaves
WHERE from_date BETWEEN p_start_date AND p_end_date
ORDER BY from_date, leave_id;

END;
$$;

--withdraw leaves

CREATE OR REPLACE FUNCTION withdraw_leave(
    p_leave_id INTEGER
)
RETURNS TABLE (
    leave_id INTEGER,
    from_date DATE,
    to_date DATE,
    reason VARCHAR(255),
    status VARCHAR,
    employee_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY

UPDATE leaves
SET status = 'WITHDRAWN'
WHERE leaves.leave_id = p_leave_id
  AND UPPER(leaves.status) = 'PENDING'

    RETURNING
        leaves.leave_id,
        leaves.from_date,
        leaves.to_date,
        leaves.reason,
        leaves.status,
        leaves.employee_id;

END;
$$;

-- =========================================================
-- 12. GET LEAVE REQUESTS BY MANAGER
-- =========================================================

CREATE OR REPLACE FUNCTION get_manager_leave_requests(
    p_manager_id INTEGER
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT l.*
FROM leaves l
         INNER JOIN employees e
                    ON l.employee_id = e.employee_id
WHERE e.manager_id = p_manager_id
ORDER BY l.leave_id;

END;
$$;

-- =========================================================
-- UPDATE MANAGER LEAVE STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION update_manager_leave_status(
    p_leave_id INTEGER,
    p_manager_id INTEGER,
    p_status VARCHAR
)
RETURNS SETOF leaves
LANGUAGE plpgsql
AS $$
BEGIN

    -- Only APPROVED or REJECTED are allowed
    IF UPPER(p_status) NOT IN ('APPROVED', 'REJECTED') THEN
        RAISE EXCEPTION 'Invalid leave status';
END IF;

RETURN QUERY
UPDATE leaves l
SET status = UPPER(p_status)
WHERE l.leave_id = p_leave_id
  AND l.status = 'PENDING'
  AND EXISTS (
    SELECT 1
    FROM employees e
    WHERE e.employee_id = l.employee_id
      AND e.manager_id = p_manager_id
)
    RETURNING l.*;

END;
$$;

CREATE OR REPLACE FUNCTION get_manager_leave_requests(
    p_manager_id INTEGER
)
RETURNS TABLE (
    leave_id INTEGER,
    from_date DATE,
    to_date DATE,
    reason VARCHAR,
    status VARCHAR,
    employee_id INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY

SELECT
    l.leave_id,
    l.from_date,
    l.to_date,
    l.reason,
    l.status,
    l.employee_id

FROM leaves l

         JOIN employees e
              ON l.employee_id = e.employee_id

WHERE e.manager_id = p_manager_id

ORDER BY l.leave_id DESC;

END;
$$;
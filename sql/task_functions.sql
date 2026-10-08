-- =========================================================
-- 1. CREATE TASK
-- =========================================================

CREATE OR REPLACE FUNCTION create_task(
    p_task_name VARCHAR,
    p_description VARCHAR,
    p_priority VARCHAR,
    p_status VARCHAR,
    p_start_date DATE,
    p_due_date DATE,
    p_project_id INTEGER,
    p_assigned_to INTEGER
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

INSERT INTO tasks (
    task_name,
    description,
    priority,
    status,
    start_date,
    due_date,
    project_id,
    assigned_to
)
VALUES (
           p_task_name,
           p_description,
           p_priority,
           p_status,
           p_start_date,
           p_due_date,
           p_project_id,
           p_assigned_to
       );

RETURN QUERY
SELECT *
FROM tasks
WHERE task_id = currval(
        pg_get_serial_sequence('tasks', 'task_id')
                );

END;
$$;


-- =========================================================
-- 2. GET ALL TASKS
-- =========================================================

CREATE OR REPLACE FUNCTION get_all_tasks()
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 3. GET TASK BY ID
-- =========================================================

CREATE OR REPLACE FUNCTION get_task_by_id(
    p_task_id INTEGER
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE task_id = p_task_id;

END;
$$;


-- =========================================================
-- 4. UPDATE TASK
-- =========================================================

CREATE OR REPLACE FUNCTION update_task(
    p_task_id INTEGER,
    p_task_name VARCHAR,
    p_description VARCHAR,
    p_priority VARCHAR,
    p_status VARCHAR,
    p_start_date DATE,
    p_due_date DATE,
    p_project_id INTEGER,
    p_assigned_to INTEGER
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

UPDATE tasks
SET
    task_name = p_task_name,
    description = p_description,
    priority = p_priority,
    status = p_status,
    start_date = p_start_date,
    due_date = p_due_date,
    project_id = p_project_id,
    assigned_to = p_assigned_to
WHERE task_id = p_task_id;

RETURN QUERY
SELECT *
FROM tasks
WHERE task_id = p_task_id;

END;
$$;


-- =========================================================
-- 5. DELETE TASK
-- =========================================================

CREATE OR REPLACE FUNCTION delete_task(
    p_task_id INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN

DELETE FROM tasks
WHERE task_id = p_task_id;

RETURN FOUND;

END;
$$;


-- =========================================================
-- 6. SEARCH TASKS BY NAME
-- =========================================================

CREATE OR REPLACE FUNCTION search_tasks_by_name(
    p_task_name VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE task_name ILIKE '%' || p_task_name || '%'
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 7. GET TASKS BY PROJECT
-- =========================================================

CREATE OR REPLACE FUNCTION get_tasks_by_project(
    p_project_id INTEGER
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE project_id = p_project_id
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 8. GET TASKS BY EMPLOYEE
-- =========================================================
CREATE OR REPLACE FUNCTION get_tasks_by_employee(
    p_employee_id INTEGER
)
RETURNS TABLE (
    task_id INTEGER,
    task_name VARCHAR,
    description TEXT,
    priority VARCHAR,
    status VARCHAR,
    start_date DATE,
    due_date DATE,
    project_id INTEGER,
    assigned_to INTEGER,
    project_name VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT
    t.task_id,
    t.task_name,
    t.description,
    t.priority,
    t.status,
    t.start_date,
    t.due_date,
    t.project_id,
    t.assigned_to,
    p.project_name
FROM tasks t
         LEFT JOIN projects p
                   ON t.project_id = p.project_id
WHERE t.assigned_to = p_employee_id
ORDER BY t.task_id;

END;
$$;


-- =========================================================
-- 9. GET TASKS BY STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_tasks_by_status(
    p_status VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE LOWER(status) = LOWER(p_status)
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 10. GET TASKS BY PRIORITY
-- =========================================================

CREATE OR REPLACE FUNCTION get_tasks_by_priority(
    p_priority VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE LOWER(priority) = LOWER(p_priority)
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 11. EMPLOYEE + STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_employee_tasks_by_status(
    p_employee_id INTEGER,
    p_status VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE assigned_to = p_employee_id
  AND LOWER(status) = LOWER(p_status)
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 12. PROJECT + STATUS
-- =========================================================

CREATE OR REPLACE FUNCTION get_project_tasks_by_status(
    p_project_id INTEGER,
    p_status VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE project_id = p_project_id
  AND LOWER(status) = LOWER(p_status)
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 13. GET TASKS BY DUE DATE
-- =========================================================

CREATE OR REPLACE FUNCTION get_tasks_by_due_date(
    p_due_date DATE
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE due_date = p_due_date
ORDER BY task_id;

END;
$$;


-- =========================================================
-- 14. GET TASKS BETWEEN DUE DATES
-- =========================================================

CREATE OR REPLACE FUNCTION get_tasks_between_due_dates(
    p_start_date DATE,
    p_end_date DATE
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE due_date BETWEEN p_start_date AND p_end_date
ORDER BY due_date, task_id;

END;
$$;


-- =========================================================
-- 15. GET OVERDUE TASKS
-- =========================================================

CREATE OR REPLACE FUNCTION get_overdue_tasks(
    p_date DATE,
    p_completed_status VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
BEGIN

RETURN QUERY
SELECT *
FROM tasks
WHERE due_date < p_date
  AND LOWER(status) <> LOWER(p_completed_status)
ORDER BY due_date, task_id;

END;
$$;


-- =========================================================
-- 16. SORT TASKS
-- =========================================================

CREATE OR REPLACE FUNCTION sort_tasks(
    p_field VARCHAR,
    p_direction VARCHAR
)
RETURNS SETOF tasks
LANGUAGE plpgsql
AS $$
DECLARE
v_field VARCHAR;
    v_direction VARCHAR;
BEGIN

    v_field := LOWER(p_field);
    v_direction := LOWER(p_direction);

    IF v_field NOT IN (
        'task_id',
        'task_name',
        'priority',
        'status',
        'start_date',
        'due_date'
    ) THEN
        RAISE EXCEPTION 'Invalid sort field: %', p_field;
END IF;

    IF v_direction NOT IN ('asc', 'desc') THEN
        RAISE EXCEPTION 'Invalid sort direction: %', p_direction;
END IF;

RETURN QUERY EXECUTE
        'SELECT * FROM tasks ORDER BY '
        || quote_ident(v_field)
        || ' '
        || v_direction;

END;
$$;

CREATE FUNCTION get_tasks_by_manager(
    p_manager_id INTEGER
)
    RETURNS TABLE (
                      task_id INTEGER,
                      task_name VARCHAR,
                      description VARCHAR,
                      priority VARCHAR,
                      status VARCHAR,
                      start_date DATE,
                      due_date DATE,
                      project_id INTEGER,
                      project_name VARCHAR,
                      assigned_to INTEGER
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    t.task_id,
    t.task_name,
    t.description,
    t.priority,
    t.status,
    t.start_date,
    t.due_date,
    t.project_id,
    p.project_name,
    t.assigned_to
FROM tasks t
         LEFT JOIN projects p
                   ON t.project_id = p.project_id
         JOIN employees e
              ON t.assigned_to = e.employee_id
WHERE e.manager_id = p_manager_id
ORDER BY t.task_id DESC;
END;
$$;
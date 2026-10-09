-- =========================================================
-- PROJECT FUNCTIONS
-- =========================================================


-- 1. CREATE PROJECT
CREATE OR REPLACE FUNCTION create_project(
    p_project_name VARCHAR,
    p_start_date DATE,
    p_end_date DATE,
    p_manager_id INTEGER
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
INSERT INTO projects (
    project_name,
    start_date,
    end_date,
    manager_id
)
VALUES (
           p_project_name,
           p_start_date,
           p_end_date,
           p_manager_id
       );

RETURN QUERY
SELECT *
FROM projects
WHERE project_id = currval(
        pg_get_serial_sequence('projects', 'project_id')
                   );
END;
$$;


-- 2. GET ALL PROJECTS
CREATE OR REPLACE FUNCTION get_all_projects()
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
ORDER BY project_id;
END;
$$;


-- 3. GET PROJECT BY ID
CREATE OR REPLACE FUNCTION get_project_by_id(
    p_project_id INTEGER
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE project_id = p_project_id;
END;
$$;


-- 4. UPDATE PROJECT
CREATE OR REPLACE FUNCTION update_project(
    p_project_id INTEGER,
    p_project_name VARCHAR,
    p_start_date DATE,
    p_end_date DATE
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
UPDATE projects
SET
    project_name = p_project_name,
    start_date = p_start_date,
    end_date = p_end_date
WHERE project_id = p_project_id;

RETURN QUERY
SELECT *
FROM projects
WHERE project_id = p_project_id;
END;
$$;


-- 5. DELETE PROJECT
CREATE OR REPLACE FUNCTION delete_project(
    p_project_id INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
DELETE FROM projects
WHERE project_id = p_project_id;

RETURN FOUND;
END;
$$;


-- 6. SEARCH PROJECTS BY NAME
CREATE OR REPLACE FUNCTION search_projects_by_name(
    p_project_name VARCHAR
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE project_name ILIKE '%' || p_project_name || '%'
ORDER BY project_id;
END;
$$;


-- 7. GET PROJECT BY EXACT NAME
CREATE OR REPLACE FUNCTION get_projects_by_exact_name(
    p_project_name VARCHAR
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE LOWER(project_name) = LOWER(p_project_name)
ORDER BY project_id;
END;
$$;


-- 8. GET PROJECTS BY START DATE
CREATE OR REPLACE FUNCTION get_projects_by_start_date(
    p_start_date DATE
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE start_date = p_start_date
ORDER BY project_id;
END;
$$;


-- 9. GET PROJECTS BY END DATE
CREATE OR REPLACE FUNCTION get_projects_by_end_date(
    p_end_date DATE
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE end_date = p_end_date
ORDER BY project_id;
END;
$$;


-- 10. GET PROJECTS BETWEEN START DATES
CREATE OR REPLACE FUNCTION get_projects_between_dates(
    p_start_date DATE,
    p_end_date DATE
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE start_date BETWEEN p_start_date AND p_end_date
ORDER BY start_date, project_id;
END;
$$;


-- 11. GET ONGOING PROJECTS
CREATE OR REPLACE FUNCTION get_ongoing_projects()
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT *
FROM projects
WHERE end_date IS NULL
ORDER BY project_id;
END;
$$;


-- 12. SORT PROJECTS BY FIELD
CREATE OR REPLACE FUNCTION sort_projects(
    p_field VARCHAR,
    p_direction VARCHAR
)
RETURNS SETOF projects
LANGUAGE plpgsql
AS $$
BEGIN

    IF LOWER(p_field) = 'projectname'
       OR LOWER(p_field) = 'project_name' THEN

        IF LOWER(p_direction) = 'desc' THEN
            RETURN QUERY
SELECT *
FROM projects
ORDER BY project_name DESC;
ELSE
            RETURN QUERY
SELECT *
FROM projects
ORDER BY project_name ASC;
END IF;

    ELSIF LOWER(p_field) = 'startdate'
          OR LOWER(p_field) = 'start_date' THEN

        IF LOWER(p_direction) = 'desc' THEN
            RETURN QUERY
SELECT *
FROM projects
ORDER BY start_date DESC;
ELSE
            RETURN QUERY
SELECT *
FROM projects
ORDER BY start_date ASC;
END IF;

    ELSIF LOWER(p_field) = 'enddate'
          OR LOWER(p_field) = 'end_date' THEN

        IF LOWER(p_direction) = 'desc' THEN
            RETURN QUERY
SELECT *
FROM projects
ORDER BY end_date DESC;
ELSE
            RETURN QUERY
SELECT *
FROM projects
ORDER BY end_date ASC;
END IF;

ELSE

        IF LOWER(p_direction) = 'desc' THEN
            RETURN QUERY
SELECT *
FROM projects
ORDER BY project_id DESC;
ELSE
            RETURN QUERY
SELECT *
FROM projects
ORDER BY project_id ASC;
END IF;

END IF;

END;
$$;

CREATE OR REPLACE FUNCTION public.get_projects_by_manager(
    p_manager_id INTEGER
)
RETURNS TABLE (
    project_id INTEGER,
    project_name VARCHAR,
    start_date DATE,
    end_date DATE
)
LANGUAGE plpgsql
AS $$
BEGIN
RETURN QUERY
SELECT
    p.project_id,
    p.project_name,
    p.start_date,
    p.end_date
FROM projects p
WHERE p.manager_id = p_manager_id
ORDER BY p.project_id DESC;
END;
$$;

CREATE OR REPLACE FUNCTION get_unassigned_projects()
RETURNS SETOF projects
LANGUAGE sql
AS $$
SELECT p.*
FROM projects p
WHERE NOT EXISTS (
    SELECT 1
    FROM employee_project ep
    WHERE ep.project_id = p.project_id
)
ORDER BY p.project_id;
$$;

CREATE OR REPLACE FUNCTION public.get_unassigned_projects_by_manager(
    p_manager_id INTEGER
)
RETURNS TABLE(
    project_id INTEGER,
    project_name VARCHAR,
    start_date DATE,
    end_date DATE
)
LANGUAGE plpgsql
AS $function$
BEGIN

RETURN QUERY
SELECT
    p.project_id,
    p.project_name,
    p.start_date,
    p.end_date
FROM projects p
WHERE p.manager_id = p_manager_id
  AND NOT EXISTS (
    SELECT 1
    FROM employee_project ep
    WHERE ep.project_id = p.project_id
)
ORDER BY p.project_id DESC;

END;
$function$;